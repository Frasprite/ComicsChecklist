package org.checklist.comics.comicschecklist.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.util.Constants

import org.jetbrains.anko.doAsync

import java.util.ArrayList

/**
 * Service class which support widgets.
 */
class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        return ComicsRemoteViewsFactory(this.applicationContext, intent)
    }

    companion object {

        /**
         * Method used to update widget used on home screen.
         * @param context the context caller
         */
        fun updateWidget(context: Context?) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, WidgetProvider::class.java))
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list)
        }
    }
}

internal class ComicsRemoteViewsFactory
// TODO Widget content is empty at creation, but if screen is rotated it will appear
// TODO when app is updated, widget content disappear
/*
    https://medium.com/the-wtf-files/the-mysterious-case-of-the-disappearing-widget-f4177a8e2c2b
    https://gist.github.com/rock3r/9809139
    https://github.com/frakbot/FWeather
    https://stackoverflow.com/questions/8304387/android-how-do-i-force-the-update-of-all-widgets-of-a-particular-kind/8304682#8304682
     */
(private val mContext: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private val mWidgetItems = ArrayList<WidgetItem>()
    private val mAppWidgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    private val mEditor: String = intent.getStringExtra(Constants.WIDGET_EDITOR)
    private val mTitle: String = intent.getStringExtra(Constants.WIDGET_TITLE)

    override fun onCreate() {
        CCLogger.i(TAG, "onCreate - WidgetService onCreate $mEditor $mTitle $mAppWidgetId")
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        populateWidget()
    }

    private fun populateWidget() {
        mWidgetItems.clear()

        // Load data based on selected editor
        CCLogger.d(TAG, "populateWidget - Raw editor $mEditor")

        doAsync {
            val list = (mContext as CCApp).repository.loadComicsByEditorSync(mEditor)
            CCLogger.d(TAG, "populateWidget - List : " + list!!)

            if (list.isNotEmpty()) {
                CCLogger.d(TAG, "populateWidget - List has data : " + list.size)
                for (comicEntity in list) {
                    mWidgetItems.add(WidgetItem(comicEntity.id, comicEntity.name,
                            mContext.getString(R.string.format, comicEntity.releaseDate.time)))
                }
            }
        }
    }

    override fun onDataSetChanged() {
        CCLogger.v(TAG, "onDataSetChanged")
    }

    override fun onDestroy() {
        CCLogger.v(TAG, "onDestroy")
    }

    override fun getCount(): Int {
        return mWidgetItems.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        CCLogger.i(TAG, "getViewAt - position: $position")
        val rv = RemoteViews(mContext.packageName, R.layout.list_item_widget)
        rv.setTextViewText(R.id.widget_tv1, mWidgetItems[position]._name)
        rv.setTextViewText(R.id.widget_tv2, mWidgetItems[position]._release)

        val fillInIntent = Intent()
        fillInIntent.putExtra(Constants.COMIC_ID_FROM_WIDGET, mWidgetItems[position]._comicID)
        rv.setOnClickFillInIntent(R.id.widget_layout, fillInIntent)

        return rv
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    companion object {

        private val TAG = ComicsRemoteViewsFactory::class.java.simpleName
    }
}
