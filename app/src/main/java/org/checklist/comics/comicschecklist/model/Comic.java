package org.checklist.comics.comicschecklist.model;

public interface Comic {
    int getId();
    String getName();
    String getRelease();
    int getReleaseDate();
    String getDescription();
    String getPrice();
    String getFeature();
    String getCover();
    String getEditor();
    boolean isFavorite();
    boolean isOnCart();
    String getURL();
}
