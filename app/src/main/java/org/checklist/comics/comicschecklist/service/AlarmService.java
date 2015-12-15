package org.checklist.comics.comicschecklist.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.checklist.comics.comicschecklist.ComicListActivity;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.receiver.AlarmReceiver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Francesco Bevilacqua on 18/02/2015.
 * This code is part of Comics Checklist project.
 */
public class AlarmService extends IntentService {

    private static final String TAG = AlarmService.class.getSimpleName();

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public AlarmService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        // Do the work that requires your app to keep the CPU running.
        // TODO search on favorite and find out if something is available today, then create a notification
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = df.format(calendar.getTime());
        String selection = ComicDatabase.COMICS_RELEASE_KEY + " LIKE =? AND " + ComicDatabase.COMICS_FAVORITE_KEY + " =?";
        String[] selectionArguments = new String[]{formattedDate, "yes"};
        Cursor cursor = this.getContentResolver().query(ComicContentProvider.CONTENT_URI, null, selection,
                selectionArguments, null);

        createNotification("AlarmManagerDemo: this is a test message!", false);

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
    }

    private void createNotification(String message, boolean bool) {
        Log.d(TAG, "Creating notification");
        // Prepare intent which is triggered if the notification is selected
        Intent intent = new Intent(this, ComicListActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Add a sound to notification
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // Build notification
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

