package org.checklist.comics.comicschecklist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Francesco Bevilacqua on 31/10/2014.
 * This code is part of Comics Checklist project.
 */
public class CartDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "carttable.db";
    private static final int DATABASE_VERSION = 1;

    public CartDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        CartDatabase.onCreate(database);
    }

    // Method is called during an upgrade of the database, e.g. if you increase the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        CartDatabase.onUpgrade(database, oldVersion, newVersion);
    }
}
