package org.checklist.comics.comicschecklist.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import org.checklist.comics.comicschecklist.ui.ActivityMain;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.log.CCLogger;

/**
 * Class used to create and manage notifications.
 */
public class CCNotificationManager {

    private static final String TAG = CCNotificationManager.class.getSimpleName();

    private static final int NOTIFICATION_ID = 7171;
    private static final String CHANNEL_ID = "cc_notification_channel";
    private static final String CHANNEL_NAME = "ComicsChecklist";

    /**
     * Private method which will create a {@link android.app.NotificationManager} if device is on Oreo and above.
     * @param context the {@link Context} to use for creating the channel
     */
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * Private method which will create a notification indicating progress of sending measurement.
     * @param context the {@link Context} to use for creating the notification
     * @param text the message to use on notification
     * @param showProgress whether the notification should show a progress bar
     */
    public static void createNotification(Context context, String text, boolean showProgress) {
        CCLogger.d(TAG, "createNotification - Creating notification for favorite alert");

        // First, create channel
        createNotificationChannel(context);

        // Prepare intent which is triggered if the notification is selected
        Intent intent = new Intent(context, ActivityMain.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Add a sound to notification
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Then create notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(text)
                .setSound(sound)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setProgress(0, 0, showProgress);

        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Private method which will update the notification according to parameter.
     * @param context the {@link Context} to use for updating the notification
     * @param text the message to use on notification
     * @param showProgress whether the notification should show a progress bar
     */
    public static void updateNotification(Context context, String text, boolean showProgress) {
        CCLogger.d(TAG, "updateNotification - Creating notification for favorite alert");

        // Prepare intent which is triggered if the notification is selected
        Intent intent = new Intent(context, ActivityMain.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Then create notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(text)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setProgress(0, 0, showProgress);

        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Method used to delete the notification when search is done.
     * the {@link Context} to use for deleting the notification
     */
    public static void deleteNotification(Context context) {
        // Then create notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(NOTIFICATION_ID);
    }
}
