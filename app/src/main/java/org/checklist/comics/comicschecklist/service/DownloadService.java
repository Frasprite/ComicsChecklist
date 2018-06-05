package org.checklist.comics.comicschecklist.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.checklist.comics.comicschecklist.CCApp;
import org.checklist.comics.comicschecklist.database.AppDatabase;
import org.checklist.comics.comicschecklist.ui.ActivityMain;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.log.ParserLog;
import org.checklist.comics.comicschecklist.parser.ParserBonelli;
import org.checklist.comics.comicschecklist.parser.ParserPanini;
import org.checklist.comics.comicschecklist.parser.ParserRW;
import org.checklist.comics.comicschecklist.parser.ParserStar;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.notification.CCNotificationManager;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.DateCreator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class used to download on background info about comics.
 */
public class DownloadService extends IntentService {

    private static final String TAG = DownloadService.class.getSimpleName();
    private boolean error = false;

    /* Init a static map of editor to search */
    private static final Map<Constants.Sections, String> mEditorMap;
    static {
        Map<Constants.Sections, String> aMap = new HashMap<>();
        aMap.put(Constants.Sections.RW, Constants.PREF_RW_LAST_SCAN);
        // By using only Panini, will search for Marvel and Planet Manga as well
        aMap.put(Constants.Sections.PANINI, Constants.PREF_PANINI_LAST_SCAN);
        aMap.put(Constants.Sections.BONELLI, Constants.PREF_BONELLI_LAST_SCAN);
        aMap.put(Constants.Sections.STAR, Constants.PREF_STAR_LAST_SCAN);
        mEditorMap = Collections.unmodifiableMap(aMap);
    }

