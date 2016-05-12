package org.checklist.comics.comicschecklist;

import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.provider.WidgetProvider;

/**
 * Created by Francesco Bevilacqua on 05/01/2015.
 * This code is part of Comics Checklist project.
 */
public class SettingsWidget extends ListActivity {

    private static final String TAG = SettingsWidget.class.getSimpleName();

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private static final String PREFS_NAME = "AppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate - start");
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set layout size of activity
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // Defined array values to show in ListView
        String[] values = getResources().getStringArray(R.array.widget_editors_array);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        getListView().setAdapter(adapter);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        Log.v(TAG, "onCreate - end");
    }

    @Override
    protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
        Log.d(TAG, "onListItemClick - start - position " + position);
        Context context = SettingsWidget.this;
        Constants.Editors editor = Constants.Editors.getEditor(position);
        // Take name and title reference of editor chosen
        String title = Constants.Editors.getTitle(editor);
        String name = Constants.Editors.getName(editor);
        // Create widget
        Log.v(TAG, "onListItemClick - end - title " + title + " name " + name);
        createWidget(context, title, name);
    }

    private void createWidget(Context context, String title, String name) {
        Log.i(TAG, "createWidget - title " + title + " name " + name);
        // Store the string locally
        saveTitlePref(context, mAppWidgetId, title);

        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        WidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        resultValue.putExtra(Constants.WIDGET_TITLE, title);
        resultValue.putExtra(Constants.WIDGET_EDITOR, name);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    // Write the prefix to the SharedPreferences object for this widget
    private static void saveTitlePref(Context context, int appWidgetId, String text) {
        Log.d(TAG, "saveTitlePref " + PREF_PREFIX_KEY + appWidgetId + " text " + text);
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    public static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        Log.d(TAG, "loadTitlePref " + PREF_PREFIX_KEY + appWidgetId + " return " + titleValue);
        if (titleValue != null) {
            return titleValue;
        } else {
            return Constants.Editors.getTitle(Constants.Editors.FAVORITE);
        }
    }

    public static void deleteTitlePref(Context context, int appWidgetId) {
        Log.d(TAG, "deleteTitlePref " + PREF_PREFIX_KEY + appWidgetId);
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }
}
