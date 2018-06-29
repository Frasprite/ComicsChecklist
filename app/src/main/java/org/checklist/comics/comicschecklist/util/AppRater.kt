package org.checklist.comics.comicschecklist.util

import org.checklist.comics.comicschecklist.R

import android.content.Context
import android.content.Intent
import android.net.Uri

import org.jetbrains.anko.alert

/**
 * Class which launch a dialog asking for rate the app.
 */
object AppRater {

    /**
     * Method which is launched every time the app is opened.
     */
    fun appLaunched(context: Context) {
        val prefs = context.getSharedPreferences(Constants.PREF_APP_RATER, 0)
        if (prefs.getBoolean(Constants.PREF_USER_DONT_RATE, false)) {
            return
        }

        val editor = prefs.edit()

        // Increment counter of how many times user launched the app
        val launchCount = prefs.getLong(Constants.PREF_LAUNCH_COUNT, 0) + 1
        editor.putLong(Constants.PREF_LAUNCH_COUNT, launchCount)

        // Take date of first launch
        var dateFirstLaunch: Long? = prefs.getLong(Constants.PREF_DATE_FIRST_LAUNCH, 0)
        if (dateFirstLaunch == 0L) {
            dateFirstLaunch = System.currentTimeMillis()
            editor.putLong(Constants.PREF_DATE_FIRST_LAUNCH, dateFirstLaunch)
        }

        // Wait at least 7 days before opening rate window
        if (launchCount >= Constants.LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= dateFirstLaunch!! + Constants.DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000) {

                // Show dialog
                context.alert {
                    titleResource = R.string.app_name
                    messageResource = R.string.dialog_rate_text

                    positiveButton(R.string.dialog_rate_button) { dialog -> dialog.dismiss()
                        context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + context.packageName)))
                    }

                    negativeButton(R.string.dialog_no_thanks_button) { dialog ->
                        val editorPref = context.getSharedPreferences(Constants.PREF_APP_RATER, 0).edit()
                        if (editorPref != null) {
                            editorPref.putBoolean(Constants.PREF_USER_DONT_RATE, true)
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
