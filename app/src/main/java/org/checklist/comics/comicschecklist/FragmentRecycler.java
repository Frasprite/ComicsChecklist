package org.checklist.comics.comicschecklist;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.checklist.comics.comicschecklist.adapter.CustomCursorRecyclerViewAdapter;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.util.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;

public class FragmentRecycler extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, RecyclerView.OnItemTouchListener {

    // TODO highlight selected item (and dave it on save instance state)
    // http://stackoverflow.com/questions/27194044/how-to-properly-highlight-selected-item-on-recyclerview
    // http://stackoverflow.com/questions/26682277/how-do-i-get-the-position-selected-in-a-recyclerview
    // TODO add expanded FAB menu
    // http://stackoverflow.com/questions/30699302/android-design-support-library-fab-menu
    // https://github.com/pmahsky/FloatingActionMenuAndroid
    // TODO add refresh on FAB sub-menu
    // TODO add refresh progress on UI
    // TODO add empty list UI

    private static final String TAG = FragmentRecycler.class.getSimpleName();

    private Constants.Sections mEditor;
    private CustomCursorRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;

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
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected(long id);

        /**
         * Callback for when list is scrolled down / up.
         */
        void onHideFAB();

        /**
         * Callback for when list is scrolled stops.
         */
        void onShowFAB();
    }

    /**
     * An implementation of the {@link FragmentRecycler.Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final FragmentRecycler.Callbacks sComicCallbacks = new FragmentRecycler.Callbacks() {
        @Override
        public void onItemSelected(long id) {}

        @Override
        public void onHideFAB() {}

        @Override
        public void onShowFAB() {}
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

        fillData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        CCLogger.d(TAG, "onCreateView - start");
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new CustomCursorRecyclerViewAdapter(getActivity(), null);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    mCallbacks.onHideFAB();
                } else {
                    mCallbacks.onShowFAB();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mRecyclerView.addOnItemTouchListener(this);

        getActivity().getSupportLoaderManager().restartLoader(0, null, this);

        return view;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        // Case item is clicked
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mGestureDetector.onTouchEvent(e)) {
            long id = mAdapter.getItemId(view.getChildAdapterPosition(childView));
            CCLogger.d(TAG, "onItemClick - item ID " + id);
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mCallbacks.onItemSelected(id);
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        // Not used
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // Not used
    }

    /**
     * Using {@link GestureDetector} in order to detect if an item is clicked for a long time.
     */
    GestureDetector mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Case item is long clicked
            View child = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (child != null && mEditor.equals(Constants.Sections.FAVORITE) || mEditor.equals(Constants.Sections.CART)) {
                long id = mAdapter.getItemId(mRecyclerView.getChildAdapterPosition(child));
                CCLogger.d(TAG, "onLongItemClick - ID " + id);
                launchActionDialog(id);
            }
        }
    });

    /**
     * Used to launch a confirm dialog with some options.
     * @param itemID the ID of item selected
     */
    private void launchActionDialog(final long itemID) {
        // Prepare dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);

        // Add decoration
        builder.setTitle(R.string.dialog_context_action);
        builder.setItems(R.array.dialog_available_actions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // Delete entry from list
                        deleteEntry(itemID);
                        break;
                    case 1:
                        // Delete all entries from list
                        deleteAllEntries();
                        break;
                }
            }
        });

        // Neutral button: propose next time
        builder.setNeutralButton(R.string.dialog_undo_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    /**
     * Method used to delete all entries from list.
     */
    private void deleteAllEntries() {
        ContentValues mUpdateValues = new ContentValues();
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
    }

    /**
     * Method used to delete an entry from database.
     * @param itemID the ID of item selected
     */
    private void deleteEntry(long itemID) {
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

    /**
     * Method used to remove an entry from Cart database.
     * @param comicId the ID of item selected
     */
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

    /**
     * Method used to remove all entries from cart database.
     * @return the number of comic removed
     */
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

    /**
     * Method used to fill data on list.
     */
    private void fillData() {
        CCLogger.d(TAG, "fillData - start");

        getLoaderManager().initLoader(0, null, this);

        CCLogger.d(TAG, "fillData - end");
    }

    /* ****************************************************************************************
     * LoaderManager methods
     ******************************************************************************************/

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Data is not available anymore, delete reference
        mAdapter.swapCursor(null);
    }

}
