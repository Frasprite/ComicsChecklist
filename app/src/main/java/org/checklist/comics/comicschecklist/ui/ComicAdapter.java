package org.checklist.comics.comicschecklist.ui;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.databinding.ListItemViewBinding;
import org.checklist.comics.comicschecklist.model.Comic;

import java.util.List;
import java.util.Objects;

public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.ComicViewHolder> {

    List<? extends Comic> mComicList;

    @Nullable
    private final ComicClickCallback mComicClickCallback;

    public ComicAdapter(@Nullable ComicClickCallback clickCallback) {
        mComicClickCallback = clickCallback;
    }

    public void setComicList(final List<? extends Comic> comicList) {
        if (mComicList == null) {
            mComicList = comicList;
            notifyItemRangeInserted(0, comicList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mComicList.size();
                }

                @Override
                public int getNewListSize() {
                    return comicList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mComicList.get(oldItemPosition).getId() ==
                            comicList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Comic newComic = comicList.get(newItemPosition);
                    Comic oldComic = mComicList.get(oldItemPosition);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        return newComic.getId() == oldComic.getId()
                                && Objects.equals(newComic.getDescription(), oldComic.getDescription())
                                && Objects.equals(newComic.getName(), oldComic.getName())
                                && Objects.equals(newComic.getPrice(), oldComic.getPrice());
                    } else {
                        return newComic.getId() == oldComic.getId()
                                && newComic.getDescription().equalsIgnoreCase(oldComic.getDescription())
                                && newComic.getName().equalsIgnoreCase(oldComic.getName())
                                && newComic.getPrice().equalsIgnoreCase(oldComic.getPrice());
                    }
                }
            });
            mComicList = comicList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    public ComicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ListItemViewBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.list_item_view,
                        parent, false);
        binding.setCallback(mComicClickCallback);
        return new ComicViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ComicViewHolder holder, int position) {
        holder.binding.setComic(mComicList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mComicList == null ? 0 : mComicList.size();
    }

    static class ComicViewHolder extends RecyclerView.ViewHolder {

        final ListItemViewBinding binding;

        public ComicViewHolder(ListItemViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /*private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > mLastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
            viewToAnimate.startAnimation(animation);
            mLastPosition = position;
        }
    }*/
}
