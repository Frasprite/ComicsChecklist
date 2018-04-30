package org.checklist.comics.comicschecklist.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;

import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.DateCreator;

/**
 * Activity used to add a comic on database.
 */
public class ActivityAddComic extends AppCompatActivity {

    private static final String TAG = ActivityAddComic.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_detail);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Only for Lollipop and newer versions
            Window window = this.getWindow();

            // Clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // Add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            // Finally change the color
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_dark));
        }

        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // Create the detail fragment and add it to the activity using a fragment transaction.
        Bundle arguments = new Bundle();
        arguments.putLong(Constants.ARG_COMIC_ID, getIntent().getLongExtra(Constants.ARG_COMIC_ID, -1));
        FragmentAddComic fragment = new FragmentAddComic();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction().add(R.id.comic_detail_container, fragment, "addComicFragment").commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, ActivityMain.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void changeDate(View view) {
        switch (view.getId()) {
            case R.id.button_change_data:
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
                break;
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            int year = DateCreator.getCurrentYear();
            int month = DateCreator.getCurrentMonth();
            int day = DateCreator.getCurrentDay();

            // Create a new instance of DatePickerDialog and return it
            CCLogger.d(TAG, "onCreateDialog - date is " + day + "/" + month + "/" + year);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Set chosen date to text view
            FragmentAddComic articleFrag = (FragmentAddComic)
                    getActivity().getSupportFragmentManager().findFragmentByTag("addComicFragment");
            String date = DateCreator.elaborateDate(year, month, day);
            CCLogger.i(TAG, "onDateSet - returning " + date);
            articleFrag.updateDate(date);
        }
    }
}
