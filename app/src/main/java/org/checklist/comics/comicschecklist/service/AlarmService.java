package org.checklist.comics.comicschecklist.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import org.checklist.comics.comicschecklist.ActivityMain;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.receiver.AlarmReceiver;
import org.checklist.comics.comicschecklist.util.CCLogger;
import org.checklist.comics.comicschecklist.util.DateCreator;

/**
 * Class which fire a Notification for incoming comic.
 */
public class AlarmService extends IntentService {

    private static final String TAG = AlarmService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 1;

    public AlarmService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        CCLogger.d(TAG, "onHandleIntent - start");
        // Do the work that requires your app to keep the CPU running.
        String[] columns = new String[]{ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_EDITOR_KEY, ComicDatabase.COMICS_RELEASE_KEY};
        String selection = ComicDatabase.COMICS_RELEASE_KEY + " LIKE ? AND " + ComicDatabase.COMICS_FAVORITE_KEY + " LIKE ?";
        String[] selectionArguments = new String[]{DateCreator.getTodayString(), "yes"};
        String order = ComicDatabase.COMICS_DATE_KEY + " " + "ASC";
        Cursor cursor = ComicDatabaseManager.query(this, ComicContentProvider.CONTENT_URI, columns, selection,
                selectionArguments, order);
        if (cursor != null) {
            if (cursor.getCount() == 1) {
                CCLogger.i(TAG, "onHandleIntent - Favorite comic found");
                createNotification(getResources().getString(R.string.notification_comic_out), false);
            } else if (cursor.getCount() > 1) {
                CCLogger.i(TAG, "onHandleIntent - Founded more favorite comics");
                String text = getResources().getString(R.string.notification_comics_out_1) + " " + cursor.getCount() + " " +
                                getResources().getString(R.string.notification_comics_out_2);
                createNotification(text, false);
            }
            cursor.close();
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
    }

    private void createNotification(String message, boolean bool) {
        CCLogger.d(TAG, "onHandleIntent - Creating notification for favorite alert");
        // Prepare intent which is triggered if the notification is selected
        Intent intent = new Intent(this, ActivityMain.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Add a sound to notification
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // Build notification
        // TODO update deprecated class
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message).setSmallIcon(R.drawable.ic_stat_notification)
                .setSound(sound)
                .setContentIntent(pIntent);

        if (bool)
            mBuilder.setProgress(0, 0, true);

        mBuilder.setContentIntent(pIntent);
        // Sets an ID for the notification and gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
