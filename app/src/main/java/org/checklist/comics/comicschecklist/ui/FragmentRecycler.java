package org.checklist.comics.comicschecklist.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.adapter.ComicAdapter;
import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.RecyclerViewEmptySupport;

/**
 * A fragment representing a list of Comics. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link FragmentDetail}.
 * <p>
 */
public class FragmentRecycler extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = FragmentRecycler.class.getSimpleName();

    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;

    public enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    private RecyclerViewEmptySupport mRecyclerView;
    private ComicAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Constants.Sections mEditor;

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sComicCallbacks;

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
    }

    /**
     * An implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final Callbacks sComicCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(long id) {}
    };

    private ItemTouchHelper.SimpleCallback sItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            // Remove swiped item from list and notify the RecyclerView
            int position = viewHolder.getAdapterPosition();
            Cursor cursor = mAdapter.getCursor();
            if (cursor != null) {
                cursor.moveToPosition(position);
                // Take comic ID
                long comicID = cursor.getLong(cursor.getColumnIndex(ComicDatabase.ID));
                CCLogger.v(TAG, "onSwiped - Deleting comic with ID " + comicID);
                deleteComic(comicID);
            } else {
                CCLogger.w(TAG, "onSwiped - Cursor is null, can't delete item!");
            }
        }
    };

    private ComicAdapter.ComicViewHolder.ViewHolderClicks mClickListener = new ComicAdapter.ComicViewHolder.ViewHolderClicks() {
        @Override
        public void itemClicked(View container, int position) {
            CCLogger.d(TAG, "itemClicked - Clicked " + position);
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            Cursor cursor = mAdapter.getCursor();
            if (cursor != null) {
                cursor.moveToPosition(position);
                long id = cursor.getLong(cursor.getColumnIndex(ComicDatabase.ID));
                mCallbacks.onItemSelected(id);
            }
        }
    };

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static FragmentRecycler newInstance(Constants.Sections section) {
        CCLogger.v(TAG, "newInstance - " + section.toString());
        FragmentRecycler fragment = new FragmentRecycler();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CCLogger.d(TAG, "onCreateView - start");
        // Create the list fragment's content view by calling the super method
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        mRecyclerView = view.findViewById(R.id.recyclerView);

        // Attach swipe to delete if we are in CART or FAVORITE section
        if (mEditor.equals(Constants.Sections.CART) || mEditor.equals(Constants.Sections.FAVORITE)) {
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(sItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(mRecyclerView);
        }

        // Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setEmptyView(view.findViewById(R.id.empty_text_view));

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        getActivity().setTitle(Constants.Sections.getTitle(mEditor));

        if (mEditor.equals(Constants.Sections.FAVORITE) || mEditor.equals(Constants.Sections.CART)) {
            CCLogger.v(TAG, "onCreateView - Created a Fragment - end");
            return view;
        } else {
            // Now create a SwipeRefreshLayout to wrap the fragment's content view
            mSwipeRefreshLayout = new FragmentRecyclerSwipeRefreshLayout(container.getContext());

            // Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
            // the SwipeRefreshLayout
            mSwipeRefreshLayout.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            // Make sure that the SwipeRefreshLayout will fill the fragment
            mSwipeRefreshLayout.setLayoutParams(
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));

            // Set color
            mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_light, R.color.primary, R.color.primary_dark, R.color.accent);

            // Now return the SwipeRefreshLayout as this fragment's content view
            CCLogger.v(TAG, "onCreateView - Created a SwipeRefreshLayout - end");
            return mSwipeRefreshLayout;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CCLogger.d(TAG, "onViewCreated - start");

        fillData();

        TextView emptyText = view.findViewById(R.id.empty_text_view);

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

        CCLogger.v(TAG, "onViewCreated - end");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
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

    public void deleteComic(long itemID) {
        ContentValues mUpdateValues = new ContentValues();
        if (mEditor.equals(Constants.Sections.FAVORITE)) {
            mUpdateValues.put(ComicDatabase.COMICS_FAVORITE_KEY, "no");
            CCLogger.d(TAG, "onContextItemSelected - preparing for removing favorite comic with ID " + itemID);
            // Defines selection criteria for the rows you want to update
            String whereClause = ComicDatabase.ID + "=?";
            String[] whereArgs = new String[]{String.valueOf(itemID)};
            int rowUpdated = ComicDatabaseManager.update(getActivity(), mUpdateValues, whereClause, whereArgs);
            CCLogger.v(TAG, "onContextItemSelected - favorite comic UPDATED " + rowUpdated);
        } else if (mEditor.equals(Constants.Sections.CART)) {
            CCLogger.d(TAG, "onContextItemSelected - preparing for removing comic in cart with ID " + itemID);
            removeComicFromCart(itemID);
        }

        WidgetService.updateWidget(getActivity());
    }

    public void deleteAllComic() {
        ContentValues mUpdateValues = new ContentValues();
        int updateResult;
        switch (mEditor) {
            case FAVORITE:
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
                break;
            case CART:
                // Remove comic from cart
                updateResult = removeAllComicFromCart();
                if (updateResult > 0) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_deleted_all_cart), Toast.LENGTH_SHORT).show();
                    CCLogger.d(TAG, "onContextItemSelected - deleting " + updateResult + " from cart");
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_delete_all_fail), Toast.LENGTH_SHORT).show();
                    CCLogger.w(TAG, "onContextItemSelected - error while removing all comic from cart");
                }
                break;
            default:
                // Removing all comics of current editor
                CCLogger.v(TAG, "deleteAllComic - Deleting all comics from this editor " + mEditor);
                String selection = ComicDatabase.COMICS_EDITOR_KEY + " =? AND " +
                        ComicDatabase.COMICS_CART_KEY + " =? AND " +
                        ComicDatabase.COMICS_FAVORITE_KEY + " =?";
                String[] selectionArgs = new String[]{mEditor.getName(), "no", "no"};
                int result = ComicDatabaseManager.delete(getActivity(), ComicContentProvider.CONTENT_URI, selection, selectionArgs);
                CCLogger.d(TAG, "Deleted " + result + " entries of " + mEditor + " section");
                break;

        }
    }

    /**
     * Method which return the current layout of list.
     * @return the {@link LayoutManagerType}
     */
    public LayoutManagerType getCurrentLayoutManagerType() {
        return mCurrentLayoutManagerType;
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
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

    /**
     * Method used to return current editor.
     * @return the editor showed on UI.
     */
    public Constants.Sections getCurrentEditor() {
        return mEditor;
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
    private class FragmentRecyclerSwipeRefreshLayout extends SwipeRefreshLayout {

        public FragmentRecyclerSwipeRefreshLayout(Context context) {
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
            return mRecyclerView.getVisibility() == View.VISIBLE && mRecyclerView.canScrollVertically(-1);
        }
    }

    /* ****************************************************************************************
     * Data loader methods
     ******************************************************************************************/

    /**
     * Method used to fill data on list.
     */
    private void fillData() {
        CCLogger.d(TAG, "fillData - start");

        getLoaderManager().initLoader(0, null, this);
        // Specify an adapter (see also next example)
        mAdapter = new ComicAdapter(getActivity(), null, mClickListener);
        mRecyclerView.setAdapter(mAdapter);
        CCLogger.v(TAG, "fillData - end");
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
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // Data is not available anymore, delete reference
        mAdapter.swapCursor(null);
    }
}
