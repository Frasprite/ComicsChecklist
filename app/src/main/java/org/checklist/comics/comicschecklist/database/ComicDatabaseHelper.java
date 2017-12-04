package org.checklist.comics.comicschecklist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Internal class which supports database creation.
 * @author Francesco Bevilacqua
 */
public class ComicDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "comictable.db";
    private static final int DATABASE_VERSION = 3;

    public ComicDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        ComicDatabase.onCreate(database);
    }

    // Method is called during an upgrade of the database, e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        ComicDatabase.onUpgrade(database, oldVersion, newVersion);
    }
}
