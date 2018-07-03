package org.checklist.comics.comicschecklist.ui

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Bundle
import android.preference.*
import android.view.MenuItem

import com.evernote.android.job.JobManager

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.notification.ComicReleaseSyncJob
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.widget.WidgetService
import org.checklist.comics.comicschecklist.util.Constants
import org.checklist.comics.comicschecklist.util.DateCreator

/**
 * A [android.preference.PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 *
 * See [
 * Android Design: Settings](http://developer.android.com/design/patterns/settings.html) for design guidelines and the [Settings
 * API Guide](http://developer.android.com/guide/topics/ui/settings.html) for more information on developing a Settings UI.
 */
class ActivitySettings : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                //startActivity(new Intent(getActivity(), ActivitySettings.class));
                onBackPressed()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return (NotificationPreferenceFragment::class.java.name == fragmentName
                || DataSyncPreferenceFragment::class.java.name == fragmentName
                || DataFilterPreferenceFragment::class.java.name == fragmentName)
    }

    /**
     * {@inheritDoc}
     */
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class NotificationPreferenceFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_notification)
            setHasOptionsMenu(true)
            findPreference(Constants.PREF_FAVORITE_NOTIFICATION).onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                val boolValue = newValue as Boolean
                if (boolValue) {
                    CCLogger.d(TAG, "onCreate - Activate notification for favorite")
                    ComicReleaseSyncJob.scheduleJob()
                } else {
                    CCLogger.d(TAG, "onCreate - Disable notification for favorite")
                    val mJobManager = JobManager.instance()
                    mJobManager.cancelAll()
                }
                true
            }
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                android.R.id.home -> {
                    activity.onBackPressed()
                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }
        }

        companion object {

            private val TAG = NotificationPreferenceFragment::class.java.simpleName
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class DataSyncPreferenceFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_data_sync)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(Constants.PREF_SYNC_FREQUENCY))
            bindPreferenceSummaryToValue(findPreference(Constants.PREF_DELETE_FREQUENCY))

            val preference = findPreference(Constants.PREF_LAST_SYNC)
            preference.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
                CCLogger.d(TAG, "onPreferenceClick - preference " + preference.key)
                launchLastSyncDialog(preference.context)
                true
            }
        }

        private fun launchLastSyncDialog(context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.dialog_last_sync_title)
                    .setMessage(composeLastSyncMessage(context))
                    .setPositiveButton(R.string.dialog_confirm_button) { dialog, which -> dialog.dismiss() }

            builder.create().show()
        }

        private fun composeLastSyncMessage(context: Context): String {
            var message: String
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            message = "Panini Comics : " + DateCreator.elaborateHumanDate(sp.getString(Constants.PREF_PANINI_LAST_SCAN, "01/01/2012")) + "\n"
            message = message + "RW Edizioni : " + DateCreator.elaborateHumanDate(sp.getString(Constants.PREF_RW_LAST_SCAN, "01/01/2012")) + "\n"
            message = message + "Bonelli : " + DateCreator.elaborateHumanDate(sp.getString(Constants.PREF_BONELLI_LAST_SCAN, "01/01/2012")) + "\n"
            message = message + "Star Comics : " + DateCreator.elaborateHumanDate(sp.getString(Constants.PREF_STAR_LAST_SCAN, "01/01/2012"))
            return message
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                android.R.id.home -> {
                    activity.onBackPressed()
                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class DataFilterPreferenceFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_data_filter)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            val multiSelectListPreference = findPreference(Constants.PREF_AVAILABLE_EDITORS) as MultiSelectListPreference
            multiSelectListPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, value ->
                CCLogger.d(TAG, "onPreferenceChange - preference " + preference.key + " value " + value.toString())
                true
            }

            val preference = findPreference(Constants.PREF_DELETE_CONTENT)
            preference.onPreferenceClickListener = Preference.OnPreferenceClickListener { preference ->
                CCLogger.d(TAG, "onPreferenceClick - preference " + preference.key)
                launchDeleteContentDialog(preference.context)
                true
            }
        }

        private fun launchDeleteContentDialog(context: Context) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.dialog_delete_content_title)
                    .setItems(R.array.pref_available_editors) { dialog, which ->
                        // The 'which' argument contains the index position of the selected item
                        val section: Constants.Sections? = when (which) {
                            0 ->
                                // Delete Panini comics content
                                Constants.Sections.PANINI
                            1 ->
                                // Delete Star Comics content
                                Constants.Sections.STAR
                            2 ->
                                // Delete SB content
                                Constants.Sections.BONELLI
                            3 ->
                                // Delete RW content
                                Constants.Sections.RW
                            else -> null
                        }
                        if (section != null) {
                            CCLogger.v(TAG, "Selected item on position $which - obtaining section $section")
                            deleteComics(context, section)
                        } else {
                            CCLogger.w(TAG, "No section found with index $which")
                            dialog.dismiss()
                        }
                    }
            builder.create().show()
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                android.R.id.home -> {
                    activity.onBackPressed()
                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }
        }

        private fun deleteComics(context: Context, section: Constants.Sections?) {
            AsyncTask.execute {
                val rowsDeleted = (context.applicationContext as CCApp).repository.deleteComics(section!!.getName())
                CCLogger.d(TAG, "deleteComics - Entries deleted: " + rowsDeleted + " with given section " + section.getName())
                if (rowsDeleted > 0) {
                    // Update widgets as well
                    WidgetService.updateWidget(context)
                }
            }
        }
    }

    companion object {

        private val TAG = ActivitySettings::class.java.simpleName

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()
            CCLogger.d(TAG, "onPreferenceChange - preference " + preference.key + " value " + stringValue)

            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val index = preference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            null)
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.
         *
         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, "")!!)
        }
    }
}
