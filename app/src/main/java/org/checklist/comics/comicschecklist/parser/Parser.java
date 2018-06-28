package org.checklist.comics.comicschecklist.parser;

import org.checklist.comics.comicschecklist.database.entity.ComicEntity;

import java.util.ArrayList;

/**
 * Abstract class which give instruction how sub-class should parse data from HTML pages.
 */
public abstract class Parser {

    public abstract ArrayList<ComicEntity> initParser();
    public abstract ArrayList<ComicEntity> parseUrl(String url);
    public abstract String searchTitle(Object object);
    public abstract String searchReleaseDate(Object object);
    public abstract String searchDescription(Object object);
    public abstract String searchCover(Object object);
    public abstract String searchFeature(Object object);
    public abstract String searchPrice(Object object);

}
