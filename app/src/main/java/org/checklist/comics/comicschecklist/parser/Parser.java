package org.checklist.comics.comicschecklist.parser;

import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.log.CCLogger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Abstract class which give instruction how sub-class should parse data from HTML pages.
 */
public abstract class Parser {

    private static final String TAG = Parser.class.getSimpleName();

    public abstract ArrayList<ComicEntity> initParser();
    public abstract ArrayList<ComicEntity> parseUrl(String url);
    public abstract String searchTitle(Object object);
    public abstract String searchReleaseDate(Object object);
    public abstract String searchDescription(Object object);
    public abstract String searchCover(Object object);
    public abstract String searchFeature(Object object);
    public abstract String searchPrice(Object object);

    /**
     * Elaborate a date, in {@link Date} format, from given date which is in {@link String} format.
     * @param releaseDate the date to elaborate
     * @return the date in another format
     */
    Date elaborateDate(String releaseDate) {
        CCLogger.v(TAG, "elaborateDate - start");
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(releaseDate);
        } catch (ParseException e) {
            CCLogger.w(TAG, "elaborateDate - Error while elaborating Date from " + releaseDate + " " + e.toString());
        } finally {
            if (date == null) {
                date = new Date();
                date.setTime(System.currentTimeMillis());
                CCLogger.d(TAG, "elaborateDate - Setting date from current time");
            }
        }
        CCLogger.v(TAG, "elaborateDate - end - " + date.toString());
        return date;
    }

}
