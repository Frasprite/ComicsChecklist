package org.checklist.comics.comicschecklist.parser;

import android.content.Context;

import org.checklist.comics.comicschecklist.CCApp;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.util.DateCreator;

import java.util.ArrayList;

/**
 * Class which parse data from HTML pages.
 */
public abstract class Parser {

    private static final String TAG = Parser.class.getSimpleName();

    final Context mContext;

    /**
     * Class constructor.
     *
     * @param context the {@link Context} for saving data
     */
    Parser(Context context) {
        mContext = context;
    }

    public abstract boolean startParsing();
    public abstract boolean parseUrl(String url);
    public abstract String searchTitle(Object object);
    public abstract String searchReleaseDate(Object object);
    public abstract String searchDescription(Object object);
    public abstract String searchCover(Object object);
    public abstract String searchFeature(Object object);
    public abstract String searchPrice(Object object);

    /**
     * This method is used for elaborate screenshot of copyright free comics.
     */
    public void startParseFreeComics() {
        CCLogger.d(TAG, "startParseFreeComics - start");

        ArrayList<ComicEntity> comicEntities = new ArrayList<>();

        // Adding comics to cart
        comicEntities.add(new ComicEntity("Sport Stars 4",
                DateCreator.elaborateDate("29/06/2016"),
                "da comprare",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/27228.jpg",
                "N.D.",
                "Free",
                true, false,
                "http://digitalcomicmuseum.com/"));
        comicEntities.add(new ComicEntity("Football Thrills 1",
                DateCreator.elaborateDate("02/07/2016"),
                "da comprare",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/27227.jpg",
                "N.D.",
                "Free",
                true, false,
                "http://digitalcomicmuseum.com/"));
        comicEntities.add(new ComicEntity("True Confidence 3",
                DateCreator.elaborateDate("07/07/2016"),
                "da comprare",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/27223.jpg",
                "N.D.",
                "Free",
                true, false,
                "http://digitalcomicmuseum.com/"));

        // Adding favorite comics
        comicEntities.add(new ComicEntity("Wanted Comics 11",
                DateCreator.elaborateDate("01/07/2016"),
                "preferiti",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/20652.jpg",
                "N.D.",
                "Free",
                false, true,
                "http://digitalcomicmuseum.com/"));
        comicEntities.add(new ComicEntity("Billy The Kid 13",
                DateCreator.elaborateDate("03/07/2016"),
                "preferiti",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/27228.jpg",
                "N.D.",
                "Free",
                false, true,
                "http://digitalcomicmuseum.com/"));
        comicEntities.add(new ComicEntity("Woman in red 10",
                DateCreator.elaborateDate("04/07/2016"),
                "preferiti",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/17932.jpg",
                "N.D.",
                "Free",
                false, true,
                "http://digitalcomicmuseum.com/"));

        ((CCApp) mContext.getApplicationContext()).getRepository().insertComics(comicEntities);

        CCLogger.v(TAG, "startParseFreeComics - stop");
    }

}
