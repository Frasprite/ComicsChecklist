package org.checklist.comics.comicschecklist.service;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.checklist.comics.comicschecklist.CCApp;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.provider.WidgetProvider;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.WidgetItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class which support widgets.
 */
public class WidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ComicsRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    /**
     * Method used to update widget used on home screen.
     * @param context the context caller
     */
    public static void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list);
    }
}

class ComicsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = ComicsRemoteViewsFactory.class.getSimpleName();

    private final List<WidgetItem> mWidgetItems = new ArrayList<>();
    private final Context mContext;
    private final int mAppWidgetId;
    private final String mEditor, mTitle;

    // TODO Widget content is empty at creation, but if screen is rotated it will appear
    // TODO when app is updated, widget content disappear
    /*
    https://medium.com/the-wtf-files/the-mysterious-case-of-the-disappearing-widget-f4177a8e2c2b
    https://gist.github.com/rock3r/9809139
    https://github.com/frakbot/FWeather
    https://stackoverflow.com/questions/8304387/android-how-do-i-force-the-update-of-all-widgets-of-a-particular-kind/8304682#8304682
     */
    ComicsRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mEditor = intent.getStringExtra(Constants.WIDGET_EDITOR);
        mTitle = intent.getStringExtra(Constants.WIDGET_TITLE);
    }

    @Override
    public void onCreate() {
        CCLogger.i(TAG, "onCreate - WidgetService onCreate " + mEditor + " " + mTitle  + " " + mAppWidgetId);
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        populateWidget();
    }

    private void populateWidget() {
        mWidgetItems.clear();

        // Load data based on selected editor
        Constants.Sections editor = Constants.Sections.getEditorFromName(mEditor);
        CCLogger.d(TAG, "populateWidget - Raw editor " + mEditor + " gives " + editor);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<ComicEntity> list = ((CCApp) mContext).getRepository().loadComicsByEditorSync(mEditor);
                CCLogger.d(TAG, "populateWidget - List : " + list);

                int mID;
                String mName;
                String mRelease;
                if (list != null) {
                    CCLogger.d(TAG, "populateWidget - List has data : " + list.size());
                    for (ComicEntity comicEntity : list) {
                        mID = comicEntity.getId();
                        mName = comicEntity.getName();
                        mRelease = mContext.getString(R.string.format, comicEntity.getReleaseDate().getTime());

                        mWidgetItems.add(new WidgetItem(mID, mName, mRelease));
                    }
                }
            }
        });
    }

    @Override
    public void onDataSetChanged() {
        CCLogger.v(TAG, "onDataSetChanged");
    }

    @Override
    public void onDestroy() {
        CCLogger.v(TAG, "onDestroy");
    }

    @Override
    public int getCount() {
        return mWidgetItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        CCLogger.i(TAG, "getViewAt - position: " + position);
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_widget);
        rv.setTextViewText(R.id.widget_tv1, mWidgetItems.get(position)._name);
        rv.setTextViewText(R.id.widget_tv2, mWidgetItems.get(position)._release);

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(Constants.COMIC_ID_FROM_WIDGET, mWidgetItems.get(position)._comicID);
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
