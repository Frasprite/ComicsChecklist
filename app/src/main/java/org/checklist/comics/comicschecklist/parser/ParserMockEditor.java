package org.checklist.comics.comicschecklist.parser;

import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.log.CCLogger;

import java.util.ArrayList;

public class ParserMockEditor extends Parser {

    private static final String TAG = ParserMockEditor.class.getSimpleName();

    @Override
    public ArrayList<ComicEntity> initParser() {
        return parseUrl("This is a test!!!");
    }

    @Override
    public ArrayList<ComicEntity> parseUrl(String url) {
        CCLogger.d(TAG, "startParseFreeComics - start");

        ArrayList<ComicEntity> comicEntities = new ArrayList<>();

        // Adding comics to cart
        comicEntities.add(new ComicEntity("Sport Stars 4",
                elaborateDate("29/06/2018"),
                "da comprare",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/27228.jpg",
                "N.D.",
                "Free",
                true, false,
                "http://digitalcomicmuseum.com/"));
        comicEntities.add(new ComicEntity("Football Thrills 1",
                elaborateDate("02/07/2018"),
                "da comprare",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/27227.jpg",
                "N.D.",
                "Free",
                true, false,
                "http://digitalcomicmuseum.com/"));
        comicEntities.add(new ComicEntity("True Confidence 3",
                elaborateDate("07/07/2018"),
                "da comprare",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/27223.jpg",
                "N.D.",
                "Free",
                true, false,
                "http://digitalcomicmuseum.com/"));

        // Adding favorite comics
        comicEntities.add(new ComicEntity("Wanted Comics 11",
                elaborateDate("01/07/2018"),
                "preferiti",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/20652.jpg",
                "N.D.",
                "Free",
                false, true,
                "http://digitalcomicmuseum.com/"));
        comicEntities.add(new ComicEntity("Billy The Kid 13",
                elaborateDate("03/07/2018"),
                "preferiti",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/27228.jpg",
                "N.D.",
                "Free",
                false, true,
                "http://digitalcomicmuseum.com/"));
        comicEntities.add(new ComicEntity("Woman in red 10",
                elaborateDate("04/07/2018"),
                "preferiti",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/17932.jpg",
                "N.D.",
                "Free",
                false, true,
                "http://digitalcomicmuseum.com/"));

        CCLogger.v(TAG, "startParseFreeComics - stop");

        return comicEntities;
    }

    @Override
    public String searchTitle(Object object) {
        return null;
    }

    @Override
    public String searchReleaseDate(Object object) {
        return null;
    }

    @Override
    public String searchDescription(Object object) {
        return null;
    }

    @Override
    public String searchCover(Object object) {
        return null;
    }

    @Override
    public String searchFeature(Object object) {
        return null;
    }

    @Override
    public String searchPrice(Object object) {
        return null;
    }
}