    public DownloadService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Checking first if we are connected to the web
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            publishResults(Constants.SearchResults.RESULT_NOT_CONNECTED, "noEditor");
            CCNotificationManager.createNotification(this, getResources().getString(R.string.toast_no_connection), false);
            return;
        }

        // Loading frequency and flag if user want to do manual search
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncPref = sharedPref.getString(Constants.PREF_SYNC_FREQUENCY, "3");
        int frequency = Integer.parseInt(syncPref);
        boolean manualSearch = intent.getBooleanExtra(Constants.MANUAL_SEARCH, false);

        CCLogger.d(TAG, "onHandleIntent - Checking if search is manual or automatic " + frequency);

        // Use this code for screenshot
        //myParser.startParseFreeComics();

        if (frequency > -1 && !manualSearch) {
            CCLogger.i(TAG, "onHandleIntent - Automatic search launched");
            automaticSearch(sharedPref, frequency);
        } else {
            CCLogger.i(TAG, "onHandleIntent - Manual search needed");
            Constants.Sections editor = (Constants.Sections) intent.getSerializableExtra(Constants.ARG_EDITOR);
            manualSearch(sharedPref, editor);
        }

        // Check if this is the fist creation of Room database
        boolean createDatabase = intent.getBooleanExtra(Constants.CREATE_DATABASE, false);
        if (createDatabase) {
            // Notify that the database was created and it's ready to be used
            AppDatabase database = AppDatabase.getInstance(this.getApplicationContext());
            database.setDatabaseCreated();
        }

        deleteOldRows();
    }

    @Override
    public void onDestroy() {
        publishResults(Constants.SearchResults.RESULT_DESTROYED, "noEditor");
        // Notification is not needed (only if there isn't an error)
        if (!error) {
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(Constants.NOTIFICATION_ID);
            }
        }
    }

    /**
     * Method which will launch automatic search of contents.
     * @param sharedPref the {@link SharedPreferences} where to load list of editors to search
     * @param frequency the time passed between last search
     */
    private void automaticSearch(SharedPreferences sharedPref, int frequency) {
        boolean notificationPref = sharedPref.getBoolean(Constants.PREF_SEARCH_NOTIFICATION, true);

        // Loading user editor preference
        String[] rawArray = getResources().getStringArray(R.array.pref_basic_editors);
        Set<String> editorSet = sharedPref.getStringSet(Constants.PREF_AVAILABLE_EDITORS, null);

        if (editorSet == null) {
            editorSet = new HashSet<>(Arrays.asList(rawArray));
        }

        for (Map.Entry<Constants.Sections, String> entry : mEditorMap.entrySet()) {
            CCLogger.d(TAG, "automaticSearch - Current editor " + entry.getKey() + " key of last scan" + entry.getValue());
            Constants.Sections currentSection = entry.getKey();
            // Check if editor is desired by user and if last day of scan has passed limits
            if (calculateDayDifference(entry.getValue()) >= frequency &&
                    editorSet.contains(String.valueOf(currentSection.getCode()))) {
                String editorTitle = Constants.Sections.getTitle(currentSection);
                publishResults(Constants.SearchResults.RESULT_START, editorTitle);
                searchComics(notificationPref, editorTitle, currentSection);

                publishResults(Constants.SearchResults.RESULT_FINISHED, "noEditor");
                if (notificationPref) {
                    CCNotificationManager.deleteNotification(this);
                }
            }
        }
    }

    /**
     * Method which will launch automatic search of contents.
     * @param sharedPref the {@link SharedPreferences} where to load list of editors to search
     * @param editor the editor to search
     */
    private void manualSearch(SharedPreferences sharedPref, Constants.Sections editor) {
        boolean notificationPref = sharedPref.getBoolean(Constants.PREF_SEARCH_NOTIFICATION, true);
        if (editor != null) {
            CCLogger.i(TAG, "manualSearch - Manual search for editor " + editor.toString());

            String editorTitle = Constants.Sections.getTitle(editor);
            publishResults(Constants.SearchResults.RESULT_START, editorTitle);
            searchComics(notificationPref, editorTitle, editor);

            // Update last scan for editor on shared preference
            String today = DateCreator.getTodayString();
            switch (editor) {
                case PANINI:
                    sharedPref.edit().putString(Constants.PREF_PANINI_LAST_SCAN, today).apply();
                    break;
                case BONELLI:
                    sharedPref.edit().putString(Constants.PREF_BONELLI_LAST_SCAN, today).apply();
                    break;
                case STAR:
                    sharedPref.edit().putString(Constants.PREF_STAR_LAST_SCAN, today).apply();
                    break;
                case RW:
                    sharedPref.edit().putString(Constants.PREF_RW_LAST_SCAN, today).apply();
                    break;
            }

            publishResults(Constants.SearchResults.RESULT_FINISHED, "noEditor");
            if (notificationPref) {
                CCNotificationManager.deleteNotification(this);
                // Favorite data may have changed, update widget as well
                WidgetService.updateWidget(this);
            }
        }
    }

    /**
     * Start searching for specified comics editor.
     *
     * @param notificationPref boolean indicating if notification are desired
     * @param editorTitle editor personal text
     * @param editor editor to search
     */
    private void searchComics(boolean notificationPref, String editorTitle, Constants.Sections editor) {
        if (notificationPref) {
            CCNotificationManager.createNotification(this, editorTitle + getResources().getString(R.string.search_started), true);
        }

        // Select editor
        switch (editor) {
            case PANINI:
                error = new ParserPanini(getApplicationContext()).startParsing();
                break;
            case STAR:
                error = new ParserStar(getApplicationContext()).startParsing();
                break;
            case BONELLI:
                error = new ParserBonelli(getApplicationContext()).startParsing();
                break;
            case RW:
                error = new ParserRW(getApplicationContext()).startParsing();
                break;
        }

        // See result
        if (error) {
            publishResults(Constants.SearchResults.RESULT_CANCELED, editorTitle);
            if (notificationPref) {
                CCNotificationManager.updateNotification(this, editorTitle + getResources().getString(R.string.search_failed), false);
            }
        } else {
            publishResults(Constants.SearchResults.RESULT_EDITOR_FINISHED, editorTitle);
            if (notificationPref) {
                CCNotificationManager.updateNotification(this, editorTitle + getResources().getString(R.string.search_editor_completed), true);
            }
        }

        ParserLog.printReport();
    }

    /**
     * This method will delete old rows on database.
     */
    private void deleteOldRows() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String deletePref = sharedPref.getString(Constants.PREF_DELETE_FREQUENCY, "-1");
        int frequency = Integer.parseInt(deletePref);
        if (frequency > -1) {
            long time = DateCreator.getPastDay(frequency).getTime();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    int rowsDeleted = ((CCApp) DownloadService.this.getApplicationContext()).getRepository().deleteOldComics(time);
                    CCLogger.d(TAG, "deleteOldRows - Entries deleted: " + rowsDeleted + " with given frequency " + frequency);
                    if (rowsDeleted > 0) {
                        // Update widgets as well
                        WidgetService.updateWidget(DownloadService.this);
                    }
                }
            });
        }
    }

    /**
     * Method which send to {@link ActivityMain} the status of search.
     * @param result the result of search
     * @param editor the editor searched
     */
    private void publishResults(Constants.SearchResults result, String editor) {
        CCLogger.v(TAG, "publishResults - Result of search " + result.name() + " " + editor);
        Intent intent = new Intent(Constants.NOTIFICATION);
        intent.putExtra(Constants.NOTIFICATION_RESULT, result);
        intent.putExtra(Constants.NOTIFICATION_EDITOR, editor);
        sendBroadcast(intent);
    }

    /**
     * This method calculate the difference since last scan for given editor.
     * @param editorLastScan the editor target
     * @return the difference in long data
     */
    private long calculateDayDifference(String editorLastScan) {
        CCLogger.v(TAG, "calculateDayDifference - Editor last scan " + editorLastScan + " - start");
        long result; // If result is 3, we need a refresh
        String today = DateCreator.getTodayString();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String dateStart = sp.getString(editorLastScan, "01/01/2012");

        CCLogger.d(TAG, "calculateDayDifference - (in milliseconds) today is " + today + " target is " + dateStart);
        // Calculate day difference in milliseconds
        long diff = DateCreator.getDifferenceInMillis(today, dateStart);

        result = diff / (24 * 60 * 60 * 1000);
        sp.edit().putString(editorLastScan, today).apply();

        // Set default value in case of error
        if (result < 0) {
            result = 3;
        }

        CCLogger.v(TAG, "calculateDayDifference - result is " + result + " - end");

        return result;
    }
}
