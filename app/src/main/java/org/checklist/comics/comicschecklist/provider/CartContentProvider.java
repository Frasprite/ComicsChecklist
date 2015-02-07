package org.checklist.comics.comicschecklist.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import org.checklist.comics.comicschecklist.database.CartDatabase;
import org.checklist.comics.comicschecklist.database.CartDatabaseHelper;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Francesco Bevilacqua on 31/10/2014.
 * This code is part of Comics Checklist project.
 */
public class CartContentProvider extends ContentProvider {

    // database
    private CartDatabaseHelper database;

    // used for the UriMacher
    private static final int COMICS = 10;
    private static final int COMIC_ID = 20;

    private static final String AUTHORITY = "org.checklist.comics.comicschecklist.cartprovider";

    private static final String BASE_PATH = "comics";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    //public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/comics";
    //public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/comic";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        // Example: sURIMatcher.addURI("contacts", "people", PEOPLE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, COMICS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", COMIC_ID);
    }

    @Override
    public boolean onCreate() {
        database = new CartDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(CartDatabase.COMICS_TABLE);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case COMICS:
                break;
            case COMIC_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(CartDatabase.ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        //int rowsDeleted = 0;
        long id;
        switch (uriType) {
            case COMICS:
                id = sqlDB.insert(CartDatabase.COMICS_TABLE, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case COMICS:
                rowsDeleted = sqlDB.delete(CartDatabase.COMICS_TABLE, selection, selectionArgs);
                break;
            case COMIC_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(CartDatabase.COMICS_TABLE, CartDatabase.ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(CartDatabase.COMICS_TABLE, CartDatabase.ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated;
        switch (uriType) {
            case COMICS:
                rowsUpdated = sqlDB.update(CartDatabase.COMICS_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case COMIC_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(CartDatabase.COMICS_TABLE,
                            values,
                            CartDatabase.ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(CartDatabase.COMICS_TABLE,
                            values,
                            CartDatabase.ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {CartDatabase.COMICS_NAME_KEY, CartDatabase.ID,
                CartDatabase.COMICS_RELEASE_KEY, CartDatabase.COMICS_DATE_KEY,
                CartDatabase.COMICS_DESCRIPTION_KEY, CartDatabase.COMICS_PRICE_KEY,
                CartDatabase.COMICS_FEATURE_KEY, CartDatabase.COMICS_COVER_KEY,
                CartDatabase.COMICS_EDITOR_KEY, CartDatabase.COMICS_FAVORITE_KEY,
                CartDatabase.COMICS_CART_KEY};
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
