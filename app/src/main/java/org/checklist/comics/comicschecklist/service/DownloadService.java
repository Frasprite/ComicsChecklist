package org.checklist.comics.comicschecklist.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.checklist.comics.comicschecklist.ActivityMain;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.parser.Parser;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.DateCreator;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Francesco Bevilacqua on 25/10/2014.
 * This code is part of ComicsChecklist project.
 */
public class DownloadService extends IntentService {

    private static final String TAG = DownloadService.class.getSimpleName();
    private boolean error = false;

    public DownloadService() {
        super(TAG);
    }

    // Will be called asynchronously by Android
    @Override
    protected void onHandleIntent(Intent intent) {
        Parser myParser = new Parser(getApplicationContext());
        boolean searchNecessary = false;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncPref = sharedPref.getString(Constants.PREF_SYNC_FREQUENCY, "3");
        int frequency = Integer.parseInt(syncPref);
        boolean manualSearch = intent.getBooleanExtra(Constants.MANUAL_SEARCH, false);

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        // Loading user editor preference
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String[] rawArray = getResources().getStringArray(R.array.pref_basic_editors);
        Set<String> editorSet = sp.getStringSet(Constants.PREF_AVAILABLE_EDITORS, null);

        if (editorSet == null) {
            editorSet = new HashSet<>(Arrays.asList(rawArray));
        }

        if (isConnected) {
            Log.d(TAG, "Checking if search is manual or automatic " + frequency);

            // Use this code for screenshot
            //myParser.startParseFreeComics();

            if (frequency > -1 && !manualSearch) {
                Log.i(TAG, "Automatic search launched");
                boolean notificationPref = sharedPref.getBoolean("notifications_new_message", true);

                // RW scan
                if (calculateDayDifference(Constants.PREF_RW_LAST_SCAN) >= frequency &&
                        editorSet.contains(String.valueOf(Constants.Sections.RW.getCode()))) {
                    searchNecessary = true;
                    String editorTitle = Constants.Sections.getTitle(Constants.Sections.RW);
                    publishResults(Constants.RESULT_START, editorTitle);
                    searchComics(myParser, notificationPref, editorTitle, Constants.Sections.RW);
                }

                // Marvel scan
                if (calculateDayDifference(Constants.PREF_MARVEL_LAST_SCAN) >= frequency &&
                        editorSet.contains(String.valueOf(Constants.Sections.MARVEL.getCode()))) {
                    searchNecessary = true;
                    String editorTitle = Constants.Sections.getTitle(Constants.Sections.MARVEL);
                    publishResults(Constants.RESULT_START, editorTitle);
                    searchComics(myParser, notificationPref, editorTitle, Constants.Sections.MARVEL);
                }

                // Panini Comics scan
                if (calculateDayDifference(Constants.PREF_PANINI_LAST_SCAN) >= frequency &&
                        editorSet.contains(String.valueOf(Constants.Sections.PANINI.getCode()))) {
                    searchNecessary = true;
                    String editorTitle = Constants.Sections.getTitle(Constants.Sections.PANINI);
                    publishResults(Constants.RESULT_START, editorTitle);
                    searchComics(myParser, notificationPref, editorTitle, Constants.Sections.PANINI);
                }

                // Planet Manga scan
                if (calculateDayDifference(Constants.PREF_PLANET_LAST_SCAN) >= frequency &&
                        editorSet.contains(String.valueOf(Constants.Sections.PLANET.getCode()))) {
                    searchNecessary = true;
                    String editorTitle = Constants.Sections.getTitle(Constants.Sections.PLANET);
                    publishResults(Constants.RESULT_START, editorTitle);
                    searchComics(myParser, notificationPref, editorTitle, Constants.Sections.PLANET);
                }

                // Bonelli scan
                if (calculateDayDifference(Constants.PREF_BONELLI_LAST_SCAN) >= frequency &&
                        editorSet.contains(String.valueOf(Constants.Sections.BONELLI.getCode()))) {
                    searchNecessary = true;
                    String editorTitle = Constants.Sections.getTitle(Constants.Sections.BONELLI);
                    publishResults(Constants.RESULT_START, editorTitle);
                    searchComics(myParser, notificationPref, editorTitle, Constants.Sections.BONELLI);
                }

                // Star comics scan
                if (calculateDayDifference(Constants.PREF_STAR_LAST_SCAN) >= frequency &&
                        editorSet.contains(String.valueOf(Constants.Sections.STAR.getCode()))) {
                    searchNecessary = true;
                    String editorTitle = Constants.Sections.getTitle(Constants.Sections.STAR);
                    publishResults(Constants.RESULT_START, editorTitle);
                    searchComics(myParser, notificationPref, editorTitle, Constants.Sections.STAR);
                }

                if (searchNecessary) {
                    publishResults(Constants.RESULT_FINISHED, "noEditor");
                    if (notificationPref) {
                        createNotification(getResources().getString(R.string.search_completed), false);
                    }
                }
            } else {
                Constants.Sections editor = (Constants.Sections) intent.getSerializableExtra(Constants.ARG_EDITOR);
                boolean notificationPref = sharedPref.getBoolean(Constants.PREF_SEARCH_NOTIFICATION, true);
                if (editor != null) {
                    Log.i(TAG, "Manual search for editor " + editor.toString());
                    searchNecessary = true;
                    String editorTitle = Constants.Sections.getTitle(editor);
                    publishResults(Constants.RESULT_START, editorTitle);
                    searchComics(myParser, notificationPref, editorTitle, editor);
                } else {
                    searchNecessary = false;
                }

                if (searchNecessary) {
                    publishResults(Constants.RESULT_FINISHED, "noEditor");
                    if (notificationPref) {
                        createNotification(getResources().getString(R.string.search_completed), false);
                        // Favorite data may have changed, update widget as well
                        WidgetService.updateWidget(this);
                    }
                }
            }
        } else {
            error = true;
            publishResults(Constants.RESULT_NOT_CONNECTED, "noEditor");
            createNotification(getResources().getString(R.string.toast_no_connection), false);
        }

        deleteOldRows();
    }

