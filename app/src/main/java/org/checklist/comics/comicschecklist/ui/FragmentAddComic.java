package org.checklist.comics.comicschecklist.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.DateCreator;

/**
 * Fragment which manage the comic creation.
 */
public class FragmentAddComic extends Fragment {

    private static final String TAG = FragmentAddComic.class.getSimpleName();

    private long mComicId = -1;

    private EditText mNameEditText;
    private EditText mInfoEditText;
    private TextView mDateTextView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FragmentAddComic() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CCLogger.d(TAG, "onCreateView - start");
        View rootView = inflater.inflate(R.layout.fragment_add_comic, container, false);

        mNameEditText = rootView.findViewById(R.id.name_edit_text);
        mInfoEditText = rootView.findViewById(R.id.info_edit_text);
        mDateTextView = rootView.findViewById(R.id.date_text_view);

        CCLogger.v(TAG, "onCreateView - end");
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CCLogger.d(TAG, "onActivityCreated - start");

        if (getArguments().containsKey(Constants.ARG_COMIC_ID)) {
            // Load comic content specified by the fragment arguments from ComicContentProvider.
            mComicId = getArguments().getLong(Constants.ARG_COMIC_ID, -1);
            CCLogger.d(TAG, "onActivityCreated - mComicId (initiated from ARGUMENTS) = " + mComicId);
        }

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mComicId = savedInstanceState.getLong(Constants.ARG_SAVED_COMIC_ID, -1);
            CCLogger.d(TAG, "onActivityCreated - mComicId (initiated from BUNDLE) = " + mComicId);
        }

        CCLogger.v(TAG, "onActivityCreated - end");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load data from database if ID is passed from activity
        if (mComicId > -1) {
            Uri uri = Uri.parse(ComicContentProvider.CONTENT_URI + "/" + mComicId);
            String[] projection = {ComicDatabase.ID, ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_RELEASE_KEY,
                    ComicDatabase.COMICS_DATE_KEY, ComicDatabase.COMICS_DESCRIPTION_KEY, ComicDatabase.COMICS_PRICE_KEY,
                    ComicDatabase.COMICS_FEATURE_KEY, ComicDatabase.COMICS_COVER_KEY, ComicDatabase.COMICS_EDITOR_KEY,
                    ComicDatabase.COMICS_FAVORITE_KEY, ComicDatabase.COMICS_CART_KEY};
            Cursor mCursor = ComicDatabaseManager.query(getActivity(), uri, projection, null, null, null);
            mCursor.moveToFirst();
            mNameEditText.setText(mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_NAME_KEY)));
            mInfoEditText.setText(mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_DESCRIPTION_KEY)));
            mDateTextView.setText(mCursor.getString(mCursor.getColumnIndex(ComicDatabase.COMICS_RELEASE_KEY)));
            mCursor.close();
        } else {
            // Leave all form in blank and set default date
            updateDate(DateCreator.getTodayString());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CCLogger.d(TAG, "onSaveInstanceState - saving mComicId " + mComicId);
        outState.putLong(Constants.ARG_SAVED_COMIC_ID, mComicId);
    }

    @Override
    public void onPause() {
        String name = mNameEditText.getText().toString();
        String info = mInfoEditText.getText().toString();
        String date = mDateTextView.getText().toString();
        if (info.length() > 0) {
            // Save data if there is at least some info and put a default title
            if (name.length() == 0) {
                name = getString(R.string.text_default_title);
            }

            if (mComicId == -1) {
                // Insert new entry
                mComicId = ComicDatabaseManager.insert(
                        getActivity(),
                        name,
                        Constants.Sections.getName(Constants.Sections.CART),
                        info,
                        date,
                        DateCreator.elaborateDate(date),
                        "error", "N.D.", "N.D.", "yes", "no", "");
                CCLogger.d(TAG, "INSERTED new entry on database with ID " + mComicId);
            } else {
                // Update entry
                ContentValues mUpdateValues = new ContentValues();
                mUpdateValues.put(ComicDatabase.COMICS_NAME_KEY, name);
                mUpdateValues.put(ComicDatabase.COMICS_DESCRIPTION_KEY, info);
                mUpdateValues.put(ComicDatabase.COMICS_DATE_KEY, DateCreator.elaborateDate(date).getTime());
                mUpdateValues.put(ComicDatabase.COMICS_RELEASE_KEY, date);
                // Defines selection criteria for the rows you want to update
                String mSelectionClause = ComicDatabase.ID +  "=?";
                String[] mSelectionArgs = new String[]{String.valueOf(mComicId)};
                int rowUpdated = ComicDatabaseManager.update(getActivity(), mUpdateValues, mSelectionClause, mSelectionArgs);
                if (rowUpdated > 0) {
                    CCLogger.d(TAG, "UPDATED entry on database with ID " + mComicId);
                } else {
                    CCLogger.w(TAG, "UPDATE failed for entry with ID " + mComicId);
                }
            }

            // Update widget
            WidgetService.updateWidget(getActivity());
        }
        super.onPause();
    }

    public void updateDate(String date) {
        if (mDateTextView != null) {
            mDateTextView.setText(date);
        }
    }
}