package org.checklist.comics.comicschecklist.ui;

import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import org.checklist.comics.comicschecklist.database.entity.ComicEntity;

import java.util.List;
import java.util.Objects;

public class ComicDiffCallback extends DiffUtil.Callback {

    private List<ComicEntity> mOldComics;
    private List<ComicEntity> mNewComics;

    ComicDiffCallback(List<ComicEntity> newComics, List<ComicEntity> oldComics) {
        this.mNewComics = newComics;
        this.mOldComics = oldComics;
    }

    @Override
    public int getOldListSize() {
        return mOldComics.size();
    }

    @Override
    public int getNewListSize() {
        return mNewComics.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldComics.get(oldItemPosition).getId() ==
                mNewComics.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        ComicEntity newComic = mNewComics.get(newItemPosition);
        ComicEntity oldComic = mOldComics.get(oldItemPosition);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return newComic.getId() == oldComic.getId()
                    && Objects.equals(newComic.isFavorite(), oldComic.isFavorite())
                    && Objects.equals(newComic.isOnCart(), oldComic.isOnCart())
                    && Objects.equals(newComic.getReleaseDate(), oldComic.getReleaseDate());
        } else {
            return newComic.getId() == oldComic.getId()
                    && newComic.isFavorite() == oldComic.isFavorite()
                    && newComic.isOnCart() == oldComic.isOnCart()
                    && newComic.getReleaseDate().equals(oldComic.getReleaseDate());
        }
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Can return particular field for changed item
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