    @Override
    public void onDestroy() {
        publishResults(Constants.RESULT_DESTROYED, "noEditor");
        // Notification is not needed (only if there isn't an error)
        if (!error) {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) this.getSystemService(ns);
            nMgr.cancel(Constants.NOTIFICATION_ID);
        }
    }

    /**
     * Start searching for specified comics editor.
     * @param myParser the parser class which will search on the web
     * @param notificationPref boolean indicating if notification are desired
     * @param editorTitle editor personal text
     * @param editor editor to search
     */
    private void searchComics(Parser myParser, boolean notificationPref, String editorTitle, Constants.Sections editor) {
        if (notificationPref) {
            createNotification(editorTitle + getResources().getString(R.string.search_started), true);
        }

        // Select editor
        switch (editor) {
            case MARVEL:
                error = myParser.startParsePanini(Constants.Sections.getName(Constants.Sections.MARVEL));
                break;
            case PANINI:
                error = myParser.startParsePanini(Constants.Sections.getName(Constants.Sections.PANINI));
                break;
            case PLANET:
                error = myParser.startParsePanini(Constants.Sections.getName(Constants.Sections.PLANET));
                break;
            case STAR:
                error = myParser.startParseStarC();
                break;
            case BONELLI:
                error = myParser.startParseBonelli();
                break;
            case RW:
                error = myParser.startParseRW();
                break;
        }

        // See result
        if (error) {
            publishResults(Constants.RESULT_CANCELED, editorTitle);
            if (notificationPref) {
                createNotification(editorTitle + getResources().getString(R.string.search_failed), false);
            }
        } else {
            publishResults(Constants.RESULT_EDITOR_FINISHED, editorTitle);
            if (notificationPref) {
                createNotification(editorTitle + getResources().getString(R.string.search_editor_completed), true);
            }
        }
    }

    /**
     * Method which create the notification.
     * @param message the message to show
     * @param bool whether or not to show an indeterminate process
     */
    private void createNotification(String message, boolean bool) {
        Log.v(TAG, "Creating notification");
        // Prepare intent which is triggered if the notification is selected
        Intent intent = new Intent(this, ActivityMain.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Build notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message).setSmallIcon(R.drawable.ic_stat_notification)
                .setContentIntent(pIntent);

        if (bool) {
            mBuilder.setProgress(0, 0, true);
        }

        mBuilder.setContentIntent(pIntent);
        // Sets an ID for the notification and gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(Constants.NOTIFICATION_ID, mBuilder.build());
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
            Calendar calendar = Calendar.getInstance();
            int x = frequency * -1;
            calendar.add(Calendar.DAY_OF_YEAR, x);
            Date sevenDaysAgo = calendar.getTime();
            // Defines selection criteria for the rows to delete
            String mSelectionClause = ComicDatabase.COMICS_DATE_KEY + "<?";
            String[] mSelectionArgs = {"" + sevenDaysAgo.getTime()};
            rowsDeleted = ComicDatabaseManager.delete(this,
                    ComicContentProvider.CONTENT_URI,   // the comic content URI
                    mSelectionClause,                   // the column to select on
                    mSelectionArgs                      // the value to compare to
            );

            Log.d(TAG, "Entries deleted: " + rowsDeleted);
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
    private void publishResults(int result, String editor) {
        Log.v(TAG, "Result of search " + result + " " + editor);
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
        Log.v(TAG, "calculateDayDifference " + editorLastScan + " - start");
        long result; // If result is 3, we need a refresh
        String today = DateCreator.getTodayString();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String dateStart = sp.getString(editorLastScan, "01/01/2012");

        Log.d(TAG, "calculateDayDifference - date is " + today);
        Date d1 = DateCreator.elaborateDate(dateStart);
        Date d2 = DateCreator.elaborateDate(today);

        // Calculate day difference in milliseconds
        long diff = d2.getTime() - d1.getTime();

        result = diff / (24 * 60 * 60 * 1000);
        sp.edit().putString(editorLastScan, today).apply();

        // Set default value in case of error
        if (result < 0) {
            result = 3;
        }

        Log.v(TAG, "calculateDayDifference " + result + " - end");

        return result;
    }
}
