package org.checklist.comics.comicschecklist.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.ArrayAdapter

import kotlinx.android.synthetic.main.activity_widget_settings.*

import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.extensions.PreferenceHelper
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.util.Constants
import org.checklist.comics.comicschecklist.widget.WidgetProvider

import java.util.ArrayList
import java.util.Arrays
import java.util.HashSet

/**
 * Activity used to managed widget options.
 */
class WidgetSettings : AppCompatActivity() {

    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private var mUpdatableSectionList: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CCLogger.d(TAG, "onCreate - start")
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(Activity.RESULT_CANCELED)

        setContentView(R.layout.activity_widget_settings)
        supportActionBar?.title = getString(R.string.activity_title_chose_content)

        // Defined array values to show in ListView
        val values = resources.getStringArray(R.array.widget_sections_array)

        // Loading user editor preference
        val preferenceHelper = PreferenceHelper.defaultPrefs(this)
        val rawArray = resources.getStringArray(R.array.pref_basic_editors)
        var editorSet = preferenceHelper.getStringSet(Constants.PREF_AVAILABLE_EDITORS, emptySet())

        if (editorSet.isEmpty()) {
            editorSet = HashSet(Arrays.asList(*rawArray))
        }

        // Create fixed array list then a new "open" one
        val sectionList = Arrays.asList(*values)
        mUpdatableSectionList = ArrayList()
        mUpdatableSectionList!!.addAll(sectionList)

        for (editor in editorSet) {
            // Get section and add it to list
            val section = Constants.Sections.fromCode(Integer.parseInt(editor))
            mUpdatableSectionList!!.add(section.title)
        }

        val adapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, mUpdatableSectionList!!)

        widgetList.adapter = adapter
        widgetList.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            CCLogger.d(TAG, "onListItemClick - start - position $position")
            // Take name and title reference of editor chosen
            val editorTitle = mUpdatableSectionList!![position]
            val name = Constants.Sections.fromTitle(editorTitle).sectionName

            // Create widget
            CCLogger.v(TAG, "onListItemClick - end - title $editorTitle name $name")
            createWidget(this@WidgetSettings , editorTitle, name)
        }

        buttonCancel.setOnClickListener { this@WidgetSettings.finish() }

        // Find the widget id from the intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        CCLogger.v(TAG, "onCreate - end")
    }

    private fun createWidget(context: Context, title: String, name: String) {
        CCLogger.i(TAG, "createWidget - title $title name $name")
        // Store the string locally
        saveTitlePref(context, mAppWidgetId, title)

        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        WidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        resultValue.putExtra(Constants.WIDGET_TITLE, title)
        resultValue.putExtra(Constants.WIDGET_EDITOR, name)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    companion object {

        private val TAG = WidgetSettings::class.java.simpleName

        private const val PREFS_NAME = "AppWidget"
        private const val PREF_PREFIX_KEY = "appwidget"

        // Write the prefix to the SharedPreferences object for this widget
        private fun saveTitlePref(context: Context, appWidgetId: Int, text: String) {
            CCLogger.d(TAG, "saveTitlePref - $PREF_PREFIX_KEY$appWidgetId text $text")
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
        fun loadTitlePref(context: Context, appWidgetId: Int): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
            CCLogger.d(TAG, "loadTitlePref - $PREF_PREFIX_KEY $appWidgetId return $titleValue")
            return titleValue ?: Constants.Sections.FAVORITE.sectionName
        }

        fun deleteTitlePref(context: Context, appWidgetId: Int) {
            CCLogger.d(TAG, "deleteTitlePref - $PREF_PREFIX_KEY$appWidgetId")
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }
}
