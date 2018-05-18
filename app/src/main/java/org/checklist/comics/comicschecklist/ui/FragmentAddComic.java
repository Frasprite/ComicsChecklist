package org.checklist.comics.comicschecklist.ui;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.checklist.comics.comicschecklist.CCApp;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.DateCreator;

import java.util.concurrent.Callable;

/**
 * Fragment which manage the comic creation.
 */
public class FragmentAddComic extends Fragment {

    private static final String TAG = FragmentAddComic.class.getSimpleName();

    private int mComicId = -1;

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
            mComicId = getArguments().getInt(Constants.ARG_COMIC_ID, -1);
            CCLogger.d(TAG, "onActivityCreated - mComicId (initiated from ARGUMENTS) = " + mComicId);
        }

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mComicId = savedInstanceState.getInt(Constants.ARG_SAVED_COMIC_ID, -1);
            CCLogger.d(TAG, "onActivityCreated - mComicId (initiated from BUNDLE) = " + mComicId);
        }

        CCLogger.v(TAG, "onActivityCreated - end");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load data from database if ID is passed from activity
        if (mComicId > -1) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    LiveData<ComicEntity> liveData = ((CCApp) getActivity().getApplication()).getRepository().loadComic(mComicId);
                    ComicEntity comicEntity = liveData.getValue();
                    CCLogger.d(TAG, "loadComicWithID - Comic : " + comicEntity);

                    mNameEditText.setText(comicEntity.getName());
                    mInfoEditText.setText(comicEntity.getDescription());
                    mDateTextView.setText(DateCreator.elaborateDate(comicEntity.getReleaseDate()));
                }
            });
        } else {
            // Leave all form in blank and set default date
            updateDate(DateCreator.getTodayString());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CCLogger.d(TAG, "onSaveInstanceState - saving mComicId " + mComicId);
        outState.putInt(Constants.ARG_SAVED_COMIC_ID, mComicId);
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

            // Insert new entry
            ComicEntity comicEntity = new ComicEntity(name,
                    DateCreator.elaborateDate(date),
                    info,
                    "N.D.",
                    "N.D.",
                    "N.D.",
                    Constants.Sections.getName(Constants.Sections.CART),
                    false, false,
                    "");

            if (mComicId == -1) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        mComicId = (int)((CCApp) getActivity().getApplication()).getRepository().insertComic(comicEntity);
                        CCLogger.d(TAG, "onPause - INSERTED new entry on database with ID " + mComicId);
                    }
                });
            } else {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        ((CCApp) getActivity().getApplication()).getRepository().updateComic(comicEntity);
                        CCLogger.d(TAG, "onPause - UPDATED entry on database with ID " + mComicId);
                    }
                });
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
