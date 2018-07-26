package org.checklist.comics.comicschecklist.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.preference.PreferenceManager

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.database.AppDatabase
import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.ui.ActivityMain
import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.log.ParserLog
import org.checklist.comics.comicschecklist.parser.ParserBonelli
import org.checklist.comics.comicschecklist.parser.ParserPanini
import org.checklist.comics.comicschecklist.parser.ParserRW
import org.checklist.comics.comicschecklist.parser.ParserStar
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.notification.CCNotificationManager
import org.checklist.comics.comicschecklist.util.Constants
import org.checklist.comics.comicschecklist.widget.WidgetService

import org.joda.time.DateTime
import org.joda.time.Days

import java.util.ArrayList
import java.util.Arrays
import java.util.Collections
import java.util.HashMap
import java.util.HashSet

/**
 * Class used to download on background info about comics.
 */
class DownloadService : IntentService(TAG) {

    override fun onHandleIntent(intent: Intent?) {
        // Checking first if we are connected to the web
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting

        if (!isConnected) {
            publishResults(Constants.RESULT_NOT_CONNECTED, "noEditor")
            CCNotificationManager.createNotification(this, resources.getString(R.string.toast_no_connection), false)
            return
        }

        // Loading frequency and flag if user want to do manual search
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val syncPref = sharedPref.getString(Constants.PREF_SYNC_FREQUENCY, "3")
        val frequency = Integer.parseInt(syncPref)
        val manualSearch = intent!!.getBooleanExtra(Constants.MANUAL_SEARCH, false)

        CCLogger.d(TAG, "onHandleIntent - Checking if search is manual or automatic $frequency")

        // Use this code for screenshot
        //myParser.startParseFreeComics();

        if (frequency > -1 && !manualSearch) {
            CCLogger.i(TAG, "onHandleIntent - Automatic search launched")
            automaticSearch(sharedPref, frequency)
        } else {
            CCLogger.i(TAG, "onHandleIntent - Manual search needed")
            val editor = intent.getSerializableExtra(Constants.ARG_EDITOR) as Constants.Sections
            manualSearch(sharedPref, editor)
        }

        // Check if this is the fist creation of Room database
        val createDatabase = intent.getBooleanExtra(Constants.CREATE_DATABASE, false)
        if (createDatabase) {
            // Notify that the database was created and it's ready to be used
            val database = AppDatabase.getInstance(this.applicationContext)
            database.setDatabaseCreated()
        }

        deleteOldRows()
    }

    override fun onDestroy() {
        publishResults(Constants.RESULT_DESTROYED, "noEditor")
        // Notification is not needed (only if there isn't an error)
        CCNotificationManager.deleteNotification(this)
    }

    /**
     * Method which will launch automatic search of contents.
     * @param sharedPref the [SharedPreferences] where to load list of editors to search
     * @param frequency the time passed between last search
     */
    private fun automaticSearch(sharedPref: SharedPreferences, frequency: Int) {
        val notificationPref = sharedPref.getBoolean(Constants.PREF_SEARCH_NOTIFICATION, true)

        // Loading user editor preference
        val rawArray = resources.getStringArray(R.array.pref_basic_editors)
        var editorSet = sharedPref.getStringSet(Constants.PREF_AVAILABLE_EDITORS, null)

        if (editorSet == null) {
            editorSet = HashSet(Arrays.asList(*rawArray))
        }

        for ((currentSection, value) in mEditorMap) {
            CCLogger.d(TAG, "automaticSearch - Current editor $currentSection key of last scan $value")
            // Check if editor is desired by user and if last day of scan has passed limits
            if (calculateDayDifference(value) >= frequency && editorSet.contains(currentSection.code.toString())) {
                publishResults(Constants.RESULT_START, currentSection.title)
                searchComics(notificationPref, currentSection.title, currentSection)

                publishResults(Constants.RESULT_FINISHED, "noEditor")
                if (notificationPref) {
                    CCNotificationManager.deleteNotification(this)
                }
            }
        }
    }

    /**
     * Method which will launch automatic search of contents.
     * @param sharedPref the [SharedPreferences] where to load list of editors to search
     * @param editor the editor to search
     */
    private fun manualSearch(sharedPref: SharedPreferences, editor: Constants.Sections) {
        val notificationPref = sharedPref.getBoolean(Constants.PREF_SEARCH_NOTIFICATION, true)
        CCLogger.i(TAG, "manualSearch - Manual search for editor " + editor.toString())

        publishResults(Constants.RESULT_START, editor.title)
        searchComics(notificationPref, editor.title, editor)

        // Update last scan for editor on shared preference
        val today = System.currentTimeMillis()
        when (editor) {
            Constants.Sections.PANINI -> sharedPref.edit().putLong(Constants.PREF_PANINI_LAST_SCAN, today).apply()
            Constants.Sections.BONELLI -> sharedPref.edit().putLong(Constants.PREF_BONELLI_LAST_SCAN, today).apply()
            Constants.Sections.STAR -> sharedPref.edit().putLong(Constants.PREF_STAR_LAST_SCAN, today).apply()
            Constants.Sections.RW -> sharedPref.edit().putLong(Constants.PREF_RW_LAST_SCAN, today).apply()
            else -> CCLogger.w(TAG, "Can't search data for given section ${editor.title}")
        }

        publishResults(Constants.RESULT_FINISHED, "noEditor")
        if (notificationPref) {
            CCNotificationManager.deleteNotification(this)
            // Favorite data may have changed, update widget as well
            WidgetService.updateWidget(this)
        }
    }

