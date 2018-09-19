package org.checklist.comics.comicschecklist.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat

import org.checklist.comics.comicschecklist.ui.ActivityMain
import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.extensions.PreferenceHelper
import org.checklist.comics.comicschecklist.extensions.PreferenceHelper.get
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.util.Constants

/**
 * Class used to create and manage notifications.
 */
object CCNotificationManager {

    private val TAG = CCNotificationManager::class.java.simpleName

    private const val NOTIFICATION_ID = 7171
    private const val CHANNEL_ID = "cc_notification_channel"
    private const val CHANNEL_NAME = "ComicsChecklist"

    /**
     * Private method which will create a [android.app.NotificationManager] if device is on Oreo and above.
     * @param context the [Context] to use for creating the channel
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val importance = NotificationManager.IMPORTANCE_LOW
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    /**
     * Private method which will create a notification indicating progress of sending measurement.
     * @param context the [Context] to use for creating the notification
     * @param text the message to use on notification
     * @param showProgress whether the notification should show a progress bar
     */
    fun createNotification(context: Context, text: String, showProgress: Boolean) {
        if (shouldShowNotification(context)) {
            CCLogger.d(TAG, "createNotification - Creating notification:\ntext > $text\nprogress > $showProgress")

            // First, create channel
            createNotificationChannel(context)

            // Prepare intent which is triggered if the notification is selected
            val intent = Intent(context, ActivityMain::class.java)
            val pIntent = PendingIntent.getActivity(context, 0, intent, 0)

            // Add a sound to notification
            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // Then create notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(text)
                    .setSound(sound)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setProgress(0, 0, showProgress)

            notificationManager.notify(NOTIFICATION_ID, mBuilder.build())
        }
    }

    /**
     * Private method which will update the notification according to parameter.
     * @param context the [Context] to use for updating the notification
     * @param text the message to use on notification
     * @param showProgress whether the notification should show a progress bar
     */
    fun updateNotification(context: Context, text: String, showProgress: Boolean) {
        if (shouldShowNotification(context)) {
            CCLogger.d(TAG, "updateNotification - Creating notification for favorite alert")

            // Prepare intent which is triggered if the notification is selected
            val intent = Intent(context, ActivityMain::class.java)
            val pIntent = PendingIntent.getActivity(context, 0, intent, 0)

            // Then create notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_notification)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(text)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .setProgress(0, 0, showProgress)

            notificationManager.notify(NOTIFICATION_ID, mBuilder.build())
        }
    }

    /**
     * Method used to delete the notification when search is done.
     * the [Context] to use for deleting the notification
     */
    fun deleteNotification(context: Context) {
        if (shouldShowNotification(context)) {
            // Then create notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.cancel(NOTIFICATION_ID)
        }
    }

    private fun shouldShowNotification(context: Context): Boolean {
        val preferenceHelper = PreferenceHelper.defaultPrefs(context)
        val result = preferenceHelper[Constants.PREF_SEARCH_NOTIFICATION, true]
        return result!!
    }
}
