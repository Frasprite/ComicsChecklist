package org.checklist.comics.comicschecklist.ui;

import org.checklist.comics.comicschecklist.database.entity.ComicEntity;

public interface ComicClickCallback {
    void onClick(ComicEntity comic);
}
