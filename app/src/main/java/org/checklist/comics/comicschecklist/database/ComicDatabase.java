package org.checklist.comics.comicschecklist.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import org.checklist.comics.comicschecklist.util.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;

public class ComicDatabase {

    private static final String TAG = ComicDatabase.class.getSimpleName();

    public static final String COMICS_TABLE = "comic";
    public static final String ID = "_id";
    public static final String COMICS_NAME_KEY = "name";
    public static final String COMICS_RELEASE_KEY = "release";
    public static final String COMICS_DATE_KEY = "date";
    public static final String COMICS_DESCRIPTION_KEY = "description";
    public static final String COMICS_PRICE_KEY = "price";
    public static final String COMICS_FEATURE_KEY = "feature";
    public static final String COMICS_COVER_KEY = "cover";
    public static final String COMICS_EDITOR_KEY = "editor";
    public static final String COMICS_FAVORITE_KEY = "favorite";
    public static final String COMICS_CART_KEY = "wishlist";
    public static final String COMICS_URL_KEY = "url";

    /**
     * Table creation code.
     */
    private static final String COMICS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + COMICS_TABLE + " ("
            + ID + " integer primary key autoincrement, "
            + COMICS_NAME_KEY + " text not null, "
            + COMICS_RELEASE_KEY + " text not null, "
            + COMICS_DATE_KEY + " int not null, "
            + COMICS_DESCRIPTION_KEY + " text not null, "
            + COMICS_PRICE_KEY + " text not null, "
            + COMICS_FEATURE_KEY + " text not null, "
            + COMICS_COVER_KEY + " text not null, "
            + COMICS_EDITOR_KEY + " text not null, "
            + COMICS_FAVORITE_KEY + " text not null, "
            + COMICS_CART_KEY + " text not null, "
            + COMICS_URL_KEY + " text not null, "
            + "UNIQUE (" + COMICS_NAME_KEY + ", " + COMICS_EDITOR_KEY + ", " + COMICS_RELEASE_KEY +") ON CONFLICT REPLACE);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(COMICS_TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            CCLogger.w(TAG, "onUpgrade - Upgrading database from version "
                    + oldVersion + " to " + newVersion);
            // Update database by adding new column
            database.execSQL("ALTER TABLE " + COMICS_TABLE + " ADD COLUMN " + COMICS_URL_KEY + " TEXT");
            // Populate new column
            updateRows(database, ComicDatabase.COMICS_URL_KEY, Constants.URL_PANINI, Constants.Sections.getName(Constants.Sections.PANINI));
            updateRows(database, ComicDatabase.COMICS_URL_KEY, Constants.URL_STAR, Constants.Sections.getName(Constants.Sections.STAR));
            updateRows(database, ComicDatabase.COMICS_URL_KEY, Constants.URL_BONELLI, Constants.Sections.getName(Constants.Sections.BONELLI));
            updateRows(database, ComicDatabase.COMICS_URL_KEY, Constants.URL_RW, Constants.Sections.getName(Constants.Sections.RW));
        }

        // N.B.:
        // http://www.sqlite.org/lang_altertable.html
        // It is not possible to remove a column on SQLite.

        if (oldVersion == 2 && newVersion == 3) {
            CCLogger.w(TAG, "onUpgrade - Upgrading database from version "
                    + oldVersion + " to " + newVersion);
            // Change all comics with "marvelitalia" and "planetmanga" to "paninicomics"
            updateRows(database, ComicDatabase.COMICS_EDITOR_KEY, Constants.Sections.getName(Constants.Sections.PANINI), "marvelitalia");
            updateRows(database, ComicDatabase.COMICS_EDITOR_KEY, Constants.Sections.getName(Constants.Sections.PANINI), "planetmanga");
        }
    }

    private static void updateRows(SQLiteDatabase database, String columnName, String data, String editorName) {
        // Add default value to new column
        ContentValues mUpdateValues = new ContentValues();
        mUpdateValues.put(columnName, data);
        // Defines selection criteria for the rows you want to update
        String mSelectionClause = ComicDatabase.COMICS_EDITOR_KEY +  "=?";
        String[] mSelectionArgs = new String[]{String.valueOf(editorName)};
        int rows = database.update(COMICS_TABLE, mUpdateValues, mSelectionClause, mSelectionArgs);
        CCLogger.v(TAG, "updateRows - Total rows updated: " + rows);
    }
}
