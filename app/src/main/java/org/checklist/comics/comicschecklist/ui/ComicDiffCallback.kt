package org.checklist.comics.comicschecklist.ui

import android.os.Build
import android.support.v7.util.DiffUtil

import org.checklist.comics.comicschecklist.database.entity.ComicEntity

class ComicDiffCallback internal constructor(private val mNewComics: List<ComicEntity>, private val mOldComics: List<ComicEntity>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return mOldComics.size
    }

    override fun getNewListSize(): Int {
        return mNewComics.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldComics[oldItemPosition].id == mNewComics[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val newComic = mNewComics[newItemPosition]
        val oldComic = mOldComics[oldItemPosition]
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            (newComic.id == oldComic.id
                    && newComic.isFavorite == oldComic.isFavorite
                    && newComic.isOnCart == oldComic.isOnCart
                    && newComic.releaseDate == oldComic.releaseDate)
        } else {
            (newComic.id == oldComic.id
                    && newComic.isFavorite == oldComic.isFavorite
                    && newComic.isOnCart == oldComic.isOnCart
                    && newComic.releaseDate == oldComic.releaseDate)
        }
    }

}
