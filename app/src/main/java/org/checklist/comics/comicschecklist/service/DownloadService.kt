package org.checklist.comics.comicschecklist.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.database.AppDatabase
import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.ui.ActivityMain
import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.extensions.PreferenceHelper
import org.checklist.comics.comicschecklist.extensions.PreferenceHelper.get
import org.checklist.comics.comicschecklist.extensions.PreferenceHelper.set
import org.checklist.comics.comicschecklist.log.ParserLog
import org.checklist.comics.comicschecklist.parser.ParserBonelli
import org.checklist.comics.comicschecklist.parser.ParserPanini
import org.checklist.comics.comicschecklist.parser.ParserRW
import org.checklist.comics.comicschecklist.parser.ParserStar
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.notification.CCNotificationManager
import org.checklist.comics.comicschecklist.util.Constants
import org.checklist.comics.comicschecklist.widget.WidgetService

import org.jetbrains.anko.doAsync

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

        if (!checkConnection(this.applicationContext)) {
            publishResults(Constants.RESULT_NOT_CONNECTED, "noEditor")
            CCNotificationManager.createNotification(this, resources.getString(R.string.toast_no_connection), false)
            return
        }

        val manualSearch = intent!!.getBooleanExtra(Constants.MANUAL_SEARCH, false)

        CCLogger.d(TAG, "onHandleIntent - Should use manual search : $manualSearch")

        // Use this code for screenshot
        //myParser.startParseFreeComics();

        if (manualSearch && intent.hasExtra(Constants.ARG_EDITOR)) {
            CCLogger.i(TAG, "onHandleIntent - Manual search needed")
            val editor = intent.getSerializableExtra(Constants.ARG_EDITOR) as Constants.Sections
            manualSearch(editor)
        } else {
            CCLogger.i(TAG, "onHandleIntent - Automatic search launched")
            automaticSearch()
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
     *
     * This will search the frequency of updates and if it is >= -1, will launch search of new entries.
     */
    private fun automaticSearch() {
        val preferenceHelper = PreferenceHelper.defaultPrefs(this)

        val syncPref = preferenceHelper[Constants.PREF_SYNC_FREQUENCY, "3"]
        val frequency = Integer.parseInt(syncPref)

        if (frequency <= -1) return

        // Loading user editor preference
        val rawArray = resources.getStringArray(R.array.pref_basic_editors)
        var editorSet = preferenceHelper.getStringSet(Constants.PREF_AVAILABLE_EDITORS, emptySet())

        if (editorSet.isEmpty()) {
            editorSet = HashSet(Arrays.asList(*rawArray))
        }

        for ((currentSection, value) in mEditorMap) {
            CCLogger.d(TAG, "automaticSearch - Current editor $currentSection key of last scan $value")
            // Check if editor is desired by user and if last day of scan has passed limits
            if (calculateDayDifference(value) >= frequency && editorSet.contains(currentSection.code.toString())) {
                searchData(currentSection)
            }
        }

        // Update widget as well
        WidgetService.updateWidget(this)
    }

    /**
     * Method which will launch automatic search of contents.
     * @param editor the editor to search
     */
    private fun manualSearch(editor: Constants.Sections) {
        CCLogger.i(TAG, "manualSearch - Manual search for editor " + editor.toString())

        searchData(editor)

        // Update widget as well
        WidgetService.updateWidget(this)
    }

    /**
     * Start searching for specified comics editor.
     *
     * @param editor editor to search
     */
    private fun searchData(editor: Constants.Sections) {
        publishResults(Constants.RESULT_START, editor.title)

        CCNotificationManager.createNotification(this, editor.title + resources.getString(R.string.search_started), true)

        // Select editor
        val comicEntities: ArrayList<ComicEntity> = ArrayList()
        when (editor) {
            Constants.Sections.PANINI -> comicEntities.addAll(ParserPanini().initParser())
            Constants.Sections.STAR -> comicEntities.addAll(ParserStar().initParser())
            Constants.Sections.BONELLI -> comicEntities.addAll(ParserBonelli().initParser())
            Constants.Sections.RW -> comicEntities.addAll(ParserRW().initParser())
            else -> CCLogger.w(TAG, "Can't search data for given section ${editor.title}")
        }

        // Check if there are favorite and wishlist comics for current editor
        val checkedComicList = (this.applicationContext as CCApp).repository.loadCheckedComicsByEditorSync(editor.sectionName)
        CCLogger.v(TAG, "searchData - Found ${checkedComicList.size} which are added into wishlist / favorite")

        if (checkedComicList.size > 0) {
            filterComics(comicEntities, checkedComicList)
        }

        if (comicEntities.size > 0) {
            (this.applicationContext as CCApp).repository.insertComics(comicEntities)
        }

        publishResults(Constants.RESULT_EDITOR_FINISHED, editor.title)
        CCNotificationManager.updateNotification(this, editor.title + resources.getString(R.string.search_editor_completed), true)

        ParserLog.printReport()

        CCNotificationManager.deleteNotification(this)

        // Update last scan for editor on shared preference
        val preferenceHelper = PreferenceHelper.defaultPrefs(this)
        when (editor) {
            Constants.Sections.PANINI -> preferenceHelper[Constants.PREF_PANINI_LAST_SCAN] = System.currentTimeMillis()
            Constants.Sections.BONELLI -> preferenceHelper[Constants.PREF_BONELLI_LAST_SCAN] = System.currentTimeMillis()
            Constants.Sections.STAR -> preferenceHelper[Constants.PREF_STAR_LAST_SCAN] = System.currentTimeMillis()
            Constants.Sections.RW -> preferenceHelper[Constants.PREF_RW_LAST_SCAN] = System.currentTimeMillis()
            else -> CCLogger.w(TAG, "Can't search data for given section ${editor.title}")
        }
    }

    /**
     * Method used to filter favorite and wishlist listed comics (in order to avoid any override or unwanted remove).
     * @param comicEntities the list to filter
     * @param checkedComicList the list of comics to subtract from target list
     */
    private fun filterComics(comicEntities: ArrayList<ComicEntity>, checkedComicList: MutableList<ComicEntity>) {
        CCLogger.d(TAG, "filterComics - Total comics before filter = ${comicEntities.size}")

        for (checkedComic in checkedComicList) {
            for (comic in ArrayList(comicEntities)) {
                if (comic.name == checkedComic.name && comic.releaseDate == checkedComic.releaseDate) {
                    comicEntities.remove(comic)
                }
            }
        }

        CCLogger.d(TAG, "filterComics - Total comics after filter = ${comicEntities.size}")
    }

    /**
     * This method will delete old rows on database.
     */
    private fun deleteOldRows() {
        val preferenceHelper = PreferenceHelper.defaultPrefs(this)
        val deletePref = preferenceHelper[Constants.PREF_DELETE_FREQUENCY, "-1"]
        val frequency = Integer.parseInt(deletePref)
        if (frequency > -1) {
            val dateTime = DateTime()
            val time = dateTime.minus(frequency.toLong()).millis
            doAsync {
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
        ServiceEvents.publish(Message(result, editor))
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

        val preferenceHelper = PreferenceHelper.defaultPrefs(this)
        val dateStart = preferenceHelper[editorLastScan, -1L]
        val dateLastScan = DateTime(dateStart)

        CCLogger.d(TAG, "calculateDayDifference - (in milliseconds) today is $today target is $dateStart")
        // Calculate day difference
        result = Days.daysBetween(today, dateLastScan).days

        preferenceHelper[editorLastScan] = today.millis

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

        fun checkConnection(applicationContext: Context): Boolean {
            // Checking first if we are connected to the web
            val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting
        }
    }
}
