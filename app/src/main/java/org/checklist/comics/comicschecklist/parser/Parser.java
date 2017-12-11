package org.checklist.comics.comicschecklist.parser;

import android.content.Context;

import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.util.CCLogger;
import org.checklist.comics.comicschecklist.util.DateCreator;

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
    public Parser(Context context) {
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

        // Adding 3 comics to cart
        ComicDatabaseManager.insert(mContext, "Sport Stars 4", "da comprare", "N.D.", "29/06/2016",
                DateCreator.elaborateDate("29/06/2016"), "http://digitalcomicmuseum.com/thumbnails/27228.jpg", "N.D.", "Free", "yes", "no", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Football Thrills 1", "da comprare", "N.D.", "02/07/2016",
                DateCreator.elaborateDate("02/07/2016"), "http://digitalcomicmuseum.com/thumbnails/27227.jpg", "N.D.", "Free", "yes", "no", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "True Confidence 3", "da comprare", "N.D.", "07/07/2016",
                DateCreator.elaborateDate("07/07/2016"), "http://digitalcomicmuseum.com/thumbnails/27223.jpg", "N.D.", "Free", "yes", "no", "http://digitalcomicmuseum.com/");

        // Adding 7 comics to favorite
        ComicDatabaseManager.insert(mContext, "Wanted Comics 11", "preferiti", "N.D.", "01/07/2016",
                DateCreator.elaborateDate("01/07/2016"), "http://digitalcomicmuseum.com/thumbnails/20652.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Billy The Kid 13", "preferiti", "N.D.", "03/07/2016",
                DateCreator.elaborateDate("03/07/2016"), "http://digitalcomicmuseum.com/thumbnails/27228.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Woman in red 10", "preferiti", "N.D.", "04/07/2016",
                DateCreator.elaborateDate("04/07/2016"), "http://digitalcomicmuseum.com/thumbnails/17932.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Out of this world adventures 1", "preferiti", "N.D.", "10/07/2016",
                DateCreator.elaborateDate("10/07/2016"), "http://digitalcomicmuseum.com/thumbnails/1105.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Whiz Comics 6", "preferiti", "N.D.", "20/07/2016",
                DateCreator.elaborateDate("20/07/2016"), "http://digitalcomicmuseum.com/thumbnails/19861.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Ghost Comics 67", "preferiti", "N.D.", "26/07/2016",
                DateCreator.elaborateDate("26/07/2016"), "http://digitalcomicmuseum.com/thumbnails/1860.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Strange World 100", "preferiti", "N.D.", "01/08/2016",
                DateCreator.elaborateDate("01/08/2016"), "http://digitalcomicmuseum.com/thumbnails/7806.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        CCLogger.v(TAG, "startParseFreeComics - stop");
    }

}
