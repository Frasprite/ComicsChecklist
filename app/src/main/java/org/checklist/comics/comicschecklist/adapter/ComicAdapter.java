package org.checklist.comics.comicschecklist.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.ComicDatabase;

public class ComicAdapter extends RecyclerView.Adapter<ComicAdapter.ComicViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;

    private ComicViewHolder.ViewHolderClicks mClickListener;
    private int mLastPosition = -1;

    public ComicAdapter(Context context, Cursor cursor, ComicViewHolder.ViewHolderClicks listener) {
        mContext = context;
        mCursor = cursor;
        mDataValid = cursor != null;
        mRowIdColumn = mDataValid ? mCursor.getColumnIndex(ComicDatabase.ID) : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mCursor != null) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }

        mClickListener = listener;
    }

    @Override
    public ComicAdapter.ComicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_view, parent, false);
        return new ComicViewHolder(view, mClickListener);
    }

    @Override
    public void onBindViewHolder(ComicViewHolder holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("This should only be called when the cursor is valid!");
        }

        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Couldn't move cursor to position " + position);
        }

        mCursor.moveToPosition(position);
        holder.mNameTextView.setText(mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_NAME_KEY)));
        holder.mReleaseTextView.setText(mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_RELEASE_KEY)));

        // Apply the animation when the view is bound
        setAnimation(holder.itemView, position);
    }

    @Override
    public void onViewDetachedFromWindow(ComicViewHolder holder) {
        holder.clearAnimation();
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        }

        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }

        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > mLastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
            viewToAnimate.startAnimation(animation);
            mLastPosition = position;
        }
    }

    /**
     * Method used to return the cursor.
     * @return the {@link Cursor} containing all data
     */
    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     */
    private void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor. <br>
     * Unlike {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }

        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }

        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow(ComicDatabase.ID);
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    public static class ComicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mNameTextView;
        TextView mReleaseTextView;

        ViewHolderClicks mListener;

        public interface ViewHolderClicks {
            void itemClicked(View container, int position);
        }

        ComicViewHolder(View itemView, ViewHolderClicks listener) {
            super(itemView);

            mListener = listener;
            itemView.setOnClickListener(this);

            mNameTextView = itemView.findViewById(R.id.comic_name_view);
            mReleaseTextView = itemView.findViewById(R.id.comic_release_view);
        }

        @Override
        public void onClick(View view) {
            mListener.itemClicked(view, this.getAdapterPosition());
        }

        void clearAnimation() {
            this.itemView.clearAnimation();
        }
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataValid = false;
            notifyDataSetChanged();
            // N.B.: There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
        }
    }
}
