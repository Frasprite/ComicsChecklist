package org.checklist.comics.comicschecklist.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import org.checklist.comics.comicschecklist.ComicListActivity;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.util.Constants;

/**
 * Created by Francesco Bevilacqua on 05/01/2015.
 * This code is part of Comics Checklist project.
 */
public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = WidgetProvider.class.getSimpleName();

    public static final String CLICK_ACTION = "CLICK_ACTION";
    public static final String COMIC_ID = "COMIC_ID";
    /**public static final String TOAST_ACTION = "TOAST_ACTION";
    public static final String EXTRA_ITEM = "EXTRA_ITEM";*/

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            // Set up the intent that starts the StackViewService, which will
            // provide the views for this collection.
            Intent intent = new Intent(context, WidgetService.class);
            // Add the app widget unique ID to the intent extras.
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            //Uri data = Uri.withAppendedPath(Uri.parse(Constants.URI_SCHEME + "://widget/id/"),  String.valueOf(appWidgetId));
            Log.d(TAG, "WidgetProvider onUpdate widget ID: " + Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)).toString());
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));//(data);
            // Instantiate the RemoteViews object for the app widget layout.
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects to a RemoteViewsService  through the specified intent.
            // This is how you populate the data.
            rv.setRemoteAdapter(R.id.list, intent);
            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews object above.
            rv.setEmptyView(R.id.list, R.id.empty_view);

            //
            // Do additional processing specific to this app widget...
            Intent clickIntent = new Intent(context, WidgetProvider.class);
            clickIntent.setAction(WidgetProvider.CLICK_ACTION);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.list, clickPendingIntent);
            //

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "WidgetProvider onReceive");
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        // TODO launch comic detail
        /**if (intent.getAction().equals(TOAST_ACTION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);
            Toast.makeText(context, "Touched view " + viewIndex, Toast.LENGTH_SHORT).show();
        }*/

        if (intent.getAction().equals(CLICK_ACTION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            int comicID = intent.getIntExtra(COMIC_ID, 0);

            //Uri details = Uri.withAppendedPath(ComicContentProvider.CONTENT_URI, "" + empID);
            Intent detailsIntent = new Intent(context, ComicListActivity.class);
            detailsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            detailsIntent.putExtra(Constants.COMIC_ID_FROM_WIDGET, comicID);
            context.startActivity(detailsIntent);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }
}
