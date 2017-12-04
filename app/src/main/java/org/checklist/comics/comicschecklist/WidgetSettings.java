package org.checklist.comics.comicschecklist;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.checklist.comics.comicschecklist.util.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.provider.WidgetProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity used to managed widget options.
 */
public class WidgetSettings extends AppCompatActivity {

    private static final String TAG = WidgetSettings.class.getSimpleName();

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private static final String PREFS_NAME = "AppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget";

    private ArrayList<String> mUpdatableSectionList;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CCLogger.d(TAG, "onCreate - start");
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.activity_widget_settings);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.activity_title_chose_content));
        }

        // Defined array values to show in ListView
        String[] values = getResources().getStringArray(R.array.widget_sections_array);

        // Loading user editor preference
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String[] rawArray = getResources().getStringArray(R.array.pref_basic_editors);
        Set<String> editorSet = sp.getStringSet(Constants.PREF_AVAILABLE_EDITORS, null);

        if (editorSet == null) {
            editorSet = new HashSet<>(Arrays.asList(rawArray));
        }

        // Create fixed array list then a new "open" one
        List<String> sectionList = Arrays.asList(values);
        mUpdatableSectionList = new ArrayList<>();
        mUpdatableSectionList.addAll(sectionList);

        for (String editor : editorSet) {
            // Get section and add it to list
            Constants.Sections section = Constants.Sections.getEditor(Integer.parseInt(editor));
            if (section != null) {
                mUpdatableSectionList.add(section.getTitle());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, mUpdatableSectionList);

        ListView listView = findViewById(R.id.widget_list) ;
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                CCLogger.d(TAG, "onListItemClick - start - position " + position);
                Context context = WidgetSettings.this;
                String editorTitle = mUpdatableSectionList.get(position);
                Constants.Sections editor = Constants.Sections.getEditorFromTitle(editorTitle);
                // Take name and title reference of editor chosen
                String title = Constants.Sections.getTitle(editor);
                String name = Constants.Sections.getName(editor);
                // Create widget
                CCLogger.v(TAG, "onListItemClick - end - title " + title + " name " + name);
                createWidget(context, title, name);
            }
        });

        Button cancelButton = findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WidgetSettings.this.finish();
            }
        });

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

        CCLogger.v(TAG, "onCreate - end");
    }

    private void createWidget(Context context, String title, String name) {
        CCLogger.i(TAG, "createWidget - title " + title + " name " + name);
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
        CCLogger.d(TAG, "saveTitlePref - " + PREF_PREFIX_KEY + appWidgetId + " text " + text);
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    public static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        CCLogger.d(TAG, "loadTitlePref - " + PREF_PREFIX_KEY + appWidgetId + " return " + titleValue);
        if (titleValue != null) {
            return titleValue;
        } else {
            return Constants.Sections.getTitle(Constants.Sections.FAVORITE);
        }
    }

    public static void deleteTitlePref(Context context, int appWidgetId) {
        CCLogger.d(TAG, "deleteTitlePref - " + PREF_PREFIX_KEY + appWidgetId);
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }
}
