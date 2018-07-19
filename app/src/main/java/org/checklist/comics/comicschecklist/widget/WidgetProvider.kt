package org.checklist.comics.comicschecklist.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

import org.checklist.comics.comicschecklist.ui.ActivityMain
import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.ui.WidgetSettings
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.util.Constants

/**
 * Bridge class for widgets UI.
 */
class WidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it
        for (appWidgetId in appWidgetIds) {
            WidgetSettings.deleteTitlePref(context, appWidgetId)
        }

        super.onDeleted(context, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        private val TAG = WidgetProvider::class.java.simpleName

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                            appWidgetId: Int) {

            val widgetText = WidgetSettings.loadTitlePref(context, appWidgetId)
            CCLogger.d(TAG, "updateAppWidget - editor $widgetText id $appWidgetId")
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget)
            views.setTextViewText(R.id.widgetTextView, widgetText)

            // Set up the intent that starts the StackViewService, which will
            // provide the views for this collection.
            val intent = Intent(context, WidgetService::class.java)
            // Add the app widget unique ID to the intent extras.
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.putExtra(Constants.WIDGET_TITLE, widgetText)
            intent.putExtra(Constants.WIDGET_EDITOR, Constants.Sections.getName(widgetText as String))
            CCLogger.d(TAG, "updateAppWidget - widget ID: " + Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)).toString())
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects to a RemoteViewsService  through the specified intent.
            // This is how you populate the data.
            views.setRemoteAdapter(R.id.list, intent)
            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews object above.
            views.setEmptyView(R.id.list, R.id.empty_view)

            // Redirect on click event on list item to activity
            val startActivityIntent = Intent(context, ActivityMain::class.java)
            startActivityIntent.action = Constants.ACTION_COMIC_WIDGET
            startActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setPendingIntentTemplate(R.id.list, startActivityPendingIntent)

            // Redirect on widget button click event on add comic activity
            val addComicIntent = Intent(context, ActivityMain::class.java)
            addComicIntent.action = Constants.ACTION_WIDGET_ADD
            addComicIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val actionAddPendingIntent = PendingIntent.getActivity(context, 0, addComicIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.widgetAddButton, actionAddPendingIntent)

            // Redirect on widget button click event to open app
            val openAppIntent = Intent(context, ActivityMain::class.java)
            openAppIntent.action = Constants.ACTION_WIDGET_OPEN_APP
            openAppIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val actionOpenAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.widgetTextView, actionOpenAppPendingIntent)

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
