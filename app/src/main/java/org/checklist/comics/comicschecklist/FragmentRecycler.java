package org.checklist.comics.comicschecklist;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

public class FragmentRecycler extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = FragmentRecycler.class.getSimpleName();

    private RecyclerView mRecyclerView;

    private Constants.Sections mEditor;
    private CustomCursorRecyclerViewAdapter mAdapter;

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

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });

        getActivity().getSupportLoaderManager().restartLoader(0, null, this);

        return view;
    }

    /**
     * Method used to fill data on list.
     */
    private void fillData() {
        CCLogger.d(TAG, "fillData - start");

        getLoaderManager().initLoader(0, null, this);

        CCLogger.d(TAG, "fillData - end");
    }

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
