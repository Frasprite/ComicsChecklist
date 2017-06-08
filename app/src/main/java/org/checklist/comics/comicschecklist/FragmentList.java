package org.checklist.comics.comicschecklist;

import android.content.ContentValues;
import android.content.Context;
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
import android.text.TextUtils;
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
import android.widget.Toast;

import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.util.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;

/**
 * A list fragment representing a list of Comics. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link FragmentDetail}.
 * <p>
 */
public class FragmentList extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = FragmentList.class.getSimpleName();

    private SimpleCursorAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Constants.Sections mEditor;
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
    interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected(long id);

        /**
         * Callback for when list is scrolled down / up.
         */
        void onHideBottomView();

        /**
         * Callback for when list is scrolled stops.
         */
        void onShowBottomView();
    }

    /**
     * An implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final Callbacks sComicCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(long id) {}

        @Override
        public void onHideBottomView() {}

        @Override
        public void onShowBottomView() {}
    };

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static FragmentList newInstance(Constants.Sections section) {
        CCLogger.v(TAG, "newInstance - " + section.toString());
        FragmentList fragment = new FragmentList();
        Bundle args = new Bundle();
        args.putSerializable(Constants.ARG_EDITOR, section);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Find editor
        mEditor = (Constants.Sections) getArguments().getSerializable(Constants.ARG_EDITOR);
        CCLogger.d(TAG, "onCreate - name " + mEditor);

        fillData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CCLogger.d(TAG, "onCreateView - start");
        // Create the list fragment's content view by calling the super method
        final View listFragmentView = inflater.inflate(R.layout.fragment_main, container, false);//super.onCreateView(inflater, container, savedInstanceState);

        getActivity().setTitle(Constants.Sections.getTitle(mEditor));

        if (mEditor.equals(Constants.Sections.FAVORITE) || mEditor.equals(Constants.Sections.CART)) {
            CCLogger.v(TAG, "onCreateView - created a ListFragment - end");
            return listFragmentView;
        } else {
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
            mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_light, R.color.primary, R.color.primary_dark, R.color.accent);

            // Now return the SwipeRefreshLayout as this fragment's content view
            CCLogger.v(TAG, "onCreateView - created a SwipeRefreshLayout - end");
            return mSwipeRefreshLayout;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CCLogger.d(TAG, "onViewCreated - start");
        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

        TextView emptyText = (TextView)view.findViewById(android.R.id.empty);

        if (mEditor.equals(Constants.Sections.FAVORITE)) {
            emptyText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_empty_list_stub, 0, 0);
            emptyText.setText(getString(R.string.empty_favorite_list));
        } else if (mEditor.equals(Constants.Sections.CART)) {
            emptyText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_empty_list_stub, 0, 0);
            emptyText.setText(getString(R.string.empty_cart_list));
        } else {
            emptyText.setText(getString(R.string.empty_editor_list));
        }

        /*
          Implement {@link SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
          refresh" gesture, SwipeRefreshLayout invokes
          {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
          {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
          refreshes the content. Call the same method in response to the Refresh action from the
          action bar.
         */
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ActivityMain activityMain = (ActivityMain) getActivity();
                activityMain.initiateRefresh(mEditor);
            }
        });

        final int[] mLastFirstVisibleItem = {0};
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem > mLastFirstVisibleItem[0]) {
                    //CCLogger.v(TAG, "onScroll - Scrolling down...");
                    mCallbacks.onHideBottomView();

                } else if (firstVisibleItem < mLastFirstVisibleItem[0]) {
                    //CCLogger.v(TAG, "onScroll - Scrolling up...");
                    mCallbacks.onHideBottomView();
                }

                mLastFirstVisibleItem[0] = firstVisibleItem;
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    //CCLogger.v(TAG, "onScroll - Scrolling stopped...");
                    mCallbacks.onShowBottomView();
                }
            }
        });

        CCLogger.v(TAG, "onViewCreated - end");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CCLogger.v(TAG, "onAttach");
        // Activities containing this fragment must implement its callbacks.
        if (!(context instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        CCLogger.v(TAG, "onDetach");
        // Reset the active callbacks interface to the callback implementation.
        mCallbacks = sComicCallbacks;
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        CCLogger.v(TAG, "onActivityCreated");
        if (mEditor.equals(Constants.Sections.FAVORITE) || mEditor.equals(Constants.Sections.CART)) {
            registerForContextMenu(getListView());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.context_menu_delete_comic);
        menu.add(0, DELETE_ALL, 1, R.string.context_menu_delete_all);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContentValues mUpdateValues;
        switch (item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mUpdateValues = new ContentValues();
                if (mEditor.equals(Constants.Sections.FAVORITE)) {
                    mUpdateValues.put(ComicDatabase.COMICS_FAVORITE_KEY, "no");
                    CCLogger.d(TAG, "onContextItemSelected - preparing for removing favorite comic with ID " + info.id);
                    // Defines selection criteria for the rows you want to update
                    String whereClause = ComicDatabase.ID + "=?";
                    String[] whereArgs = new String[]{String.valueOf(info.id)};
                    int rowUpdated = ComicDatabaseManager.update(getActivity(), mUpdateValues, whereClause, whereArgs);
                    CCLogger.v(TAG, "onContextItemSelected - favorite comic UPDATED " + rowUpdated);
                } else if (mEditor.equals(Constants.Sections.CART)) {
                    CCLogger.d(TAG, "onContextItemSelected - preparing for removing comic in cart with ID " + info.id);
                    removeComicFromCart(info.id);
                }
                WidgetService.updateWidget(getActivity());
                return true;
            case DELETE_ALL:
                mUpdateValues = new ContentValues();
                int updateResult;
                if (mEditor.equals(Constants.Sections.FAVORITE)) {
                    // Update all favorite
                    mUpdateValues.put(ComicDatabase.COMICS_FAVORITE_KEY, "no");
                    updateResult = ComicDatabaseManager.update(getActivity(), mUpdateValues, null, null);
                    if (updateResult > 0) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.comic_deleted_all_favorite), Toast.LENGTH_SHORT).show();
                        CCLogger.d(TAG, "onContextItemSelected - deleting all favorite comic");
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.comic_delete_all_fail), Toast.LENGTH_SHORT).show();
                        CCLogger.w(TAG, "onContextItemSelected - error while removing all comic from favorite");
                    }
                } else if (mEditor.equals(Constants.Sections.CART)) {
                    // Remove comic from cart
                    updateResult = removeAllComicFromCart();
                    if (updateResult > 0) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.comic_deleted_all_cart), Toast.LENGTH_SHORT).show();
                        CCLogger.d(TAG, "onContextItemSelected - deleting " + updateResult + " from cart");
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.comic_delete_all_fail), Toast.LENGTH_SHORT).show();
                        CCLogger.w(TAG, "onContextItemSelected - error while removing all comic from cart");
                    }
                }

                WidgetService.updateWidget(getActivity());
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void removeComicFromCart(long comicId) {
        // Evaluate if comic was created by user or it is coming from network
        Uri uri = Uri.parse(ComicContentProvider.CONTENT_URI + "/" + comicId);
        String[] projection = {ComicDatabase.COMICS_EDITOR_KEY};
        Cursor cursor = ComicDatabaseManager.query(getActivity(), uri, projection, null, null, null);
        cursor.moveToFirst();
        String rawEditor = cursor.getString(cursor.getColumnIndex(ComicDatabase.COMICS_EDITOR_KEY));
        Constants.Sections editor = Constants.Sections.getEditorFromName(rawEditor);
        CCLogger.d(TAG, "removeComicFromCart - rawEditor " + rawEditor + " editor " + editor + " for comicId " + comicId);
        cursor.close();
        // Defines selection criteria for the rows you want to update / remove
        String whereClause = ComicDatabase.ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(comicId)};
        switch (editor) {
            case CART:
                int rowRemoved = ComicDatabaseManager.delete(getActivity(), ComicContentProvider.CONTENT_URI, whereClause, whereArgs);
                CCLogger.v(TAG, "removeComicFromCart - row REMOVED " + rowRemoved);
                break;
            default:
                ContentValues mUpdateValues = new ContentValues();
                mUpdateValues.put(ComicDatabase.COMICS_CART_KEY, "no");
                int rowUpdated = ComicDatabaseManager.update(getActivity(), mUpdateValues, whereClause, whereArgs);
                CCLogger.v(TAG, "removeComicFromCart - row UPDATED " + rowUpdated);
                break;
        }
    }

    private int removeAllComicFromCart() {
        // Update all comic on cart and delete those manually created
        int total = 0;
        ContentValues mUpdateValues = new ContentValues();
        mUpdateValues.put(ComicDatabase.COMICS_CART_KEY, "no");
        total = total + ComicDatabaseManager.update(getActivity(), mUpdateValues, null, null);
        // Defines selection criteria for the rows you want to update / remove
        String whereClause = ComicDatabase.COMICS_EDITOR_KEY + "=?";
        String[] whereArgs = new String[]{String.valueOf(Constants.Sections.getName(Constants.Sections.CART))};
        total = total + ComicDatabaseManager.delete(getActivity(), ComicContentProvider.CONTENT_URI, whereClause, whereArgs);
        return total;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(id);
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
        CCLogger.v(TAG, "setActivatedPosition - position activated is " + position);
        mActivatedPosition = position;
    }

    /* ****************************************************************************************
     * SwipeRefreshLayout methods
     ******************************************************************************************/

    /**
     * Set the {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener} to listen for
     * initiated refreshes.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout#setOnRefreshListener(android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener)
     */
    private void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(listener);
        }
    }

    /**
     * Returns whether the {@link android.support.v4.widget.SwipeRefreshLayout} is currently
     * refreshing or not.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout#isRefreshing()
     */
    public boolean isRefreshing() {
        SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();
        if (swipeRefreshLayout != null) {
            return swipeRefreshLayout.isRefreshing();
        } else {
            CCLogger.v(TAG, "isRefreshing - Returning FALSE because swipe refresh layout is null");
            return false;
        }
    }

    /**
     * Set whether the {@link android.support.v4.widget.SwipeRefreshLayout} should be displaying
     * that it is refreshing or not.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout#setRefreshing(boolean)
     */
    public void setRefreshing(boolean refreshing) {
        SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(refreshing);
        }
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
        return ViewCompat.canScrollVertically(listView, -1);
    }

    /* ****************************************************************************************
     * Data loader methods
     ******************************************************************************************/

    /**
     * Method used to fill data on list.
     */
    private void fillData() {
        CCLogger.d(TAG, "fillData - start");
        // Fields from the database (projection) must include the id column for the adapter to work
        String[] from = new String[] {ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_RELEASE_KEY};
        // Fields on the UI to which we map
        int[] to = new int[] {android.R.id.text1, android.R.id.text2};

        getLoaderManager().initLoader(0, null, this);
        adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_activated_2, null, from, to, 0) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                text1.setMaxLines(1);
                text1.setEllipsize(TextUtils.TruncateAt.END);
                return view;
            }
        };
        setListAdapter(adapter);
        CCLogger.d(TAG, "fillData - end");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Order list by DESC or ASC
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String rawSortOrder = sharedPref.getString(Constants.PREF_LIST_ORDER, String.valueOf(Constants.Filters.getCode(Constants.Filters.DATE_ASC)));
        // Quick fix on migration (1.3 --> 1.4)
        if (rawSortOrder.equalsIgnoreCase("ASC") || rawSortOrder.equalsIgnoreCase("DESC")) {
            rawSortOrder = "1";
        }
        String sortOrder = Constants.Filters.getSortOrder(Integer.valueOf(rawSortOrder));
        CCLogger.d(TAG, "onCreateLoader - creating loader ordered with " + sortOrder);
        String[] projection = {ComicDatabase.ID, ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_RELEASE_KEY};
        String whereClause;
        String[] whereArgs;
        switch (mEditor) {
            case CART:
                CCLogger.d(TAG, "onCreateLoader - Loading CART content");
                // Load comic with special editor name and buy flag to true
                whereClause = ComicDatabase.COMICS_EDITOR_KEY + " LIKE ? OR " + ComicDatabase.COMICS_CART_KEY + " LIKE ?";
                whereArgs = new String[]{Constants.Sections.getName(mEditor), "yes"};
                break;
            case FAVORITE:
                CCLogger.d(TAG, "onCreateLoader - Loading FAVORITE content");
                // Load only comic with positive favorite flag
                whereClause = ComicDatabase.COMICS_FAVORITE_KEY + "=?";
                whereArgs = new String[]{"yes"};
                break;
            default:
                CCLogger.d(TAG, "onCreateLoader - Loading " + mEditor + " content");
                // Do a simple load from editor name
                whereClause = ComicDatabase.COMICS_EDITOR_KEY + "=?";
                whereArgs = new String[]{Constants.Sections.getName(mEditor)};
                break;
        }

        return new CursorLoader(getActivity(), ComicContentProvider.CONTENT_URI, projection, whereClause, whereArgs, sortOrder);
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
