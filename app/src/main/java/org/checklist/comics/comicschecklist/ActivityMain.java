package org.checklist.comics.comicschecklist;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.service.DownloadService;
import org.checklist.comics.comicschecklist.util.AppRater;
import org.checklist.comics.comicschecklist.util.Constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * An activity representing a list of Comics. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ActivityDetail} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link FragmentList} and the item details
 * (if present) is a {@link FragmentDetail}.
 * <p>
 * to listen for item selections.
 */
public class ActivityMain extends AppCompatActivity implements FragmentList.Callbacks,
                                                                NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = ActivityMain.class.getSimpleName();

    // Whether or not the activity is in two-pane mode, i.e. running on a tablet device
    private boolean mTwoPane;

    private FragmentList mListFragment;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private ActionBar mActionBar;

    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;

    final private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(Constants.NOTIFICATION_RESULT);
                String mCurrentEditor = bundle.getString(Constants.NOTIFICATION_EDITOR);
                if (resultCode == Constants.RESULT_START) {
                    Toast.makeText(getApplicationContext(), mCurrentEditor + getString(R.string.search_started), Toast.LENGTH_SHORT).show();
                } else if (resultCode == Constants.RESULT_FINISHED) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.search_completed), Toast.LENGTH_SHORT).show();
                } else if (resultCode == Constants.RESULT_EDITOR_FINISHED) {
                    if (mListFragment != null) {
                        mListFragment.setRefreshing(false);
                    }
                    Toast.makeText(getApplicationContext(), mCurrentEditor + getResources().getString(R.string.search_editor_completed), Toast.LENGTH_SHORT).show();
                } else if (resultCode == Constants.RESULT_CANCELED) {
                    if (mListFragment != null) {
                        mListFragment.setRefreshing(false);
                    }
                    Toast.makeText(getApplicationContext(), mCurrentEditor + getResources().getString(R.string.search_failed), Toast.LENGTH_LONG).show();
                } else if (resultCode == Constants.RESULT_NOT_CONNECTED) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_no_connection), Toast.LENGTH_LONG).show();
                    if (mListFragment != null) {
                        mListFragment.setRefreshing(false);
                    }
                } else if (resultCode == Constants.RESULT_DESTROYED) {
                    Log.i(TAG, "Service destroyed");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate - start");
        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mUserLearnedDrawer = sp.getBoolean(Constants.PREF_USER_LEARNED_DRAWER, false);

        setContentView(R.layout.activity_comic_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open add comic activity
                Intent addComicIntent = new Intent(ActivityMain.this, ActivityAddComic.class);
                startActivity(addComicIntent);
            }
        });

        // The detail container view will be present only in the
        // large-screen layouts (res/values-large-land and
        // res/values-sw600dp-land). If this view is present, then the
        // activity should be in two-pane mode.
        mTwoPane = findViewById(R.id.comic_detail_container) != null;
        Log.d(TAG, mTwoPane ? "Application is running on singlePane" : "Application is running on twoPane");

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Set action bar
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                if (mActionBar != null) {
                    mActionBar.setTitle(mTitle);
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                if (mActionBar != null) {
                    mActionBar.setTitle(mDrawerTitle);
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ActivityMain.this);
                    sp.edit().putBoolean(Constants.PREF_USER_LEARNED_DRAWER, true).apply();
                }

                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_drawer);
        mNavigationView.setNavigationItemSelectedListener(this);
        View headerLayout = mNavigationView.getHeaderView(0);
        TextView versionTextView = (TextView) headerLayout.findViewById(R.id.versionTextView);
        PackageInfo pInfo;
        String version = "";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Can't find app version!", e);
        }
        versionTextView.setText(version);

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mNavigationView);
        }

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(Constants.Sections.FAVORITE);
        } else {
            mFromSavedInstanceState = true;
        }

        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);

        // Launch AppRater
        AppRater.app_launched(this);

        // Handle search intent
        if (getIntent() != null) {
            handleIntent(getIntent());
        }
        Log.v(TAG, "onCreate - end");
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            Log.d(TAG, "onBackPressed - closing NavigationView");
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Log.d(TAG, "onBackPressed - closing app");
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(TAG, "onNewIntent - " + intent);
        handleIntent(intent);
    }

    /**
     * Method used to handle the intent received from search.
     * @param intent the intent passed through search
     */
    private void handleIntent(Intent intent) {
        // Special processing of the incoming intent only occurs if the if the action specified
        // by the intent is ACTION_SEARCH.
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "handleIntent - handling a search query " + query);
            doMySearch(query);
        } else if (intent.getAction() != null && intent.getAction().equals(Constants.ACTION_COMIC_WIDGET)) {
            int comicId = intent.getIntExtra(Constants.COMIC_ID_FROM_WIDGET, 0);
            Log.d(TAG, "handleIntent - launching detail for comic ID " + comicId);
            launchDetailView(comicId);
        } else if (intent.getAction() != null && intent.getAction().equals(Constants.ACTION_WIDGET_ADD)) {
            // Open add comic activity
            Intent addComicIntent = new Intent(ActivityMain.this, ActivityAddComic.class);
            startActivity(addComicIntent);
        }
    }

    /**
     * Method used to do a search and show founded data in a dialog.
     * @param query the text to search on database
     */
    private void doMySearch(String query) {
        Log.d(TAG, "doMySearch - start searching " + query);

        // Load order for list
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String rawSortOrder = sharedPref.getString(Constants.PREF_LIST_ORDER, String.valueOf(Constants.Filters.getCode(Constants.Filters.DATE_ASC)));
        String sortOrder = Constants.Filters.getSortOrder(Integer.valueOf(rawSortOrder));
        Log.d(TAG, "doMySearch - ordering by " + sortOrder);

        // Query database
        final Cursor cursor = ComicDatabaseManager.query(this,
                                                   ComicContentProvider.CONTENT_URI,
                                                   new String[] {ComicDatabase.ID, ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_RELEASE_KEY},
                                                   ComicDatabase.COMICS_NAME_KEY + " LIKE ?",
                                                   new String[] {"%" + query + "%"},
                                                   sortOrder);

        if (cursor != null && cursor.getCount() == 0) {
            Log.d(TAG, "doMySearch - no data found!");
            // There are no results
            cursor.close();
            Toast.makeText(this, getResources().getText(R.string.search_no_result), Toast.LENGTH_SHORT).show();
        } else if (cursor != null && cursor.getCount() > 0) {
            Log.d(TAG, "doMySearch - found data: " + cursor.getCount());
            // There are multiple results which fit the given query, so show this on dialog

            // Open a dialog with a list of results
            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);

            // Set negative button
            builder.setNegativeButton(R.string.dialog_undo_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Simply dismiss dialog
                    dialog.dismiss();
                }
            });

            builder.setCursor(cursor, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // The 'which' argument contains the index position of the selected item);
                    if (cursor.moveToPosition(which)) {
                        long comicID = cursor.getLong(cursor.getColumnIndex(ComicDatabase.ID));
                        Log.d(TAG, "onClick (dialog) - ID " + comicID + " from position " + which);

                        if (comicID != 0) {
                            launchDetailView(comicID);
                        } else {
                            Toast.makeText(ActivityMain.this, getResources().getText(R.string.search_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }, ComicDatabase.COMICS_NAME_KEY);

            // Return dialog
            builder.setTitle(R.string.search_result)
                    .create()
                    .show();
        }
        Log.v(TAG, "doMySearch - end searching " + query);
    }

    /**
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
        if (mTwoPane) {
            // In two-pane mode, list items should be given the 'activated' state when touched
            if (mListFragment != null) {
                Log.d(TAG, "onStart - two-pane mode, activate item on click");
                mListFragment.setActivateOnItemClick(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        registerReceiver(receiver, new IntentFilter(Constants.NOTIFICATION));
        // Populate navigation view menu
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String[] rawArray = getResources().getStringArray(R.array.pref_basic_editors);
        Set<String> editorSet = sp.getStringSet(Constants.PREF_AVAILABLE_EDITORS, null);

        if (editorSet == null) {
            editorSet = new HashSet<>(Arrays.asList(rawArray));
        }

        // Populate menu
        if (editorSet.contains(String.valueOf(Constants.Sections.MARVEL.getCode()))) {
            mNavigationView.getMenu().findItem(R.id.list_marvel).setVisible(true);
        } else {
            mNavigationView.getMenu().findItem(R.id.list_marvel).setVisible(false);
        }
        if (editorSet.contains(String.valueOf(Constants.Sections.PANINI.getCode()))) {
            mNavigationView.getMenu().findItem(R.id.list_panini).setVisible(true);
        } else {
            mNavigationView.getMenu().findItem(R.id.list_panini).setVisible(false);
        }
        if (editorSet.contains(String.valueOf(Constants.Sections.PLANET.getCode()))) {
            mNavigationView.getMenu().findItem(R.id.list_planet).setVisible(true);
        } else {
            mNavigationView.getMenu().findItem(R.id.list_planet).setVisible(false);
        }
        if (editorSet.contains(String.valueOf(Constants.Sections.STAR.getCode()))) {
            mNavigationView.getMenu().findItem(R.id.list_star).setVisible(true);
        } else {
            mNavigationView.getMenu().findItem(R.id.list_star).setVisible(false);
        }
        if (editorSet.contains(String.valueOf(Constants.Sections.BONELLI.getCode()))) {
            mNavigationView.getMenu().findItem(R.id.list_bonelli).setVisible(true);
        } else {
            mNavigationView.getMenu().findItem(R.id.list_bonelli).setVisible(false);
        }
        if (editorSet.contains(String.valueOf(Constants.Sections.RW.getCode()))) {
            mNavigationView.getMenu().findItem(R.id.list_rw).setVisible(true);
        } else {
            mNavigationView.getMenu().findItem(R.id.list_rw).setVisible(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (!mDrawerLayout.isDrawerOpen(mNavigationView)) {
            // Get the SearchView and set the searchable configuration
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            MenuItem searchItem = menu.findItem(R.id.search);
            if (searchItem != null) {
                SearchView searchView = (SearchView) searchItem.getActionView();
                // Assumes current activity is the searchable activity
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
            }

            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mNavigationView);
        // Hide detail buttons
        menu.findItem(R.id.search).setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mActionBar != null) {
            Log.v(TAG, "setTitle - setting " + mTitle + " on mActionBar");
            mActionBar.setTitle(mTitle);
        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Callback method from {@link FragmentList.Callbacks}
     * indicating that the comic was selected.
     */
    @Override
    public void onItemSelected(long id) {
        Log.d(TAG, "Launching detail for comic with ID " + id);
        launchDetailView(id);
    }

    /**
     * This method launch detail view in a Fragment or on a new Activity.
     * @param id the comic id
     */
    private void launchDetailView(long id) {
        // Load only a part of selected id
        Uri uri = Uri.parse(ComicContentProvider.CONTENT_URI + "/" + id);
        String[] projection = {ComicDatabase.COMICS_EDITOR_KEY};
        Cursor mCursor = ComicDatabaseManager.query(this, uri, projection, null, null, null);
        mCursor.moveToFirst();
        String rawEditor = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_EDITOR_KEY));
        mCursor.close();
        Log.d(TAG, "launchDetailView - comic ID is " + id + " editor is " + rawEditor);
        Constants.Sections editor = Constants.Sections.getEditorFromName(rawEditor);
        switch (editor) {
            case CART:
                // Show note
                Intent addComicIntent = new Intent(ActivityMain.this, ActivityAddComic.class);
                addComicIntent.putExtra(Constants.ARG_COMIC_ID, id);
                startActivity(addComicIntent);
                break;
            default:
                // Show normal comic
                if (mTwoPane) {
                    Log.d(TAG, "Launching detail view in two pane mode");
                    // In two-pane mode, show the detail view in this activity by adding
                    // or replacing the detail fragment using a fragment transaction
                    Bundle arguments = new Bundle();
                    arguments.putLong(Constants.ARG_COMIC_ID, id);
                    FragmentDetail mDetailFragment = new FragmentDetail();
                    mDetailFragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction().replace(R.id.comic_detail_container, mDetailFragment).commit();
                } else {
                    Log.d(TAG, "Launching detail view in single pane mode");
                    // In single-pane mode, simply start the detail activity
                    // for the selected item ID
                    Intent detailIntent = new Intent(this, ActivityDetail.class);
                    detailIntent.putExtra(Constants.ARG_COMIC_ID, id);
                    startActivity(detailIntent);
                }
                break;
        }
    }

    @SuppressLint("InflateParams")
    private void selectItem(Constants.Sections section) {
        Log.d(TAG, "selectItem - " + section);
        if (mNavigationView != null) {
            if (mDrawerLayout != null) {
                if (section == Constants.Sections.FAVORITE || section == Constants.Sections.CART ||
                        section == Constants.Sections.MARVEL || section == Constants.Sections.PLANET ||
                        section == Constants.Sections.PANINI || section == Constants.Sections.BONELLI ||
                        section == Constants.Sections.RW || section == Constants.Sections.STAR) {
                    // Update selected item title, then close the drawer
                    setTitle(Constants.Sections.getTitle(section));
                    mDrawerLayout.closeDrawer(mNavigationView);
                }
            }
        }

        switch (section) {
            case FAVORITE:
            case CART:
            case MARVEL:
            case PLANET:
            case PANINI:
            case BONELLI:
            case RW:
            case STAR:
                // Update the main content by replacing fragments
                mListFragment = FragmentList.newInstance(section);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, mListFragment).commit();
                break;
            case SETTINGS:
                // Open settings
                Intent launchPreferencesIntent = new Intent().setClass(this, ActivitySettings.class);
                startActivity(launchPreferencesIntent);
                break;
            case GOOGLE:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/115824315702252939905/posts")));
                break;
            case GUIDA:
                // Open help dialog
                AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                helpBuilder.setNegativeButton(R.string.dialog_confirm_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        dialog.dismiss();
                    }
                });
                // Get the layout inflater
                LayoutInflater inflaterHelp = this.getLayoutInflater();
                // Inflate and set the layout for the dialog; pass null as the parent view because its going in the dialog layout
                helpBuilder.setView(inflaterHelp.inflate(R.layout.dialog_help, null));
                // Set title
                helpBuilder.setTitle(R.string.dialog_help_title);
                helpBuilder.show();
                break;
            case INFO:
                // Open info dialog
                AlertDialog.Builder infoBuilder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                infoBuilder.setNegativeButton(R.string.dialog_confirm_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        dialog.dismiss();
                    }
                });
                // Get the layout inflater
                LayoutInflater inflaterInfo = this.getLayoutInflater();
                // Inflate and set the layout for the dialog; pass null as the parent view because its going in the dialog layout
                infoBuilder.setView(inflaterInfo.inflate(R.layout.dialog_info, null));
                infoBuilder.show();
                break;
        }
    }

    /* ****************************************************************************************
     * NAVIGATION VIEW CALLBACK
     ******************************************************************************************/

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected - start " + item.getTitle());
        Constants.Sections section;
        boolean result;
        switch (item.getItemId()) {
            case R.id.list_favorite:
                section = Constants.Sections.FAVORITE;
                result = true;
                break;
            case R.id.list_cart:
                section = Constants.Sections.CART;
                result = true;
                break;
            case R.id.list_marvel:
                section = Constants.Sections.MARVEL;
                result = true;
                break;
            case R.id.list_panini:
                section = Constants.Sections.PANINI;
                result = true;
                break;
            case R.id.list_planet:
                section = Constants.Sections.PLANET;
                result = true;
                break;
            case R.id.list_star:
                section = Constants.Sections.STAR;
                result = true;
                break;
            case R.id.list_bonelli:
                section = Constants.Sections.BONELLI;
                result = true;
                break;
            case R.id.list_rw:
                section = Constants.Sections.RW;
                result = true;
                break;
            case R.id.action_settings:
                section = Constants.Sections.SETTINGS;
                result = false;
                break;
            case R.id.action_social:
                section = Constants.Sections.GOOGLE;
                result = false;
                break;
            case R.id.action_help:
                section = Constants.Sections.GUIDA;
                result = false;
                break;
            case R.id.action_info:
                section = Constants.Sections.INFO;
                result = false;
                break;
            default:
                section = Constants.Sections.FAVORITE;
                result = true;
                break;
        }
        selectItem(section);
        Log.v(TAG, "onNavigationItemSelected - end - section " + section + " result " + result);
        return result;
    }
}
