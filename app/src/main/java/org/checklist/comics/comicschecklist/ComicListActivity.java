package org.checklist.comics.comicschecklist;

import android.app.DialogFragment;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
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
import org.checklist.comics.comicschecklist.database.ComicDatabaseHelper;
import org.checklist.comics.comicschecklist.database.SuggestionDatabase;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.provider.SuggestionProvider;
import org.checklist.comics.comicschecklist.service.DownloadService;
import org.checklist.comics.comicschecklist.util.AppRater;
import org.checklist.comics.comicschecklist.util.Constants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
                    Toast.makeText(getApplicationContext(), mCurrentEditor + " " + getString(R.string.search_started), Toast.LENGTH_SHORT).show();
                else if (resultCode == Constants.RESULT_FINISHED)
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.search_completed), Toast.LENGTH_SHORT).show();
                else if (resultCode == Constants.RESULT_EDITOR_FINISHED) {
                    if (mListFragment != null)
                        mListFragment.setRefreshing(false);
                    Toast.makeText(getApplicationContext(), mCurrentEditor + ": " + getResources().getString(R.string.search_editor_completed), Toast.LENGTH_SHORT).show();
                } else if (resultCode == Constants.RESULT_CANCELED) {
                    if (mListFragment != null)
                        mListFragment.setRefreshing(false);
                    Toast.makeText(getApplicationContext(), mCurrentEditor + ": " + getResources().getString(R.string.search_failed), Toast.LENGTH_LONG).show();
                } else if (resultCode == Constants.RESULT_NOT_CONNECTED)
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
                else if (resultCode == Constants.RESULT_DESTROYED)
                    Log.i(Constants.LOG_TAG, "Service destroyed");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_list);

        getSupportActionBar();

        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), getSupportActionBar());

        // TODO detect if this is the first launch and ask user if he want to launch download now
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);

        // Launch AppRater
        AppRater.app_launched(this);

        // Handle search intent
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * Method used to handle the intent received from search.
     * @param intent the intent passed through search
     */
    private void handleIntent(Intent intent) {
        // Get the intent, verify the action and get the query
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // Handles a click on a search suggestion; launches activity/fragment to show comic detail
            Uri uri = intent.getData();
            Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();

                String name = cursor.getString(cursor.getColumnIndexOrThrow(SuggestionDatabase.KEY_COMIC_NAME));
                //String release = cursor.getString(cursor.getColumnIndexOrThrow(SuggestionDatabase.KEY_COMIC_RELEASE));
                // Replace special characters
                name = name.replaceAll("'", "''");

                ComicDatabaseHelper database = new ComicDatabaseHelper(this);
                // Using SQLiteQueryBuilder instead of query() method
                SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                // Set the table
                queryBuilder.setTables(ComicDatabase.COMICS_TABLE);
                // Adding name and release to the original query
                queryBuilder.appendWhere(ComicDatabase.COMICS_NAME_KEY + "='" + name + "'");
                SQLiteDatabase db = database.getWritableDatabase();
                Cursor comicCursor = queryBuilder.query(db, null, null, null, null, null, null);

                if (comicCursor != null) {
                    comicCursor.moveToFirst();
                    launchDetailView(comicCursor.getLong(comicCursor.getColumnIndex(ComicDatabase.ID)), "search");
                    comicCursor.close();
                } else
                    Toast.makeText(this, getResources().getText(R.string.search_error), Toast.LENGTH_SHORT).show();

                cursor.close();
            }
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    /**
     * Method used to do a search and show founded data in a dialog.
     * @param query the text to search on database
     */
    private void doMySearch(String query) {
        Cursor cursor = this.getContentResolver().query(SuggestionProvider.CONTENT_URI, null, null,
                new String[] {query}, null);

        if (cursor == null) {
            // There are no results
            Toast.makeText(this, getResources().getText(R.string.search_no_result), Toast.LENGTH_SHORT).show();
        } else {
            // There are multiple results which fit the given query, so show this on dialog
            cursor.close();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(Constants.PREF_SEARCH_QUERY, query);
            editor.apply();
            // Open a dialog with a list of results
            DialogFragment listDialog = ComicsChecklistDialogFragment.newInstance(4);
            listDialog.show(getFragmentManager(), "ComicsChecklistDialogFragment");
        }
    }

    /**
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
        // This code was originally on onCreate method; with fragments, must be placed here
        if (findViewById(R.id.comic_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

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
        registerReceiver(receiver, new IntentFilter(Constants.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.i(Constants.LOG_TAG, "onNavigationDrawerItemSelected position " + position);
        if (position <= 7) {
            // Update the main content by replacing fragments
            //getSupportFragmentManager().beginTransaction().replace(R.id.comic_list_container, ComicListFragment.newInstance(position + 1)).commit();
            mListFragment = mListFragment.newInstance(position + 1);
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
                    DialogFragment helpDialog = ComicsChecklistDialogFragment.newInstance(0);
                    helpDialog.show(getFragmentManager(), "ComicsChecklistDialogFragment");
                    break;
                case 11:
                    // Open info dialog
                    DialogFragment infoDialog = ComicsChecklistDialogFragment.newInstance(1);
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
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
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
            getSupportFragmentManager().beginTransaction().replace(R.id.comic_detail_container, mDetailFragment).commit();
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
    public void onDialogPositiveClick(DialogFragment dialog, String name, String info, String releaseDate) {
        if (name.length() > 0) {
            dialog.dismiss();
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date myDate;
            try {
                myDate = formatter.parse(releaseDate);
            } catch (ParseException e) {
                e.printStackTrace();
                myDate = new Date();
            }
            // Set the format to sql date time
            ContentValues values = new ContentValues();
            values.put(ComicDatabase.COMICS_NAME_KEY, name);
            values.put(ComicDatabase.COMICS_EDITOR_KEY, Constants.CART);
            values.put(ComicDatabase.COMICS_DESCRIPTION_KEY, info);
            values.put(ComicDatabase.COMICS_RELEASE_KEY, releaseDate);
            values.put(ComicDatabase.COMICS_DATE_KEY, myDate.getTime());
            values.put(ComicDatabase.COMICS_COVER_KEY, "error");
            values.put(ComicDatabase.COMICS_FEATURE_KEY, "N.D.");
            values.put(ComicDatabase.COMICS_PRICE_KEY, "N.D.");
            values.put(ComicDatabase.COMICS_CART_KEY, "yes");
            values.put(ComicDatabase.COMICS_FAVORITE_KEY, "no");

            this.getContentResolver().insert(ComicContentProvider.CONTENT_URI, values);
            Toast.makeText(this, getResources().getString(R.string.comic_added_cart), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, getResources().getString(R.string.fill_data_alert), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    @Override
    public void onDialogRateClick(DialogFragment dialog) {
        dialog.dismiss();
        this.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + this.getPackageName())));
    }

    @Override
    public void onDialogAbortRateClick(DialogFragment dialog) {
        dialog.dismiss();
        SharedPreferences prefs = this.getSharedPreferences (Constants.PREF_APP_RATER, 0);
        final SharedPreferences.Editor editorPref = prefs.edit();
        if (editorPref != null) {
            editorPref.putBoolean(Constants.PREF_USER_DONT_RATE, true);
            editorPref.apply();
        }
    }

    @Override
    public void onDialogListItemClick(DialogFragment dialog, long id, String search) {
        dialog.dismiss();
        launchDetailView(id, search);
    }
}
