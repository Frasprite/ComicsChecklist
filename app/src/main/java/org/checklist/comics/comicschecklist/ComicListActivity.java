package org.checklist.comics.comicschecklist;

import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.service.DownloadService;
import org.checklist.comics.comicschecklist.util.AppRater;
import org.checklist.comics.comicschecklist.util.Constants;

/**
 * An activity representing a list of Comics. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ComicDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ComicListFragment} and the item details
 * (if present) is a {@link ComicDetailFragment}.
 * <p>
 * to listen for item selections.
 */
public class ComicListActivity extends AppCompatActivity implements ComicListFragment.Callbacks,
                                                                    ComicsChecklistDialogFragment.ComicsChecklistDialogListener {

    private static final String TAG = ComicListActivity.class.getSimpleName();

    // Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
    private boolean mTwoPane;
    // Other interface fragments.
    private ComicDetailFragment mDetailFragment;
    private ComicListFragment mListFragment;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mNavDrawerTitles;

    private ActionBar mActionBar;

    private boolean mUserLearnedDrawer;
    private boolean mFromSavedInstanceState;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int resultCode = bundle.getInt(Constants.NOTIFICATION_RESULT);
                String mCurrentEditor = bundle.getString(Constants.NOTIFICATION_EDITOR);
                if (resultCode == Constants.RESULT_START)
                    Toast.makeText(getApplicationContext(), mCurrentEditor + getString(R.string.search_started), Toast.LENGTH_SHORT).show();
                else if (resultCode == Constants.RESULT_FINISHED)
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.search_completed), Toast.LENGTH_SHORT).show();
                else if (resultCode == Constants.RESULT_EDITOR_FINISHED) {
                    if (mListFragment != null)
                        mListFragment.setRefreshing(false);
                    Toast.makeText(getApplicationContext(), mCurrentEditor + getResources().getString(R.string.search_editor_completed), Toast.LENGTH_SHORT).show();
                } else if (resultCode == Constants.RESULT_CANCELED) {
                    if (mListFragment != null)
                        mListFragment.setRefreshing(false);
                    Toast.makeText(getApplicationContext(), mCurrentEditor + getResources().getString(R.string.search_failed), Toast.LENGTH_LONG).show();
                } else if (resultCode == Constants.RESULT_NOT_CONNECTED)
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_no_connection), Toast.LENGTH_LONG).show();
                else if (resultCode == Constants.RESULT_DESTROYED)
                    Log.i(TAG, "Service destroyed");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mUserLearnedDrawer = sp.getBoolean(Constants.PREF_USER_LEARNED_DRAWER, false);

        setContentView(R.layout.activity_comic_list);
        if (findViewById(R.id.comic_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        mTitle = mDrawerTitle = getTitle();
        mNavDrawerTitles = getResources().getStringArray(R.array.drawer_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navigation_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // Set up the drawer's list view with items and click listener
        mDrawerList.setAdapter((new NavDrawerAdapter(this, R.layout.list_item_drawer, mNavDrawerTitles)));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        //mDrawerList.setItemChecked(mCurrentSelectedPosition, true);

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
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ComicListActivity.this);
                    sp.edit().putBoolean(Constants.PREF_USER_LEARNED_DRAWER, true).apply();
                }

                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mDrawerList);
        }

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(TAG, "onNewIntent " + intent);
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
            doMySearch(query);
        } else if (intent.getAction() != null && intent.getAction().equals(Constants.ACTION_COMIC_WIDGET)) {
            int comicId = intent.getIntExtra(Constants.COMIC_ID_FROM_WIDGET, 0);
            Log.d(TAG, "Comic id is " + comicId);
            launchDetailView(comicId, "preferiti");
        }
    }

    /**
     * Method used to do a search and show founded data in a dialog.
     * @param query the text to search on database
     */
    private void doMySearch(String query) {
        Log.d(TAG, "Searching " + query);
        Cursor cursor = this.getContentResolver().query(ComicContentProvider.CONTENT_URI, null, ComicDatabase.COMICS_NAME_KEY + " LIKE ?",
                new String[] {"%" + query + "%"}, null);

        if (cursor != null && cursor.getCount() == 0) {
            Log.d(TAG, "No data found!");
            // There are no results
            cursor.close();
            Toast.makeText(this, getResources().getText(R.string.search_no_result), Toast.LENGTH_SHORT).show();
        } else if (cursor != null && cursor.getCount() > 0) {
            Log.d(TAG, "Found data: " + cursor.getCount());
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
            if (mListFragment != null)
                mListFragment.setActivateOnItemClick(true);
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
        if (!mDrawerLayout.isDrawerOpen(mDrawerList)) {
            // Get the SearchView and set the searchable configuration
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            // Assumes current activity is the searchable activity
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        // Hide detail buttons
        if (mTwoPane && mDetailFragment != null) {
            menu.findItem(R.id.calendar).setVisible(!drawerOpen);
            menu.findItem(R.id.favorite).setVisible(!drawerOpen);
            menu.findItem(R.id.buy).setVisible(!drawerOpen);
            menu.findItem(R.id.search).setVisible(!drawerOpen);
        } else {
            menu.findItem(R.id.search).setVisible(!drawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void selectItem(int position) {
        Log.d(TAG, "selectItem " + position);
        if (mDrawerList != null) {
            mDrawerList.setItemChecked(position, true);
            if (mDrawerLayout != null) {
                if (position < 8) {
                    // Update selected item title, then close the drawer
                    setTitle(mNavDrawerTitles[position]);
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
            }
        }
        if (position <= 7) {
            // Update the main content by replacing fragments
            mListFragment = ComicListFragment.newInstance(position);
            getSupportFragmentManager().beginTransaction().replace(R.id.comic_list_container, mListFragment).commit();
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

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (mActionBar != null) {
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
     * Callback method from {@link ComicListFragment.Callbacks}
     * indicating that the comic was selected.
     */
    @Override
    public void onItemSelected(long id, String section) {
        Log.d(TAG, "Launching comic with id " + id + " section " + section);
        launchDetailView(id, section);
    }

    /**
     * This method launch detail view in a Fragment or on a new Activity.
     * @param id the comic id
     * @param section is the editor of the comic
     */
    public void launchDetailView(long id, String section) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(ComicDetailFragment.ARG_COMIC_ID, id);
            arguments.putString(ComicDetailFragment.ARG_SECTION, section);
            mDetailFragment = new ComicDetailFragment();
            mDetailFragment.setArguments(arguments);
            // TODO warning: http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
            getSupportFragmentManager().beginTransaction().replace(R.id.comic_detail_container, mDetailFragment).commitAllowingStateLoss();
        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ComicDetailActivity.class);
            detailIntent.putExtra(ComicDetailFragment.ARG_COMIC_ID, id);
            detailIntent.putExtra(ComicDetailFragment.ARG_SECTION, section);
            startActivity(detailIntent);
        }
    }

    /******************************************************************************************
     * DIALOG CALLBACK
     ******************************************************************************************/

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int dialogId) {
        switch (dialogId) {
            case Constants.DIALOG_ADD_COMIC:
                dialog.dismiss();
                Toast.makeText(this, getResources().getString(R.string.comic_added_cart), Toast.LENGTH_SHORT).show();
                break;
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
            case Constants.DIALOG_ADD_COMIC:
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
    public void onDialogListItemClick(DialogFragment dialog, int dialogId, long id, String search) {
        dialog.dismiss();
        launchDetailView(id, search);
    }
}
