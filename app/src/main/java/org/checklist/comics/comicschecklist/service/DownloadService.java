package org.checklist.comics.comicschecklist.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import org.checklist.comics.comicschecklist.ActivityMain;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.log.ParserLog;
import org.checklist.comics.comicschecklist.parser.ParserBonelli;
import org.checklist.comics.comicschecklist.parser.ParserPanini;
import org.checklist.comics.comicschecklist.parser.ParserRW;
import org.checklist.comics.comicschecklist.parser.ParserStar;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
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

    // Will be called asynchronously by Android
    @Override
    protected void onHandleIntent(Intent intent) {
        boolean searchNecessary = false;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncPref = sharedPref.getString(Constants.PREF_SYNC_FREQUENCY, "3");
        int frequency = Integer.parseInt(syncPref);
        boolean manualSearch = intent.getBooleanExtra(Constants.MANUAL_SEARCH, false);

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        // Loading user editor preference
        String[] rawArray = getResources().getStringArray(R.array.pref_basic_editors);
        Set<String> editorSet = sharedPref.getStringSet(Constants.PREF_AVAILABLE_EDITORS, null);

        if (editorSet == null) {
            editorSet = new HashSet<>(Arrays.asList(rawArray));
        }

        if (isConnected) {
            CCLogger.d(TAG, "onHandleIntent - Checking if search is manual or automatic " + frequency);

            // Use this code for screenshot
            //myParser.startParseFreeComics();

            if (frequency > -1 && !manualSearch) {
                CCLogger.i(TAG, "onHandleIntent - Automatic search launched");
                boolean notificationPref = sharedPref.getBoolean("notifications_new_message", true);

                for (Map.Entry<Constants.Sections, String> entry : mEditorMap.entrySet()) {
                    CCLogger.d(TAG, "onHandleIntent - Current editor " + entry.getKey() + " key of last scan" + entry.getValue());
                    Constants.Sections currentSection = entry.getKey();
                    // Check if editor is desired by user and if last day of scan has passed limits
                    if (calculateDayDifference(entry.getValue()) >= frequency &&
                            editorSet.contains(String.valueOf(currentSection.getCode()))) {
                        searchNecessary = true;
                        String editorTitle = Constants.Sections.getTitle(currentSection);
                        publishResults(Constants.SearchResults.RESULT_START, editorTitle);
                        searchComics(notificationPref, editorTitle, currentSection);
                    }
                }

                if (searchNecessary) {
                    publishResults(Constants.SearchResults.RESULT_FINISHED, "noEditor");
                    if (notificationPref) {
                        CCNotificationManager.deleteNotification(this);
                    }
                }
            } else {
                Constants.Sections editor = (Constants.Sections) intent.getSerializableExtra(Constants.ARG_EDITOR);
                boolean notificationPref = sharedPref.getBoolean(Constants.PREF_SEARCH_NOTIFICATION, true);
                if (editor != null) {
                    CCLogger.i(TAG, "onHandleIntent - Manual search for editor " + editor.toString());
                    searchNecessary = true;
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
                } else {
                    searchNecessary = false;
                }

                if (searchNecessary) {
                    publishResults(Constants.SearchResults.RESULT_FINISHED, "noEditor");
                    if (notificationPref) {
                        CCNotificationManager.deleteNotification(this);
                        // Favorite data may have changed, update widget as well
                        WidgetService.updateWidget(this);
                    }
                }
            }
        } else {
            error = true;
            publishResults(Constants.SearchResults.RESULT_NOT_CONNECTED, "noEditor");
            CCNotificationManager.createNotification(this, getResources().getString(R.string.toast_no_connection), false);
        }

        deleteOldRows();
    }

    @Override
    public void onDestroy() {
        publishResults(Constants.SearchResults.RESULT_DESTROYED, "noEditor");
        // Notification is not needed (only if there isn't an error)
        if (!error) {
            NotificationManager nMgr = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            nMgr.cancel(Constants.NOTIFICATION_ID);
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
                //error = new ParserPanini(getApplicationContext()).startParsing();
                break;
            case STAR:
                //error = new ParserStar(getApplicationContext()).startParsing();
                break;
            case BONELLI:
                error = new ParserBonelli(getApplicationContext()).startParsing();
                break;
            case RW:
                //error = new ParserRW(getApplicationContext()).startParsing();
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
        int rowsDeleted = 0;
        if (frequency > -1) {
            // Defines selection criteria for the rows to delete
            String mSelectionClause = ComicDatabase.COMICS_DATE_KEY + "<?";
            String[] mSelectionArgs = {"" + DateCreator.getPastDay(frequency).getTime()};
            rowsDeleted = ComicDatabaseManager.delete(this,
                    ComicContentProvider.CONTENT_URI,   // the comic content URI
                    mSelectionClause,                   // the column to select on
                    mSelectionArgs                      // the value to compare to
            );

            CCLogger.d(TAG, "deleteOldRows - Entries deleted: " + rowsDeleted);
        }

        if (rowsDeleted > 0) {
            // Update widgets as well
            WidgetService.updateWidget(this);
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
