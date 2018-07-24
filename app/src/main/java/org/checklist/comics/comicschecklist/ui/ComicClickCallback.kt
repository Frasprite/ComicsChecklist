package org.checklist.comics.comicschecklist.ui

import org.checklist.comics.comicschecklist.database.entity.ComicEntity

interface ComicClickCallback {
    fun onClick(comic: ComicEntity)
}
