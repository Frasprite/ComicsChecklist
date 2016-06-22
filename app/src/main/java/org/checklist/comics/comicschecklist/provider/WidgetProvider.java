package org.checklist.comics.comicschecklist.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import org.checklist.comics.comicschecklist.ActivityMain;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.SettingsWidget;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.util.Constants;

/**
 * Created by Francesco Bevilacqua on 05/01/2015.
 * This code is part of Comics Checklist project.
 */
public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = WidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            SettingsWidget.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    /*@Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.ACTION_WIDGET_ADD)) {
            Log.i(TAG, "Widget button clicked " + Constants.ACTION_WIDGET_ADD);
            // Open add comic activity
            Intent addComicIntent = new Intent(context, ActivityMain.class);
            addComicIntent.setAction(Constants.ACTION_WIDGET_ADD);
            addComicIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(addComicIntent);
        } else {
            Log.i(TAG, "Other action from widget received " + intent.toString());
            super.onReceive(context, intent);
        }
    }*/

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = SettingsWidget.loadTitlePref(context, appWidgetId);
        Log.d(TAG, "onUpdate - editor " + widgetText + " id " + appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.widgetTextView, widgetText);

        // Set up the intent that starts the StackViewService, which will
        // provide the views for this collection.
        Intent intent = new Intent(context, WidgetService.class);
        // Add the app widget unique ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(Constants.WIDGET_TITLE, widgetText);
        intent.putExtra(Constants.WIDGET_EDITOR, Constants.Editors.getName((String) widgetText));
        Log.d(TAG, "updateAppWidget widget ID: " + Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)).toString());
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        // Set up the RemoteViews object to use a RemoteViews adapter.
        // This adapter connects to a RemoteViewsService  through the specified intent.
        // This is how you populate the data.
        views.setRemoteAdapter(R.id.list, intent);
        // The empty view is displayed when the collection has no items.
        // It should be in the same layout used to instantiate the RemoteViews object above.
        views.setEmptyView(R.id.list, R.id.empty_view);

        // Redirect on click event on list item to activity
        Intent startActivityIntent = new Intent(context, ActivityMain.class);
        startActivityIntent.setAction(Constants.ACTION_COMIC_WIDGET);
        startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.list, startActivityPendingIntent);

        // Redirect on widget button click event on add comic activity
        Intent addComicIntent = new Intent(context, ActivityMain.class);
        addComicIntent.setAction(Constants.ACTION_WIDGET_ADD);
        addComicIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent actionAddPendingIntent = PendingIntent.getActivity(context, 0, addComicIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widgetAddButton, actionAddPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
