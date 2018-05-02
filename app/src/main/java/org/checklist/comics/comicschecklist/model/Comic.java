package org.checklist.comics.comicschecklist.model;

import java.util.Date;

public interface Comic {
    String getName();
    Date getReleaseDate();
    String getDescription();
    String getPrice();
    String getFeature();
    String getCover();
    String getEditor();
    boolean isFavorite();
    boolean isOnCart();
    String getURL();
}
