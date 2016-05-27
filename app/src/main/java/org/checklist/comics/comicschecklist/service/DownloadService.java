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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Francesco Bevilacqua on 25/10/2014.
 * This code is part of ParserTest project.
 */
public class DownloadService extends IntentService {

    private static final String TAG = DownloadService.class.getSimpleName();
    private boolean error = false;

    public DownloadService() {
        super("DownloadService");
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

        if (isConnected) {
            Log.d(TAG, "Checking if search is manual or automatic " + frequency);
            if (frequency > -1 && !manualSearch) {
                Log.i(TAG, "Automatic search launched");
                boolean notificationPref = sharedPref.getBoolean("notifications_new_message", true);
                if (calculateDayDifference(Constants.PREF_RW_LAST_SCAN) >= frequency) {
                    searchNecessary = true;
                    publishResults(Constants.RESULT_START, getResources().getString(R.string.title_section8));
                    if (notificationPref)
                        createNotification(getResources().getString(R.string.title_section8) + getResources().getString(R.string.search_started), true);
                    error = myParser.startParseRW();
                    if (error) {
                        publishResults(Constants.RESULT_CANCELED, getResources().getString(R.string.title_section8));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section8) + getResources().getString(R.string.search_failed), false);
                    } else {
                        publishResults(Constants.RESULT_EDITOR_FINISHED, getResources().getString(R.string.title_section8));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section8) + getResources().getString(R.string.search_editor_completed), true);
                    }
                }
                if (calculateDayDifference(Constants.PREF_MARVEL_LAST_SCAN) >= frequency) {
                    searchNecessary = true;
                    publishResults(Constants.RESULT_START, getResources().getString(R.string.title_section3));
                    if (notificationPref)
                        createNotification(getResources().getString(R.string.title_section3) + getResources().getString(R.string.search_started), true);
                    error = myParser.startParsePanini(Constants.Editors.getName(Constants.Editors.MARVEL));
                    if (error) {
                        publishResults(Constants.RESULT_CANCELED, getResources().getString(R.string.title_section3));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section3) + getResources().getString(R.string.search_failed), false);
                    } else {
                        publishResults(Constants.RESULT_EDITOR_FINISHED, getResources().getString(R.string.title_section3));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section3) + getResources().getString(R.string.search_editor_completed), true);
                    }
                }
                if (calculateDayDifference(Constants.PREF_PANINI_LAST_SCAN) >= frequency) {
                    searchNecessary = true;
                    publishResults(Constants.RESULT_START, getResources().getString(R.string.title_section4));
                    if (notificationPref)
                        createNotification(getResources().getString(R.string.title_section4) + getResources().getString(R.string.search_started), true);
                    error = myParser.startParsePanini(Constants.Editors.getName(Constants.Editors.PANINI));
                    if (error) {
                        publishResults(Constants.RESULT_CANCELED, getResources().getString(R.string.title_section4));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section4) + getResources().getString(R.string.search_failed), false);
                    } else {
                        publishResults(Constants.RESULT_EDITOR_FINISHED, getResources().getString(R.string.title_section4));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section4) + getResources().getString(R.string.search_editor_completed), true);
                    }
                }
                if (calculateDayDifference(Constants.PREF_PLANET_LAST_SCAN) >= frequency) {
                    searchNecessary = true;
                    publishResults(Constants.RESULT_START, getResources().getString(R.string.title_section5));
                    if (notificationPref)
                        createNotification(getResources().getString(R.string.title_section5) + getResources().getString(R.string.search_started), true);
                    error = myParser.startParsePanini(Constants.Editors.getName(Constants.Editors.PLANET));
                    if (error) {
                        publishResults(Constants.RESULT_CANCELED, getResources().getString(R.string.title_section5));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section5) + getResources().getString(R.string.search_failed), false);
                    } else {
                        publishResults(Constants.RESULT_EDITOR_FINISHED, getResources().getString(R.string.title_section5));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section5) + getResources().getString(R.string.search_editor_completed), true);
                    }
                }
                if (calculateDayDifference(Constants.PREF_BONELLI_LAST_SCAN) >= frequency) {
                    searchNecessary = true;
                    publishResults(Constants.RESULT_START, getResources().getString(R.string.title_section7));
                    if (notificationPref)
                        createNotification(getResources().getString(R.string.title_section7) + getResources().getString(R.string.search_started), true);
                    error = myParser.startParseBonelli();
                    if (error) {
                        publishResults(Constants.RESULT_CANCELED, getResources().getString(R.string.title_section7));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section7) + getResources().getString(R.string.search_failed), false);
                    } else {
                        publishResults(Constants.RESULT_EDITOR_FINISHED, getResources().getString(R.string.title_section7));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section7) + getResources().getString(R.string.search_editor_completed), true);
                    }
                }
                if (calculateDayDifference(Constants.PREF_STAR_LAST_SCAN) >= frequency) {
                    searchNecessary = true;
                    publishResults(Constants.RESULT_START, getResources().getString(R.string.title_section6));
                    if (notificationPref)
                        createNotification(getResources().getString(R.string.title_section6) + getResources().getString(R.string.search_started), true);
                    error = myParser.startParseStarC();
                    if (error) {
                        publishResults(Constants.RESULT_CANCELED, getResources().getString(R.string.title_section6));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section6) + getResources().getString(R.string.search_failed), false);
                    } else {
                        publishResults(Constants.RESULT_EDITOR_FINISHED, getResources().getString(R.string.title_section6));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section6) + getResources().getString(R.string.search_editor_completed), true);
                    }
                }

                if (searchNecessary) {
                    publishResults(Constants.RESULT_FINISHED, "noEditor");
                    if (notificationPref)
                        createNotification(getResources().getString(R.string.search_completed), false);
                }
            } else {
                Constants.Editors editor = (Constants.Editors) intent.getSerializableExtra(Constants.ARG_EDITOR);
                Log.i(TAG, "Manual search for editor " + editor.toString());
                boolean notificationPref = sharedPref.getBoolean(Constants.PREF_SEARCH_NOTIFICATION, true);

                searchNecessary = true;

                switch (editor) {
                    case MARVEL:
                        // Marvel
                        publishResults(Constants.RESULT_START, getResources().getString(R.string.title_section3));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section3) + getResources().getString(R.string.search_started), true);
                        error = myParser.startParsePanini(Constants.Editors.getName(Constants.Editors.MARVEL));
                        if (error) {
                            publishResults(Constants.RESULT_CANCELED, getResources().getString(R.string.title_section3));
                            if (notificationPref)
                                createNotification(getResources().getString(R.string.title_section3) + getResources().getString(R.string.search_failed), false);
                        } else {
                            publishResults(Constants.RESULT_EDITOR_FINISHED, getResources().getString(R.string.title_section3));
                            if (notificationPref)
                                createNotification(getResources().getString(R.string.title_section3) + getResources().getString(R.string.search_editor_completed), true);
                        }
                        break;
                    case PANINI:
                        // Panini
                        publishResults(Constants.RESULT_START, getResources().getString(R.string.title_section4));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section4) + getResources().getString(R.string.search_started), true);
                        error = myParser.startParsePanini(Constants.Editors.getName(Constants.Editors.PANINI));
                        if (error) {
                            publishResults(Constants.RESULT_CANCELED, getResources().getString(R.string.title_section4));
                            if (notificationPref)
                                createNotification(getResources().getString(R.string.title_section4) + getResources().getString(R.string.search_failed), false);
                        } else {
                            publishResults(Constants.RESULT_EDITOR_FINISHED, getResources().getString(R.string.title_section4));
                            if (notificationPref)
                                createNotification(getResources().getString(R.string.title_section4) + getResources().getString(R.string.search_editor_completed), true);
                        }
                        break;
                    case PLANET:
                        // Planet
                        publishResults(Constants.RESULT_START, getResources().getString(R.string.title_section5));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section5) + getResources().getString(R.string.search_started), true);
                        error = myParser.startParsePanini(Constants.Editors.getName(Constants.Editors.PLANET));
                        if (error) {
                            publishResults(Constants.RESULT_CANCELED, getResources().getString(R.string.title_section5));
                            if (notificationPref)
                                createNotification(getResources().getString(R.string.title_section5) + getResources().getString(R.string.search_failed), false);
                        } else {
                            publishResults(Constants.RESULT_EDITOR_FINISHED, getResources().getString(R.string.title_section5));
                            if (notificationPref)
                                createNotification(getResources().getString(R.string.title_section5) + getResources().getString(R.string.search_editor_completed), true);
                        }
                        break;
                    case STAR:
                        // Star
                        publishResults(Constants.RESULT_START, getResources().getString(R.string.title_section6));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section6) + getResources().getString(R.string.search_started), true);
                        error = myParser.startParseStarC();
                        if (error) {
                            publishResults(Constants.RESULT_CANCELED, getResources().getString(R.string.title_section6));
                            if (notificationPref)
                                createNotification(getResources().getString(R.string.title_section6) + getResources().getString(R.string.search_failed), false);
                        } else {
                            publishResults(Constants.RESULT_EDITOR_FINISHED, getResources().getString(R.string.title_section6));
                            if (notificationPref)
                                createNotification(getResources().getString(R.string.title_section6) + getResources().getString(R.string.search_editor_completed), true);
                        }
                        break;
                    case BONELLI:
                        // Bonelli
                        publishResults(Constants.RESULT_START, getResources().getString(R.string.title_section7));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section7) + getResources().getString(R.string.search_started), true);
                        error = myParser.startParseBonelli();
                        if (error) {
                            publishResults(Constants.RESULT_CANCELED, getResources().getString(R.string.title_section7));
                            if (notificationPref)
                                createNotification(getResources().getString(R.string.title_section7) + getResources().getString(R.string.search_failed), false);
                        } else {
                            publishResults(Constants.RESULT_EDITOR_FINISHED, getResources().getString(R.string.title_section7));
                            if (notificationPref)
                                createNotification(getResources().getString(R.string.title_section7) + getResources().getString(R.string.search_editor_completed), true);
                        }
                        break;
                    case RW:
                        // RW
                        publishResults(Constants.RESULT_START, getResources().getString(R.string.title_section8));
                        if (notificationPref)
                            createNotification(getResources().getString(R.string.title_section8) + getResources().getString(R.string.search_started), true);
                        error = myParser.startParseRW();
                        if (error) {
                            publishResults(Constants.RESULT_CANCELED, getResources().getString(R.string.title_section8));
                            if (notificationPref)
                                createNotification(getResources().getString(R.string.title_section8) + getResources().getString(R.string.search_failed), false);
                        } else {
                            publishResults(Constants.RESULT_EDITOR_FINISHED, getResources().getString(R.string.title_section8));
                            if (notificationPref)
                                createNotification(getResources().getString(R.string.title_section8) + getResources().getString(R.string.search_editor_completed), true);
                        }
                        break;
                    default:
                        searchNecessary = false;
                        break;
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

        if (bool)
            mBuilder.setProgress(0, 0, true);

        mBuilder.setContentIntent(pIntent);
        // Sets an ID for the notification and gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(Constants.NOTIFICATION_ID, mBuilder.build());
    }

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

    private void publishResults(int result, String editor) {
        Log.v(TAG, "Result of search " + result + " " + editor);
        Intent intent = new Intent(Constants.NOTIFICATION);
        intent.putExtra(Constants.NOTIFICATION_RESULT, result);
        intent.putExtra(Constants.NOTIFICATION_EDITOR, editor);
        sendBroadcast(intent);
    }

    private long calculateDayDifference(String editorLastScan) {
        Log.v(TAG, "calculateDayDifference " + editorLastScan + " - start");
        long result; // If result is 3, we need a refresh
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String dateStart = sp.getString(editorLastScan, "01/03/2004");
        String today = day + "/" + month + "/" + year;

        // HH converts hour in 24 hours format (0-23), day calculation
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date d1 = format.parse(dateStart);
            Date d2 = format.parse(today);

            //in milliseconds
            long diff = d2.getTime() - d1.getTime();

            result = diff / (24 * 60 * 60 * 1000); //diffDays
            sp.edit().putString(editorLastScan, today).apply();
        } catch (Exception e) {
            result = 3;
        }
        Log.v(TAG, "calculateDayDifference " + result + " - end");

        return result;
    }
}
