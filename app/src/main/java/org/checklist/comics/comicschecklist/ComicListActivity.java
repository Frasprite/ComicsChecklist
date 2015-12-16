package org.checklist.comics.comicschecklist;

import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
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
 * This activity also implements the required
 * {@link ComicListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ComicListActivity extends AppCompatActivity implements ComicListFragment.Callbacks, NavigationDrawerFragment.NavigationDrawerCallbacks,
                                                                   ComicsChecklistDialogFragment.ComicsChecklistDialogListener {

    private static final String TAG = ComicListActivity.class.getSimpleName();

    // Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
    private boolean mTwoPane;
    private CharSequence mTitle;
    // Other interface fragments.
    private ComicDetailFragment mDetailFragment;
    private ComicListFragment mListFragment;
    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private NavigationDrawerFragment mNavigationDrawerFragment;

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
        setContentView(R.layout.activity_comic_list);
        if (findViewById(R.id.comic_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(5);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), actionBar);

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
            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            //((ComicListFragment) getFragmentManager().findFragmentById(R.id.comic_list)).setActivateOnItemClick(true);
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
    public void onNavigationDrawerItemSelected(int position) {
        Log.d(TAG, "onNavigationDrawerItemSelected position " + position);
        if (position <= 7) {
            // Update the main content by replacing fragments
            //getSupportFragmentManager().beginTransaction().replace(R.id.comic_list_container, ComicListFragment.newInstance(position + 1)).commit();
            mListFragment = ComicListFragment.newInstance(position + 1);
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

    public void onSectionAttached(int section) {
        switch (section) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
            case 6:
                mTitle = getString(R.string.title_section6);
                break;
            case 7:
                mTitle = getString(R.string.title_section7);
                break;
            case 8:
                mTitle = getString(R.string.title_section8);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();

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
        boolean drawerOpen = mNavigationDrawerFragment.isDrawerOpen();
        // Hide detail buttons
        if (mTwoPane && mDetailFragment != null) {
            menu.findItem(R.id.calendar).setVisible(!drawerOpen);
            menu.findItem(R.id.favorite).setVisible(!drawerOpen);
            menu.findItem(R.id.buy).setVisible(!drawerOpen);
            menu.findItem(R.id.search).setVisible(!drawerOpen);
        } else
            menu.findItem(R.id.search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
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