    /**
     * Start searching for specified comics editor.
     *
     * @param notificationPref boolean indicating if notification are desired
     * @param editorTitle editor personal text
     * @param editor editor to search
     */
    private fun searchComics(notificationPref: Boolean, editorTitle: String, editor: Constants.Sections) {
        if (notificationPref) {
            CCNotificationManager.createNotification(this, editorTitle + resources.getString(R.string.search_started), true)
        }

        // Select editor
        var comicEntities: ArrayList<ComicEntity>? = null
        when (editor) {
            Constants.Sections.PANINI -> comicEntities = ParserPanini().initParser()
            Constants.Sections.STAR -> comicEntities = ParserStar().initParser()
            Constants.Sections.BONELLI -> comicEntities = ParserBonelli().initParser()
            Constants.Sections.RW -> comicEntities = ParserRW().initParser()
            else -> CCLogger.w(TAG, "Can't search data for given section ${editor.title}")
        }

        // See result
        if (comicEntities == null || comicEntities.size == 0) {
            publishResults(Constants.RESULT_CANCELED, editorTitle)
            if (notificationPref) {
                CCNotificationManager.updateNotification(this, editorTitle + resources.getString(R.string.search_failed), false)
            }
        } else {
            // Inserting found comics into database
            (this.applicationContext as CCApp).repository.insertComics(comicEntities)

            publishResults(Constants.RESULT_EDITOR_FINISHED, editorTitle)
            if (notificationPref) {
                CCNotificationManager.updateNotification(this, editorTitle + resources.getString(R.string.search_editor_completed), true)
            }
        }

        ParserLog.printReport()
    }

    /**
     * This method will delete old rows on database.
     */
    private fun deleteOldRows() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val deletePref = sharedPref.getString(Constants.PREF_DELETE_FREQUENCY, "-1")
        val frequency = Integer.parseInt(deletePref)
        if (frequency > -1) {
            val dateTime = DateTime()
            val time = dateTime.minus(frequency.toLong()).millis
            AsyncTask.execute {
                val rowsDeleted = (this@DownloadService.applicationContext as CCApp).repository.deleteOldComics(time)
                CCLogger.d(TAG, "deleteOldRows - Entries deleted: $rowsDeleted with given frequency $frequency")
                if (rowsDeleted > 0) {
                    // Update widgets as well
                    WidgetService.updateWidget(this@DownloadService)
                }
            }
        }
    }

    /**
     * Method which send to [ActivityMain] the status of search.
     * @param result the result of search
     * @param editor the editor searched
     */
    private fun publishResults(result: Int, editor: String) {
        CCLogger.v(TAG, "publishResults - Result of search $result $editor")
        val intent = Intent(Constants.NOTIFICATION)
        intent.putExtra(Constants.NOTIFICATION_RESULT, result)
        intent.putExtra(Constants.NOTIFICATION_EDITOR, editor)
        sendBroadcast(intent)
    }

    /**
     * This method calculate the difference since last scan for given editor.
     * @param editorLastScan the editor target
     * @return the difference in long data
     */
    private fun calculateDayDifference(editorLastScan: String): Int {
        CCLogger.v(TAG, "calculateDayDifference - Editor last scan $editorLastScan - start")
        var result: Int // If result is 3, we need a refresh
        val today = DateTime()

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val dateStart = sp.getLong(editorLastScan, -1)
        val dateLastScan = DateTime(dateStart)

        CCLogger.d(TAG, "calculateDayDifference - (in milliseconds) today is $today target is $dateStart")
        // Calculate day difference
        result = Days.daysBetween(today, dateLastScan).days

        sp.edit().putLong(editorLastScan, today.millis).apply()

        // Set default value in case of error
        if (result < 0) {
            result = 3
        }

        CCLogger.v(TAG, "calculateDayDifference - Result is $result - end")

        return result
    }

    companion object {

        private val TAG = DownloadService::class.java.simpleName

        /* Init a static map of editor to search */
        private val mEditorMap: Map<Constants.Sections, String>

        init {
            val aMap = HashMap<Constants.Sections, String>()
            aMap[Constants.Sections.RW] = Constants.PREF_RW_LAST_SCAN
            aMap[Constants.Sections.PANINI] = Constants.PREF_PANINI_LAST_SCAN
            aMap[Constants.Sections.BONELLI] = Constants.PREF_BONELLI_LAST_SCAN
            aMap[Constants.Sections.STAR] = Constants.PREF_STAR_LAST_SCAN
            mEditorMap = Collections.unmodifiableMap<Constants.Sections, String>(aMap)
        }
    }
}
