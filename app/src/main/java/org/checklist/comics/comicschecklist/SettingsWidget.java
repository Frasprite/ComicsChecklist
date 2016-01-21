package org.checklist.comics.comicschecklist;

import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RemoteViews;

import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.provider.WidgetProvider;

/**
 * Created by Francesco Bevilacqua on 05/01/2015.
 * This code is part of Comics Checklist project.
 */
public class SettingsWidget extends ListActivity {

    private static final String TAG = SettingsWidget.class.getSimpleName();

    String[] mEditor;
    ArrayAdapter<String> mAdapter;
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        // Set list of editors
        mEditor = getResources().getStringArray(R.array.widget_editors_array);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mEditor);
        getListView().setAdapter(mAdapter);
    }

    @Override
    protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
        Log.d(TAG, "SettingsWidget onListItemClick " + position);
        Context mContext = getApplicationContext();
        // Take text reference of editor chosen
        String editor, title;
        switch (position) {
            case 0:
                editor = Constants.Editors.FAVORITE.name();
                title = mEditor[0];
                break;
            case 1:
                editor = Constants.Editors.CART.name();
                title = mEditor[1];
                break;
            case 2:
                editor = Constants.Editors.MARVEL.name();
                title = mEditor[2];
                break;
            case 3:
                editor = Constants.Editors.PANINI.name();
                title = mEditor[3];
                break;
            case 4:
                editor = Constants.Editors.PLANET.name();
                title = mEditor[4];
                break;
            case 5:
                editor = Constants.Editors.STAR.name();
                title = mEditor[5];
                break;
            case 6:
                editor = Constants.Editors.BONELLI.name();
                title = mEditor[6];
                break;
            case 7:
                editor = Constants.Editors.RW.name();
                title = mEditor[7];
                break;
            default:
                editor = Constants.Editors.FAVORITE.name();
                title = mEditor[0];
                break;
        }

        // Update the widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget);
        appWidgetManager.updateAppWidget(mAppWidgetId, views);

        // Return intent
        Intent resultValue = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, WidgetProvider.class);//new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        resultValue.putExtra(Constants.WIDGET_TITLE, title);
        resultValue.putExtra(Constants.WIDGET_EDITOR, editor);
        setResult(RESULT_OK, resultValue);

        finish();
    }
}
