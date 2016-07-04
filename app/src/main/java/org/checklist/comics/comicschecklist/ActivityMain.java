package org.checklist.comics.comicschecklist;

import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
                                                                    ComicsChecklistDialogFragment.ComicsChecklistDialogListener,
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
            selectItem(Constants.Editors.getCode(Constants.Editors.FAVORITE),
                       Constants.Editors.getTitle(Constants.Editors.FAVORITE));
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
        Cursor cursor = ComicDatabaseManager.query(this, ComicContentProvider.CONTENT_URI, null, ComicDatabase.COMICS_NAME_KEY + " LIKE ?",
                new String[] {"%" + query + "%"}, null);

        if (cursor != null && cursor.getCount() == 0) {
            Log.d(TAG, "doMySearch - no data found!");
            // There are no results
            cursor.close();
            Toast.makeText(this, getResources().getText(R.string.search_no_result), Toast.LENGTH_SHORT).show();
        } else if (cursor != null && cursor.getCount() > 0) {
            Log.d(TAG, "doMySearch - found data: " + cursor.getCount());
            // There are multiple results which fit the given query, so show this on dialog
            cursor.close();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(Constants.PREF_SEARCH_QUERY, query);
            editor.apply();
            // Open a dialog with a list of results
            DialogFragment listDialog = ComicsChecklistDialogFragment.newInstance(Constants.DIALOG_RESULT_LIST);
            listDialog.show(getFragmentManager(), "ComicsChecklistDialogFragment");
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
        Constants.Editors editor = Constants.Editors.getEditorFromName(rawEditor);
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

    private void selectItem(int position, String title) {
        Log.d(TAG, "selectItem " + position);
        if (mNavigationView != null) {
            if (mDrawerLayout != null) {
                if (position < 8) {
                    // Update selected item title, then close the drawer
                    setTitle(title);
                    mDrawerLayout.closeDrawer(mNavigationView);
                }
            }
        }

        if (position <= 7) {
            // Update the main content by replacing fragments
            mListFragment = FragmentList.newInstance(Constants.Editors.getEditor(position));
            getSupportFragmentManager().beginTransaction().replace(R.id.container, mListFragment).commit();
        } else {
            switch (position) {
                case 8:
                    // Open settings
                    Intent launchPreferencesIntent = new Intent().setClass(this, SettingsActivity.class);
                    startActivity(launchPreferencesIntent);
                    break;
                case 9:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/115824315702252939905/posts")));
                    break;
                case 10:
                    // Open help dialog
                    DialogFragment helpDialog = ComicsChecklistDialogFragment.newInstance(Constants.DIALOG_GUIDE);
                    helpDialog.show(getFragmentManager(), "ComicsChecklistDialogFragment");
                    break;
                case 11:
                    // Open info dialog
                    DialogFragment infoDialog = ComicsChecklistDialogFragment.newInstance(Constants.DIALOG_INFO);
                    infoDialog.show(getFragmentManager(), "ComicsChecklistDialogFragment");
                    break;
            }
        }
    }

    /* ****************************************************************************************
     * NAVIGATION VIEW CALLBACK
     ******************************************************************************************/

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected - start " + item.getTitle());
        int position = 0;
        switch (item.getItemId()) {
            case R.id.list_favorite:
                position = 0;
                break;
            case R.id.list_cart:
                position = 1;
                break;
            case R.id.list_marvel:
                position = 2;
                break;
            case R.id.list_panini:
                position = 3;
                break;
            case R.id.list_planet:
                position = 4;
                break;
            case R.id.list_star:
                position = 5;
                break;
            case R.id.list_bonelli:
                position = 6;
                break;
            case R.id.list_rw:
                position = 7;
                break;
            case R.id.action_settings:
                position = 8;
                break;
            case R.id.action_social:
                position = 9;
                break;
            case R.id.action_help:
                position = 10;
                break;
            case R.id.action_info:
                position = 11;
                break;
        }
        selectItem(position, item.getTitle().toString());
        boolean result = position <= 7;
        Log.v(TAG, "onNavigationItemSelected - end - result " + result);
        return result;
    }

    /* ****************************************************************************************
     * DIALOG CALLBACK
     ******************************************************************************************/

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int dialogId) {
        switch (dialogId) {
            case Constants.DIALOG_RATE:
                dialog.dismiss();
                this.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + this.getPackageName())));
                break;
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog, int dialogId) {
        switch (dialogId) {
            case Constants.DIALOG_GUIDE:
            case Constants.DIALOG_INFO:
            case Constants.DIALOG_RESULT_LIST:
                dialog.dismiss();
                break;
            case Constants.DIALOG_RATE:
                dialog.dismiss();
                SharedPreferences prefs = this.getSharedPreferences (Constants.PREF_APP_RATER, 0);
                final SharedPreferences.Editor editorPref = prefs.edit();
                if (editorPref != null) {
                    editorPref.putBoolean(Constants.PREF_USER_DONT_RATE, true);
                    editorPref.apply();
                }
                break;
        }
    }

    @Override
    public void onDialogNeutralClick(DialogFragment dialog, int dialogId) {
        switch (dialogId) {
            case Constants.DIALOG_RATE:
                dialog.dismiss();
                break;
        }
    }

    @Override
    public void onDialogListItemClick(DialogFragment dialog, int dialogId, long id) {
        switch (dialogId) {
            case Constants.DIALOG_RESULT_LIST:
                dialog.dismiss();
                launchDetailView(id);
                break;
        }
    }
}
