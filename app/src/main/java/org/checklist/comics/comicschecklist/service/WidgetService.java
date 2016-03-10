package org.checklist.comics.comicschecklist.service;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.WidgetItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Francesco Bevilacqua on 05/01/2015.
 * This code is part of Comics Checklist project.
 */
public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ComicsRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ComicsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = ComicsRemoteViewsFactory.class.getSimpleName();

    private int mCount = 0;
    private List<WidgetItem> mWidgetItems = new ArrayList<>();
    private Context mContext;
    private int mAppWidgetId;
    private String mEditor, mTitle;
    private Cursor mCursor;

    // TODO create an unique method which will update widgets

    public ComicsRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mEditor = intent.getStringExtra(Constants.WIDGET_EDITOR);
        mTitle = intent.getStringExtra(Constants.WIDGET_TITLE);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "WidgetService onCreate " + mEditor + " " + mTitle  + " " + mAppWidgetId);
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        populateWidget();
    }

    private void populateWidget() {
        mCount = 0;
        mWidgetItems.clear();
        // Order list by DESC or ASC
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String sortOrder = sharedPref.getString("data_order", "ASC");
        if (mEditor.equalsIgnoreCase(Constants.Editors.getName(Constants.Editors.FAVORITE))) {
            Uri uri = ComicContentProvider.CONTENT_URI;
            String[] projection = {ComicDatabase.ID, ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_RELEASE_KEY, ComicDatabase.COMICS_DATE_KEY,
                    ComicDatabase.COMICS_DESCRIPTION_KEY, ComicDatabase.COMICS_PRICE_KEY, ComicDatabase.COMICS_FEATURE_KEY, ComicDatabase.COMICS_COVER_KEY,
                    ComicDatabase.COMICS_EDITOR_KEY, ComicDatabase.COMICS_FAVORITE_KEY, ComicDatabase.COMICS_CART_KEY};

            mCursor = ComicDatabaseManager.query(mContext, uri, projection, ComicDatabase.COMICS_FAVORITE_KEY + "=?", new String[]{"yes"},
                    ComicDatabase.COMICS_DATE_KEY + " " + sortOrder);
        } else if (mEditor.equalsIgnoreCase(Constants.Editors.getName(Constants.Editors.CART))) {
            Uri uri = ComicContentProvider.CONTENT_URI;
            String[] projection = {ComicDatabase.ID, ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_RELEASE_KEY, ComicDatabase.COMICS_DATE_KEY,
                    ComicDatabase.COMICS_DESCRIPTION_KEY, ComicDatabase.COMICS_PRICE_KEY, ComicDatabase.COMICS_FEATURE_KEY, ComicDatabase.COMICS_COVER_KEY,
                    ComicDatabase.COMICS_EDITOR_KEY, ComicDatabase.COMICS_FAVORITE_KEY, ComicDatabase.COMICS_CART_KEY};

            mCursor = ComicDatabaseManager.query(mContext, uri, projection, ComicDatabase.COMICS_CART_KEY + "=?", new String[]{"yes"},
                    ComicDatabase.COMICS_DATE_KEY + " " + sortOrder);
        } else {
            Log.d(TAG, "Editor founded, query database " + mEditor);
            Uri uri = ComicContentProvider.CONTENT_URI;
            String[] projection = {ComicDatabase.ID, ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_RELEASE_KEY, ComicDatabase.COMICS_DATE_KEY,
                    ComicDatabase.COMICS_DESCRIPTION_KEY, ComicDatabase.COMICS_PRICE_KEY, ComicDatabase.COMICS_FEATURE_KEY, ComicDatabase.COMICS_COVER_KEY,
                    ComicDatabase.COMICS_EDITOR_KEY, ComicDatabase.COMICS_FAVORITE_KEY, ComicDatabase.COMICS_CART_KEY};

            mCursor = ComicDatabaseManager.query(mContext, uri, projection, ComicDatabase.COMICS_EDITOR_KEY + "=?", new String[]{mEditor},
                    ComicDatabase.COMICS_DATE_KEY + " " + sortOrder);
        }
        int mID;
        String mName;
        String mRelease;
        if (mCursor != null) {
            Log.d(TAG, "Cursor has data");
            for (int i = 0; i < mCursor.getCount(); i++) {
                mCursor.moveToNext();
                mID = mCursor.getInt(mCursor.getColumnIndex(ComicDatabase.ID));
                mName = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_NAME_KEY));
                mRelease = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_RELEASE_KEY));

                mWidgetItems.add(new WidgetItem(mID, mName, mRelease));
                mCount = mCount + 1;
            }

            mCursor.close();
        }
    }

    @Override
    public void onDataSetChanged() {
        populateWidget();
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.i(TAG, "WidgetService getViewAt for position: " + position);
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_widget);
        rv.setTextViewText(R.id.widget_tv1, mWidgetItems.get(position)._name);
        rv.setTextViewText(R.id.widget_tv2, mWidgetItems.get(position)._release);

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Constants.COMIC_ID_FROM_WIDGET, mWidgetItems.get(position)._comicID);
        fillInIntent.putExtra(Constants.COMIC_EDITOR_FROM_WIDGET, mEditor);
        rv.setOnClickFillInIntent(R.id.widget_layout, fillInIntent);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
