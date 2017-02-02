package org.checklist.comics.comicschecklist;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.checklist.comics.comicschecklist.adapter.CustomCursorRecyclerViewAdapter;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.util.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.RecyclerItemClickListener;

public class FragmentRecycler extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // TODO highlight selected item
    // http://stackoverflow.com/questions/27194044/how-to-properly-highlight-selected-item-on-recyclerview
    // http://stackoverflow.com/questions/26682277/how-do-i-get-the-position-selected-in-a-recyclerview
    // TODO add expanded FAB menu
    // http://stackoverflow.com/questions/30699302/android-design-support-library-fab-menu
    // https://github.com/pmahsky/FloatingActionMenuAndroid

    private static final String TAG = FragmentRecycler.class.getSimpleName();

    private Constants.Sections mEditor;
    private CustomCursorRecyclerViewAdapter mAdapter;

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

        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
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

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), mRecyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                long id = mAdapter.getItemId(position);
                                CCLogger.d(TAG, "onItemClick - item ID " + id);
                                // Notify the active callbacks interface (the activity, if the
                                // fragment is attached to one) that an item has been selected.
                                mCallbacks.onItemSelected(id);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                CCLogger.d(TAG, "onLongItemClick");
                            }
                        })
        );

        getActivity().getSupportLoaderManager().restartLoader(0, null, this);

        return view;
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
