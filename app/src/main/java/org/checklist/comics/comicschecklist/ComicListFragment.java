package org.checklist.comics.comicschecklist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.checklist.comics.comicschecklist.provider.CartContentProvider;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.database.CartDatabase;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.service.DownloadService;
import org.checklist.comics.comicschecklist.util.Constants;

/**
 * A list fragment representing a list of Comics. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link ComicDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ComicListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mEditor;
    private int mEditorNumber;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int DELETE_ALL = Menu.FIRST + 2;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sComicCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(long id, String mEditor);
    }

    /**
     * An implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sComicCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(long id, String mEditor) {}
    };

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static ComicListFragment newInstance(int section) {
        ComicListFragment fragment = new ComicListFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_SECTION_NUMBER, section);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Find editor
        mEditor = findEditor(getArguments().getInt(Constants.ARG_SECTION_NUMBER));
        mEditorNumber = getArguments().getInt(Constants.ARG_SECTION_NUMBER);
        // Retain this fragment across configuration changes.
        //setRetainInstance(true); // NOT GOOD WITH LOADERS!!!

        fillData();
    }

    public String findEditor(int section) {
        String result = Constants.FAVORITE;
        switch (section) {
            case 1:
                result = Constants.FAVORITE;
                break;
            case 2:
                result = Constants.CART;
                break;
            case 3:
                result = Constants.MARVEL;
                break;
            case 4:
                result = Constants.PANINI;
                break;
            case 5:
                result = Constants.PLANET;
                break;
            case 6:
                result = Constants.STAR;
                break;
            case 7:
                result = Constants.BONELLI;
                break;
            case 8:
                result = Constants.RW;
                break;
        }
        return result;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

        TextView emptyText = (TextView)view.findViewById(android.R.id.empty);

        if (mEditor.equalsIgnoreCase(Constants.FAVORITE)) {
            emptyText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star, 0, 0);
            emptyText.setText(getString(R.string.empty_favorite_list));
            //setEmptyText(getResources().getString(R.string.empty_favorite_list));
        } else if (mEditor.equalsIgnoreCase(Constants.CART)) {
            emptyText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_content_add, 0, 0);
            emptyText.setText(getString(R.string.empty_cart_list));
            //setEmptyText(getResources().getString(R.string.empty_cart_list));
        } else
            emptyText.setText(getString(R.string.empty_editor_list));
            //setEmptyText(getResources().getString(R.string.empty_editor_list));

        ListView lv = (ListView) view.findViewById(android.R.id.list);
        lv.setEmptyView(emptyText);
        if (mEditorNumber == 1 || mEditorNumber == 2)
            registerForContextMenu(lv);

        /**
         * Implement {@link SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
         * refresh" gesture, SwipeRefreshLayout invokes
         * {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
         * {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
         * refreshes the content. Call the same method in response to the Refresh action from the
         * action bar.
         */
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initiateRefresh();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Create the list fragment's content view by calling the super method
        final View listFragmentView = inflater.inflate(R.layout.fragment_list, container, false);//super.onCreateView(inflater, container, savedInstanceState);

        if (mEditor.equalsIgnoreCase(Constants.FAVORITE) || mEditor.equalsIgnoreCase(Constants.CART))
            return listFragmentView;
        else {
            // Now create a SwipeRefreshLayout to wrap the fragment's content view
            mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());

            // Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
            // the SwipeRefreshLayout
            mSwipeRefreshLayout.addView(listFragmentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            // Make sure that the SwipeRefreshLayout will fill the fragment
            mSwipeRefreshLayout.setLayoutParams(
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));

            // Set color
            mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_color_1, R.color.swipe_color_2, R.color.swipe_color_3, R.color.swipe_color_4);

            // Now return the SwipeRefreshLayout as this fragment's content view
            return mSwipeRefreshLayout;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((ComicListActivity) activity).onSectionAttached(getArguments().getInt(Constants.ARG_SECTION_NUMBER));
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Reset the active callbacks interface to the callback implementation.
        mCallbacks = sComicCallbacks;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        if (mEditor.equalsIgnoreCase(Constants.FAVORITE) || mEditor.equalsIgnoreCase(Constants.CART))
            registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.context_menu_delete_comic);
        menu.add(0, DELETE_ALL, 1, R.string.context_menu_delete_all);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                if (mEditor.equalsIgnoreCase(Constants.FAVORITE)) {
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    ContentValues mUpdateValues = new ContentValues();
                    mUpdateValues.put(ComicDatabase.COMICS_FAVORITE_KEY, "no");
                    Uri uri = Uri.parse(ComicContentProvider.CONTENT_URI + "/" + info.id);
                    getActivity().getContentResolver().update(uri, mUpdateValues, null, null);
                    return true;
                } else if (mEditor.equalsIgnoreCase(Constants.CART)) {
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    Uri uriCart = Uri.parse(CartContentProvider.CONTENT_URI + "/" + info.id);
                    String[] projection = {CartDatabase.ID, CartDatabase.COMICS_NAME_KEY};
                    Cursor mCursor = getActivity().getContentResolver().query(uriCart, projection, null, null, null);
                    mCursor.moveToFirst();
                    // Update entry on comic database
                    ContentValues mUpdateValues = new ContentValues();
                    mUpdateValues.put(ComicDatabase.COMICS_CART_KEY, "no");
                    String mComicName = mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_NAME_KEY));
                    String mSelectionClause = ComicDatabase.COMICS_NAME_KEY +  "=?";
                    String[] mSelectionArgs = {mComicName};
                    getActivity().getContentResolver().update(ComicContentProvider.CONTENT_URI, mUpdateValues, mSelectionClause, mSelectionArgs);
                    getActivity().getContentResolver().delete(uriCart, null, null);
                    mCursor.close();
                    return true;
                }
            case DELETE_ALL:
                // TODO show a dialog where user can confirm or not this action

                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        //mCallbacks.onItemSelected((String)getListAdapter().getItem(position));
        mCallbacks.onItemSelected(id, mEditor);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != AdapterView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? AbsListView.CHOICE_MODE_SINGLE
                : AbsListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == AdapterView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    /**
     * Set the {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener} to listen for
     * initiated refreshes.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout#setOnRefreshListener(android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener)
     */
    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setOnRefreshListener(listener);
    }

    /**
     * Returns whether the {@link android.support.v4.widget.SwipeRefreshLayout} is currently
     * refreshing or not.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout#isRefreshing()
     */
    public boolean isRefreshing() {
        return mSwipeRefreshLayout.isRefreshing();
    }

    /**
     * Set whether the {@link android.support.v4.widget.SwipeRefreshLayout} should be displaying
     * that it is refreshing or not.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout#setRefreshing(boolean)
     */
    public void setRefreshing(boolean refreshing) {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(refreshing);
    }

    /**
     * @return the fragment's {@link android.support.v4.widget.SwipeRefreshLayout} widget.
     */
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    /**
     * Sub-class of {@link android.support.v4.widget.SwipeRefreshLayout} for use in this
     * {@link android.support.v4.app.ListFragment}. The reason that this is needed is because
     * {@link android.support.v4.widget.SwipeRefreshLayout} only supports a single child, which it
     * expects to be the one which triggers refreshes. In our case the layout's child is the content
     * view returned from
     * {@link android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
     * which is a {@link android.view.ViewGroup}.
     *
     * <p>To enable 'swipe-to-refresh' support via the {@link android.widget.ListView} we need to
     * override the default behavior and properly signal when a gesture is possible. This is done by
     * overriding {@link #canChildScrollUp()}.
     */
    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

        public ListFragmentSwipeRefreshLayout(Context context) {
            super(context);
        }

        /**
         * As mentioned above, we need to override this method to properly signal when a
         * 'swipe-to-refresh' is possible.
         *
         * @return true if the {@link android.widget.ListView} is visible and can scroll up.
         */
        @Override
        public boolean canChildScrollUp() {
            final ListView listView = getListView();
            return listView.getVisibility() == View.VISIBLE && canListViewScrollUp(listView);
        }
    }

    /**
     * Utility method to check whether a {@link ListView} can scroll up from it's current position.
     * Handles platform version differences, providing backwards compatible functionality where
     * needed.
     */
    private static boolean canListViewScrollUp(ListView listView) {
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            // For ICS and above we can call canScrollVertically() to determine this
            return ViewCompat.canScrollVertically(listView, -1);
        } else {
            // Pre-ICS we need to manually check the first visible item and the child view's top
            // value
            return listView.getChildCount() > 0 &&
                    (listView.getFirstVisiblePosition() > 0
                            || listView.getChildAt(0).getTop() < listView.getPaddingTop());
        }
    }

    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     */
    private void initiateRefresh() {

        // Defines selection criteria for the rows to delete
        String mSelectionClause = ComicDatabase.COMICS_EDITOR_KEY + "=?";
        String[] mSelectionArgs = {mEditor};

        // Deletes the entries that match the selection criteria
        getActivity().getContentResolver().delete(
                ComicContentProvider.CONTENT_URI,   // the comic content URI
                mSelectionClause,                   // the column to select on
                mSelectionArgs                      // the value to compare to
        );

        // Update shared preference of editor as well
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (mEditor.equalsIgnoreCase(Constants.MARVEL))
            sp.edit().remove(Constants.PREF_MARVEL_LAST_SCAN).apply();
        else if (mEditor.equalsIgnoreCase(Constants.PANINI))
            sp.edit().remove(Constants.PREF_PANINI_LAST_SCAN).apply();
        else if (mEditor.equalsIgnoreCase(Constants.PLANET))
            sp.edit().remove(Constants.PREF_PLANET_LAST_SCAN).apply();
        else if (mEditor.equalsIgnoreCase(Constants.BONELLI))
            sp.edit().remove(Constants.PREF_BONELLI_LAST_SCAN).apply();
        else if (mEditor.equalsIgnoreCase(Constants.STAR))
            sp.edit().remove(Constants.PREF_STAR_LAST_SCAN).apply();
        else if (mEditor.equalsIgnoreCase(Constants.RW))
            sp.edit().remove(Constants.PREF_RW_LAST_SCAN).apply();

        /**
         * Execute the background task, which uses {@link org.checklist.comics.comicschecklist.service.DownloadService} to load the data.
         */
        Intent intent = new Intent(getActivity(), DownloadService.class);
        intent.putExtra(Constants.ARG_SECTION_NUMBER, mEditorNumber);
        getActivity().startService(intent);
        setRefreshing(true);
    }

    private void fillData() {
        // Fields from the database (projection) must include the id column for the adapter to work
        String[] from = new String[] {ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_RELEASE_KEY};
        // Fields on the UI to which we map
        int[] to = new int[] {android.R.id.text1, android.R.id.text2};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_activated_2, null, from, to, 0);

        setListAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Order list by DESC or ASC
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String order = sharedPref.getString("data_order", "ASC");
        String[] projection = {ComicDatabase.ID, ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_RELEASE_KEY};
        if (mEditor.equalsIgnoreCase(Constants.FAVORITE)) {
            return new CursorLoader(getActivity(), ComicContentProvider.CONTENT_URI, projection, ComicDatabase.COMICS_FAVORITE_KEY + "=?", new String[]{"yes"},
                    ComicDatabase.COMICS_DATE_KEY + " " + order);
        } else if (mEditor.equalsIgnoreCase(Constants.CART)) {
            return new CursorLoader(getActivity(), CartContentProvider.CONTENT_URI, projection, null, null, CartDatabase.COMICS_DATE_KEY + " " + order);
        } else {
            return new CursorLoader(getActivity(), ComicContentProvider.CONTENT_URI, projection, ComicDatabase.COMICS_EDITOR_KEY + "=?", new String[]{mEditor},
                    ComicDatabase.COMICS_DATE_KEY + " " + order);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // Data is not available anymore, delete reference
        adapter.swapCursor(null);
    }
}
