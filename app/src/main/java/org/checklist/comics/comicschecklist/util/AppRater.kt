package org.checklist.comics.comicschecklist.util

import org.checklist.comics.comicschecklist.R

import android.content.Context
import android.content.Intent
import android.net.Uri

import org.checklist.comics.comicschecklist.extensions.Loggable
import org.checklist.comics.comicschecklist.extensions.lazyLogger

import org.jetbrains.anko.alert
import org.jetbrains.anko.verbose

/**
 * Class which launch a dialog asking for rate the app.
 */
object AppRater: Loggable {

    private const val PREF_USER_NOT_RATING = "not_showing_again"
    private const val PREF_APP_RATER = "app_rater"
    private const val PREF_LAUNCH_COUNT = "launch_count"
    private const val PREF_DATE_FIRST_LAUNCH = "date_first_launch"
    private const val DAYS_UNTIL_PROMPT = 7
    private const val LAUNCHES_UNTIL_PROMPT = 7

    private val LOG by lazyLogger()

    /**
     * Method which is launched every time the app is opened.
     */
    fun appLaunched(context: Context) {
        val prefs = context.getSharedPreferences(PREF_APP_RATER, 0)
        if (prefs.getBoolean(PREF_USER_NOT_RATING, false)) {
            return
        }

        val editor = prefs.edit()

        // Increment counter of how many times user launched the app
        val launchCount = prefs.getLong(PREF_LAUNCH_COUNT, 0) + 1
        editor.putLong(PREF_LAUNCH_COUNT, launchCount)

        // Take date of first launch
        var dateFirstLaunch: Long? = prefs.getLong(PREF_DATE_FIRST_LAUNCH, 0)
        if (dateFirstLaunch == 0L) {
            dateFirstLaunch = System.currentTimeMillis()
            editor.putLong(PREF_DATE_FIRST_LAUNCH, dateFirstLaunch)
        }

        // Wait at least 7 days before opening rate window
        if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= dateFirstLaunch!! + DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000) {

                // Show dialog
                context.alert {
                    titleResource = R.string.app_name
                    messageResource = R.string.dialog_rate_text

                    positiveButton(R.string.dialog_rate_button) { dialog -> dialog.dismiss()
                        context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + context.packageName)))
                    }

                    negativeButton(R.string.dialog_no_thanks_button) { dialog ->
                        LOG.verbose("onClick - User refused to rate app..")
                        val editorPref = context.getSharedPreferences(PREF_APP_RATER, 0).edit()
                        if (editorPref != null) {
                            editorPref.putBoolean(PREF_USER_NOT_RATING, true)
                            editorPref.apply()
                        }

                        dialog.dismiss()
                    }

                    neutralPressed(R.string.dialog_late_button) { dialog -> dialog.dismiss() }
                }.show()
            }
        }

        editor.apply()
    }
}
