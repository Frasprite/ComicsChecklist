package org.checklist.comics.comicschecklist.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.checklist.comics.comicschecklist.provider.ComicContentProvider;

import java.util.Date;

/**
 * This class will manage all CRUD operation on comic database.
 */
public class ComicDatabaseManager {

    private static final String TAG = ComicDatabaseManager.class.getSimpleName();

    public static long insert(Context context, String name, String editor, String description, String releaseDate,
                              Date date, String coverUrl, String feature, String price, String cartKey, String favoriteKey) {
        Log.v(TAG, "Insert comic " + name + " " + editor + " " + releaseDate);
        ContentValues values = new ContentValues();
        values.put(ComicDatabase.COMICS_NAME_KEY, name);
        values.put(ComicDatabase.COMICS_EDITOR_KEY, editor);
        values.put(ComicDatabase.COMICS_DESCRIPTION_KEY, description);
        values.put(ComicDatabase.COMICS_RELEASE_KEY, releaseDate);
        values.put(ComicDatabase.COMICS_DATE_KEY, date.getTime());
        values.put(ComicDatabase.COMICS_COVER_KEY, coverUrl);
        values.put(ComicDatabase.COMICS_FEATURE_KEY, feature);
        values.put(ComicDatabase.COMICS_PRICE_KEY, price);
        values.put(ComicDatabase.COMICS_CART_KEY, cartKey);
        values.put(ComicDatabase.COMICS_FAVORITE_KEY, favoriteKey);

        Uri uri = context.getContentResolver().insert(ComicContentProvider.CONTENT_URI, values);
        return ContentUris.parseId(uri);
    }

    public static int delete(Context context, Uri uri, String selection, String[] selectionArgs) {
        return context.getContentResolver().delete(
                uri,                         // the comic content URI
                selection,                   // the column to select on
                selectionArgs                // the value to compare to
        );
    }

    public static Cursor query(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return context.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public static int update(Context context, ContentValues mUpdateValues, String mSelectionClause, String[] mSelectionArgs) {
        return context.getContentResolver().update(ComicContentProvider.CONTENT_URI, mUpdateValues, mSelectionClause, mSelectionArgs);
    }
}
