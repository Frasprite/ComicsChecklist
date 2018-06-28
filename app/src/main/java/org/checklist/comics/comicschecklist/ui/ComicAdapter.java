package org.checklist.comics.comicschecklist.ui;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.databinding.ListItemViewBinding;
import org.checklist.comics.comicschecklist.model.Comic;

import java.util.List;

public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.ComicViewHolder> {

    List<? extends Comic> mComicList;

    @Nullable
    private final ComicClickCallback mComicClickCallback;

    ComicAdapter(@Nullable ComicClickCallback clickCallback) {
        mComicClickCallback = clickCallback;
    }

    public void setComicList(final List<? extends Comic> comicList) {
        if (mComicList == null) {
            mComicList = comicList;
            notifyItemRangeInserted(0, comicList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new ComicDiffCallback((List<Comic>) comicList, (List<Comic>) mComicList));
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
}
