package org.checklist.comics.comicschecklist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.checklist.comics.comicschecklist.util.Constants;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    // A pointer to the current callbacks instance (the Activity).
    private NavigationDrawerCallbacks mCallbacks;
    // Helper component that ties the action bar to the navigation drawer.
    private ActionBarDrawerToggle mDrawerToggle;
    // Other utils
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;
    private String[] mNavDrawerTitles;
    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(Constants.PREF_USER_LEARNED_DRAWER, false);
        mNavDrawerTitles = getResources().getStringArray(R.array.nav_drawer_array);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(Constants.STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        mDrawerListView.setAdapter((new EditorAdapter(getActivity(), R.layout.row_drawer_list_item, mNavDrawerTitles)));
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        //ActionBar actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                toolbar,             /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,   /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(Constants.PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            if (position < 8)
                mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.menu_main, menu);
            //howGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressLint("InflateParams")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                // Open settings
                Intent launchPreferencesIntent = new Intent().setClass(getActivity(), SettingsActivity.class);
                startActivity(launchPreferencesIntent);
                return true;
            case R.id.google:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/115824315702252939905/posts")));
                return true;
            case R.id.guida:
                // Open help dialog
                DialogFragment helpDialog = ComicsChecklistDialogFragment.newInstance(0);
                helpDialog.show(getFragmentManager(), "ComicsChecklistDialogFragment");
                return true;
            case R.id.info:
                // Open info dialog
                DialogFragment infoDialog = ComicsChecklistDialogFragment.newInstance(1);
                infoDialog.show(getFragmentManager(), "ComicsChecklistDialogFragment");
                return true;
            default:
                // Launch action
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    /**private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return getActivity().getActionBar();
    }*/

    /** Return the selected item in position. */
    public int getCheckedItem() {
        return mDrawerListView.getCheckedItemPosition();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

    /** Adapter used for the list in DrawerLayout. */
    public class EditorAdapter extends ArrayAdapter<String> {
        private int resource;
        private LayoutInflater inflater;

        public EditorAdapter (Context context, int resourceId, String[] mEditorTitles) {
            super(context, resourceId, mEditorTitles);
            resource = resourceId;
            inflater = LayoutInflater.from(context);
        }

        /** This method set the section info. */
        @Override
        public View getView (int position, View convertView, ViewGroup parent) {

            String editore = getItem(position);

            EditorViewCache viewCache;

            if (convertView == null) {
                convertView = inflater.inflate(resource, null);
                viewCache = new EditorViewCache(convertView);
                convertView.setTag(viewCache);
            } else {
                viewCache = (EditorViewCache) convertView.getTag();
            }

            // Set icon with related method
            TextView editorName = viewCache.getTextViewName();
            editorName.setCompoundDrawablesWithIntrinsicBounds(setIcon(editore), 0, 0, 0);
            editorName.setCompoundDrawablePadding(20);
            editorName.setText(editore);

            View separator = viewCache.getSeparatorView();
            if (editore.equalsIgnoreCase(getResources().getString(R.string.title_section3)) ||
                    editore.equalsIgnoreCase(getResources().getString(R.string.title_section9)))
                separator.setVisibility(View.VISIBLE);

            return convertView;
        }

        /** This method set the icon for a specific section. */
        public int setIcon(String editorName) {
            int resId = R.drawable.ic_action_book;
            if (editorName.equalsIgnoreCase(getResources().getString(R.string.title_section1)))
                resId = R.drawable.ic_action_star_10;
            else if (editorName.equalsIgnoreCase(getResources().getString(R.string.title_section2)))
                resId = R.drawable.ic_action_cart;
            else if (editorName.equalsIgnoreCase(getResources().getString(R.string.title_section9)))
                resId = R.drawable.ic_action_settings;
            else if (editorName.equalsIgnoreCase(getResources().getString(R.string.title_section10)))
                resId = R.drawable.ic_action_plus_1;
            else if (editorName.equalsIgnoreCase(getResources().getString(R.string.title_section11)))
                resId = R.drawable.ic_action_help;
            else if (editorName.equalsIgnoreCase(getResources().getString(R.string.title_section12)))
                resId = R.drawable.ic_action_info;
            return resId;
        }
    }

    /**  Cache of elements which compose the list. */
    public class EditorViewCache {

        private View baseView;
        private TextView textViewName;
        private View separatorView;

        public EditorViewCache (View baseView) {
            this.baseView = baseView;
        }

        public TextView getTextViewName() {
            if (textViewName == null) {
                textViewName = (TextView) baseView.findViewById(R.id.editorName);
            }
            return textViewName;
        }

        public View getSeparatorView() {
            if (separatorView == null) {
                separatorView = baseView.findViewById(R.id.separator);
            }
            return separatorView;
        }
    }
}
