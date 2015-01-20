package org.checklist.comics.comicschecklist.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ComicDatabase {

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
            + "UNIQUE (" + COMICS_NAME_KEY + ", " + COMICS_EDITOR_KEY + ", " + COMICS_RELEASE_KEY +") ON CONFLICT REPLACE);";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(COMICS_TABLE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(ComicDatabase.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + COMICS_TABLE);
        onCreate(database);
    }
}
