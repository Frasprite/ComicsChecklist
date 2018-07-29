package org.checklist.comics.comicschecklist.ui

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View

import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.service.DownloadService
import org.checklist.comics.comicschecklist.util.AppRater
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.service.Message
import org.checklist.comics.comicschecklist.util.Constants
import org.checklist.comics.comicschecklist.service.ServiceEvents
import org.jetbrains.anko.alert

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast

/**
 * An activity representing a list of Comics. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [ActivityDetail] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 *
 *
 * The activity makes heavy use of fragments. The list of items is a
 * [FragmentRecycler] and the item details
 * (if present) is a [FragmentDetail].
 *
 *
 * to listen for item selections.
 */
class ActivityMain : AppCompatActivity(), SearchView.OnQueryTextListener, NavigationView.OnNavigationItemSelectedListener {

    // Whether or not the activity is in two-pane mode, i.e. running on a tablet device
    private var mTwoPane: Boolean = false

    private var mFragmentRecycler: FragmentRecycler? = null

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private lateinit var mNavigationView: NavigationView

    private lateinit var mDrawerTitle: CharSequence

    private var mUserLearnedDrawer: Boolean = false
    private var mFromSavedInstanceState: Boolean = false

    private var mSection: Constants.Sections = Constants.Sections.FAVORITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CCLogger.d(TAG, "onCreate - start")

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false)

        setContentView(R.layout.activity_comic_list)

        // Set options to action bar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        // The detail container view will be present only in the
        // large-screen layouts (res/values-large-land and
        // res/values-sw600dp-land). If this view is present, then the
        // activity should be in two-pane mode.
        mTwoPane = findViewById<View>(R.id.comicDetailContainer) != null
        CCLogger.d(TAG, if (mTwoPane) "Application is running on singlePane" else "Application is running on twoPane")

        mDrawerTitle = mSection.title
        mDrawerLayout = findViewById(R.id.drawerLayout)

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = object : ActionBarDrawerToggle(
                this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            override fun onDrawerClosed(view: View) {
                title = mSection.title
                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                title = mDrawerTitle

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply()
                }

                invalidateOptionsMenu() // creates call to onPrepareOptionsMenu()
            }
        }
        mDrawerLayout.addDrawerListener(mDrawerToggle)

        mNavigationView = findViewById(R.id.navigationView)
        mNavigationView.setNavigationItemSelectedListener(this)

        // Attach listener to navigation bottom
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.searchStore ->
                    // This method can be called from shortcut (Android 7.1 and above)
                    searchStore()
                R.id.addComic ->
                    // This method can be called from shortcut (Android 7.1 and above)
                    addComic()
                R.id.refresh -> if (mFragmentRecycler != null) {
                    initiateRefresh(mSection)
                }
            }
            true
        }

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mNavigationView)
        }

        mDrawerLayout.addDrawerListener(mDrawerToggle)

        if (savedInstanceState == null) {
            selectItem(Constants.Sections.FAVORITE)
        } else {
            mFromSavedInstanceState = true
        }

        val intent = Intent(this, DownloadService::class.java)
        startService(intent)

        // Listen for MessageEvents only
        ServiceEvents.listen(Message::class.java).subscribe {
            inspectResultCode(it.result, it.editor)
        }

        // Launch AppRater
        AppRater.appLaunched(this)

        // Handle search intent
        if (getIntent() != null) {
            handleIntent(getIntent())
        }

        CCLogger.v(TAG, "onCreate - end")
    }

    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            CCLogger.d(TAG, "onBackPressed - closing NavigationView")
            mDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            CCLogger.d(TAG, "onBackPressed - closing app")
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent) {
        CCLogger.v(TAG, "onNewIntent - $intent")
        handleIntent(intent)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        doMySearch(newText)
        return true
    }

    /**
     * Method used to handle the intent received from search or from other custom action.
     * @param intent the intent passed through search
     */
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEARCH -> {
                // Handles a search query
                val query = intent.getStringExtra(SearchManager.QUERY)
                CCLogger.d(TAG, "handleIntent - Handling a search query $query")
                doMySearch(query)
            }
            Constants.ACTION_COMIC_WIDGET -> {
                val comicId = intent.getIntExtra(Constants.COMIC_ID_FROM_WIDGET, 0)
                CCLogger.d(TAG, "handleIntent - Launching detail for comic ID $comicId")
                loadComicWithID(comicId)
            }
            Constants.ACTION_WIDGET_ADD -> {
                // Open add comic activity
                CCLogger.d(TAG, "handleIntent - Starting add activity (from widget)")
                addComic()
            }
            Constants.ACTION_ADD_COMIC -> {
                // Intent is coming from shortcut, so open relative action
                CCLogger.d(TAG, "handleIntent - Starting add activity (from shortcut)")
                addComic()
            }
            Constants.ACTION_SEARCH_STORE -> {
                // Intent is coming from shortcut, so search nearby store
                CCLogger.d(TAG, "handleIntent - Searching store")
                searchStore()
            }
        }
    }

    /**
     * Method which do a sync search on database for a comic with given Id.
     * @param comicId the comic ID
     */
    private fun loadComicWithID(comicId: Int) {
        doAsync {
            val comicEntity = (application as CCApp).repository.loadComicSync(comicId)
            CCLogger.d(TAG, "loadComicWithID - Comic : $comicEntity")
            launchDetailView(comicEntity)
        }
    }

    /**
     * Method used to call new UI for add comic note.
     */
    fun addComic() {
        // Open add comic activity
        val addComicIntent = Intent(this@ActivityMain, ActivityAddComic::class.java)
        startActivity(addComicIntent)
    }

    /**
     * Method used to launch Google maps for searching nearby store.
     */
    fun searchStore() {
        // Search for comics shops nearby
        val gmmIntentUri = Uri.parse("geo:0,0?q=fumetteria")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    /**
     * Method used to do a search and show founded data in a dialog.
     * @param query the text to search on database
     */
    private fun doMySearch(query: String) {
        CCLogger.d(TAG, "doMySearch - start searching $query")

        // Filtering data based on editor and newText
        if (mFragmentRecycler != null) {
            mFragmentRecycler!!.updateList(query)
        }
    }

    /**
     * @see android.app.Activity.onStart
     */
    override fun onStart() {
        super.onStart()
        CCLogger.v(TAG, "onStart")
        initVersionInfo()

        if (mTwoPane) {
            // In two-pane mode, list items should be given the 'activated' state when touched
            if (mFragmentRecycler != null) {
                // TODO highlight item
                CCLogger.d(TAG, "onStart - two-pane mode, activate item on click")
                //mFragmentRecycler.setActivateOnItemClick(true);
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CCLogger.v(TAG, "onResume")
        // Populate navigation view menu
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val rawArray = resources.getStringArray(R.array.pref_basic_editors)
        var editorSet = sp.getStringSet(Constants.PREF_AVAILABLE_EDITORS, null)

        if (editorSet == null) {
            editorSet = rawArray.toList().toHashSet()
        }

        // Populate menu
        mNavigationView.menu.findItem(R.id.list_panini).isVisible = editorSet.contains(Constants.Sections.PANINI.code.toString())
        mNavigationView.menu.findItem(R.id.list_star).isVisible = editorSet.contains(Constants.Sections.STAR.code.toString())
        mNavigationView.menu.findItem(R.id.list_bonelli).isVisible = editorSet.contains(Constants.Sections.BONELLI.code.toString())
        mNavigationView.menu.findItem(R.id.list_rw).isVisible = editorSet.contains(Constants.Sections.RW.code.toString())
    }

    override fun onPause() {
        super.onPause()
        CCLogger.v(TAG, "onPause")
        // Stop animation
        if (mFragmentRecycler != null) {
            if (mFragmentRecycler!!.isRefreshing) {
                mFragmentRecycler!!.isRefreshing = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main_top, menu)
        if (!mDrawerLayout.isDrawerOpen(mNavigationView)) {
            // Get the SearchView and set the searchable configuration
            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            val searchItem = menu.findItem(R.id.search)
            if (searchItem != null) {
                val searchView = searchItem.actionView as SearchView
                // Assumes current activity is the searchable activity
                searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
                searchView.setIconifiedByDefault(false) // Do not iconify the widget; expand it by default
                searchView.setOnQueryTextListener(this)
            }

            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // If the nav drawer is open, hide action items related to the content view
        val drawerOpen = mDrawerLayout.isDrawerOpen(mNavigationView)
        // Hide detail buttons
        menu.findItem(R.id.search).isVisible = !drawerOpen

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig)
    }

    /**
     * Method used to init some info on side menu.
     */
    private fun initVersionInfo() {
        val headerLayout = mNavigationView.getHeaderView(0)
        var version = ""
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            version = "v" + pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            CCLogger.w(TAG, "Can't find app version!", e)
        }

        headerLayout.versionTextView.text = version
    }

    /**
     * Method used to inspect messages from [DownloadService].
     * @param result the result indicating download status
     * @param currentEditor the editor searched
     */
    private fun inspectResultCode(result: Int, currentEditor: String?) {
        var shouldSetRefresh = false
        when (result) {
            Constants.RESULT_START -> {
                toast(currentEditor!! + getString(R.string.search_started))
                shouldSetRefresh = true
            }
            Constants.RESULT_FINISHED -> toast(resources.getString(R.string.search_completed))
            Constants.RESULT_EDITOR_FINISHED -> toast(currentEditor!! + resources.getString(R.string.search_editor_completed))
            Constants.RESULT_CANCELED -> toast(currentEditor!! + resources.getString(R.string.search_failed))
            Constants.RESULT_NOT_CONNECTED -> toast(resources.getString(R.string.toast_no_connection))
            Constants.RESULT_DESTROYED -> CCLogger.i(TAG, "Service destroyed")
        }

        // Set search animation on UI
        if (mFragmentRecycler != null) {
            if (!shouldSetRefresh) {
                mFragmentRecycler!!.isRefreshing = false
            }
        }
    }

    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     * @param mEditor the editor picked by user
     */
    fun initiateRefresh(mEditor: Constants.Sections) {
        CCLogger.i(TAG, "initiateRefresh - start for editor $mEditor")

        if (mEditor == Constants.Sections.FAVORITE || mEditor == Constants.Sections.CART) {
            alert {
                titleResource = R.string.dialog_pick_editor_title

                items(resources.getStringArray(R.array.pref_available_editors).toList()) { dialog, which ->
                    // The 'which' argument contains the index position of the selected item
                    CCLogger.v(TAG, "onClick - Selected position $which")
                    var pickedEditor: Constants.Sections? = null
                    when (which) {
                        0 -> pickedEditor = Constants.Sections.PANINI
                        1 -> pickedEditor = Constants.Sections.STAR
                        2 -> pickedEditor = Constants.Sections.BONELLI
                        3 -> pickedEditor = Constants.Sections.RW
                    }

                    if (pickedEditor != null) {
                        startRefresh(pickedEditor)
                        dialog.dismiss()
                    }
                }

                negativeButton(R.string.dialog_undo_button) { dialog ->
                    dialog.dismiss()
                }
            }.show()
        } else {
            startRefresh(mEditor)
        }
    }

    /**
     * Method used to start refresh.
     * @param editor the editor picked by user
     */
    private fun startRefresh(editor: Constants.Sections) {
        // Execute the background task, used on DownloadService to load the data
        val intent = Intent(this, DownloadService::class.java)
        intent.putExtra(Constants.ARG_EDITOR, editor)
        intent.putExtra(Constants.MANUAL_SEARCH, true)
        startService(intent)

        // Update refresh spinner
        if (mFragmentRecycler != null && mFragmentRecycler!!.isRefreshing) {
            mFragmentRecycler!!.isRefreshing = false
        }
    }

    /**
     * This method launch detail view in a Fragment or on a new Activity.
     * @param comic the comic to show on details UI
     */
    fun launchDetailView(comic: ComicEntity) {
        CCLogger.d(TAG, "launchDetailView - Comic ID is ${comic.id} editor is ${comic.editor}")
        when (Constants.Sections.fromName(comic.editor)) {
            Constants.Sections.CART -> {
                // Show note
                val addComicIntent = Intent(this@ActivityMain, ActivityAddComic::class.java)
                addComicIntent.putExtra(Constants.ARG_COMIC_ID, comic.id)
                startActivity(addComicIntent)
            }
            else ->
                // Show normal comic
                if (mTwoPane) {
                    CCLogger.d(TAG, "launchDetailView - Launching detail view in TWO PANE mode")
                    // In two-pane mode, show the detail view in this activity by adding
                    // or replacing the detail fragment using a fragment transaction
                    val arguments = Bundle()
                    arguments.putInt(Constants.ARG_COMIC_ID, comic.id)
                    val mDetailFragment = FragmentDetail()
                    mDetailFragment.arguments = arguments
                    supportFragmentManager.beginTransaction().replace(R.id.comicDetailContainer, mDetailFragment).commit()
                } else {
                    CCLogger.d(TAG, "launchDetailView - Launching detail view in SINGLE PANE mode")
                    // In single-pane mode, simply start the detail activity
                    // for the selected item ID
                    val detailIntent = Intent(this, ActivityDetail::class.java)
                    detailIntent.putExtra(Constants.ARG_COMIC_ID, comic.id)

                    val options = ActivityOptions.makeCustomAnimation(
                            this,
                            R.anim.turn_left,
                            R.anim.turn_right)
                    startActivity(detailIntent, options.toBundle())
                }
        }
    }

    @SuppressLint("InflateParams")
    private fun selectItem(section: Constants.Sections) {
        CCLogger.d(TAG, "selectItem - $section")

        mSection = section
        if (section == Constants.Sections.FAVORITE || section == Constants.Sections.CART ||
                section == Constants.Sections.PANINI || section == Constants.Sections.BONELLI ||
                section == Constants.Sections.RW || section == Constants.Sections.STAR) {
            // Update selected item title, then close the drawer
            title = section.title
            mDrawerTitle = section.title
            mDrawerLayout.closeDrawer(mNavigationView)
        }

        when (mSection) {
            Constants.Sections.FAVORITE, Constants.Sections.CART, Constants.Sections.PANINI, Constants.Sections.BONELLI, Constants.Sections.RW, Constants.Sections.STAR -> {
                // Update the main content by replacing fragments
                mFragmentRecycler = FragmentRecycler.newInstance(section)
                supportFragmentManager.beginTransaction().replace(R.id.container, mFragmentRecycler).commit()
            }
            Constants.Sections.SETTINGS -> {
                // Open settings
                val launchPreferencesIntent = Intent().setClass(this, ActivitySettings::class.java)
                startActivity(launchPreferencesIntent)
            }
            Constants.Sections.GOOGLE -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/115824315702252939905/posts")))
            Constants.Sections.GUIDA -> {
                // Open help UI
                val launchGuideIntent = Intent().setClass(this, ActivityGuide::class.java)
                startActivity(launchGuideIntent)
            }
            Constants.Sections.INFO -> {
                // Open info dialog with custom view
                alert {
                    negativeButton(R.string.dialog_confirm_button) {
                        dialog -> dialog.dismiss()
                    }

                    customView = layoutInflater.inflate(R.layout.dialog_info, null)
                }.show()
            }
        }
    }

    /* ****************************************************************************************
     * NAVIGATION VIEW CALLBACK
     ******************************************************************************************/

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        CCLogger.d(TAG, "onNavigationItemSelected - start " + item.title)

        when (item.itemId) {
            R.id.list_favorite -> {
                selectItem(Constants.Sections.FAVORITE)
                return true
            }
            R.id.list_cart -> {
                selectItem(Constants.Sections.CART)
                return true
            }
            R.id.list_panini -> {
                selectItem(Constants.Sections.PANINI)
                return true
            }
            R.id.list_star -> {
                selectItem(Constants.Sections.STAR)
                return true
            }
            R.id.list_bonelli -> {
                selectItem(Constants.Sections.BONELLI)
                return true
            }
            R.id.list_rw -> {
                selectItem(Constants.Sections.RW)
                return true
            }
            R.id.action_settings -> {
                selectItem(Constants.Sections.SETTINGS)
                return false
            }
            R.id.action_social -> {
                selectItem(Constants.Sections.GOOGLE)
                return false
            }
            R.id.action_help -> {
                selectItem(Constants.Sections.GUIDA)
                return false
            }
            R.id.action_info -> {
                selectItem(Constants.Sections.INFO)
                return false
            }
            else -> {
                selectItem(Constants.Sections.FAVORITE)
                return true
            }
        }
    }

    companion object {

        private val TAG = ActivityMain::class.java.simpleName

        /*
         * Flag used to show the drawer on launch until the user manually
         * expands it. This shared preference tracks this.
         */
        private const val PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned"
    }
}
