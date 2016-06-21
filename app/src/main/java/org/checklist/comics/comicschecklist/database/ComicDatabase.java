package org.checklist.comics.comicschecklist.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.checklist.comics.comicschecklist.util.Constants;

public class ComicDatabase {

    private static final String TAG = ComicDatabase.class.getSimpleName();

    // Metadati della tabella
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
     * Codice SQL di creazione della tabella.
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
            Log.w(TAG, "Upgrading database from version "
                    + oldVersion + " to " + newVersion);
            // Update database by adding new column
            database.execSQL("ALTER TABLE " + COMICS_TABLE + " ADD COLUMN " + COMICS_URL_KEY + " TEXT");
            // Populate new column
            updateRows(database, Constants.URLPANINI, Constants.Editors.getName(Constants.Editors.MARVEL));
            updateRows(database, Constants.URLPANINI, Constants.Editors.getName(Constants.Editors.PANINI));
            updateRows(database, Constants.URLPANINI, Constants.Editors.getName(Constants.Editors.PLANET));
            updateRows(database, Constants.IMG_URL, Constants.Editors.getName(Constants.Editors.STAR));
            updateRows(database, Constants.MAIN_URL, Constants.Editors.getName(Constants.Editors.BONELLI));
            updateRows(database, Constants.RW_URL, Constants.Editors.getName(Constants.Editors.RW));
        }
    }

    private static void updateRows(SQLiteDatabase database, String URL, String editorName) {
        // Add default value to new column
        ContentValues mUpdateValues = new ContentValues();
        mUpdateValues.put(ComicDatabase.COMICS_URL_KEY, URL);
        // Defines selection criteria for the rows you want to update
        String mSelectionClause = ComicDatabase.COMICS_EDITOR_KEY +  "=?";
        String[] mSelectionArgs = new String[]{String.valueOf(editorName)};
        int rows = database.update(COMICS_TABLE, mUpdateValues, mSelectionClause, mSelectionArgs);
        Log.v(TAG, "Total rows updated: " + rows);
    }
}
