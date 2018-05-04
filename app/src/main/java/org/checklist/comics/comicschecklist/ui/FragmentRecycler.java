package org.checklist.comics.comicschecklist.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentValues;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.databinding.FragmentRecyclerViewBinding;
import org.checklist.comics.comicschecklist.model.Comic;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.viewmodel.ComicListViewModel;

import java.util.List;

/**
 * A fragment representing a list of Comics. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link FragmentDetail}.
 * <p>
 */
public class FragmentRecycler extends Fragment {

    private static final String TAG = FragmentRecycler.class.getSimpleName();

    private ComicAdapter mComicAdapter;
    private FragmentRecyclerViewBinding mBinding;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Constants.Sections mEditor;

    private ItemTouchHelper.SimpleCallback sItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            // Remove swiped item from list and notify the RecyclerView
            /*int position = viewHolder.getAdapterPosition();
            Cursor cursor = mAdapter.getCursor();
            if (cursor != null) {
                cursor.moveToPosition(position);
                // Take comic ID
                long comicID = cursor.getLong(cursor.getColumnIndex(ComicDatabase.ID));
                CCLogger.v(TAG, "onSwiped - Deleting comic with ID " + comicID);
                deleteComic(comicID);
            } else {
                CCLogger.w(TAG, "onSwiped - Cursor is null, can't delete item!");
            }*/
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

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recycler_view, container, false);

        mComicAdapter = new ComicAdapter(mComicClickCallback);
        mBinding.recyclerView.setAdapter(mComicAdapter);

        RecyclerView recyclerView = mBinding.getRoot().findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = mBinding.getRoot().findViewById(R.id.swipe_refresh_layout);

        // Attach swipe to delete (right / left) if we are in CART or FAVORITE section
        if (mEditor.equals(Constants.Sections.CART) || mEditor.equals(Constants.Sections.FAVORITE)) {
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(sItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }

        // Set title into toolbar
        getActivity().setTitle(Constants.Sections.getTitle(mEditor));

        // Enable swipe to refresh if we are on other categories
        if (mEditor.equals(Constants.Sections.FAVORITE) || mEditor.equals(Constants.Sections.CART)) {
            CCLogger.v(TAG, "onCreateView - Locking swipe to refresh");
            mSwipeRefreshLayout.setEnabled(false);
        }

        // Set color of progress view
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_light, R.color.primary, R.color.primary_dark, R.color.accent);

        setRecyclerViewLayoutManager(recyclerView);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CCLogger.d(TAG, "onViewCreated - start");

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ComicListViewModel viewModel =
                ViewModelProviders.of(this).get(ComicListViewModel.class);

        subscribeUi(viewModel);
    }

    /**
     * Set the layout type of {@link RecyclerView}.
     * @param recyclerView the current recycler view where to set layout
     */
    private void setRecyclerViewLayoutManager(RecyclerView recyclerView) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.scrollToPosition(scrollPosition);
    }

    /**
     * Method which observe the status of database and update UI when data is available.
     * @param viewModel the view model which store and manage the data to show
     */
    private void subscribeUi(ComicListViewModel viewModel) {
        // Update the list when the data changes
        viewModel.filterByEditor(mEditor.getName());
        viewModel.getComics().observe(this, new Observer<List<ComicEntity>>() {
            @Override
            public void onChanged(@Nullable List<ComicEntity> myComics) {
                if (myComics != null) {
                    if (myComics.size() == 0) {
                        mBinding.setIsLoading(true);
                    } else {
                        mBinding.setIsLoading(false);
                    }
                    mComicAdapter.setComicList(myComics);
                } else {
                    mBinding.setIsLoading(true);
                }
                // Espresso does not know how to wait for data binding's loop so we execute changes
                // sync.
                mBinding.executePendingBindings();
            }
        });
    }

    private final ComicClickCallback mComicClickCallback = new ComicClickCallback() {
        @Override
        public void onClick(Comic comic) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                ((ActivityMain) getActivity()).launchDetailView(comic);
            }
        }
    };

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

    /*public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
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
    }*/
}
