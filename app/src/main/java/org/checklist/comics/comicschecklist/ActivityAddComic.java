package org.checklist.comics.comicschecklist;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ActivityAddComic extends AppCompatActivity {

    private static final String TAG = ActivityAddComic.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate - start");
        setContentView(R.layout.activity_comic_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(FragmentAddComic.ARG_COMIC_ID, getIntent().getLongExtra(FragmentAddComic.ARG_COMIC_ID, -1));
            FragmentAddComic fragment = new FragmentAddComic();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().add(R.id.comic_detail_container, fragment, "addComicFragment").commit();
        }

        Log.v(TAG, "onCreate - end");
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

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Set chosen date to text view
            FragmentAddComic articleFrag = (FragmentAddComic)
                    getActivity().getSupportFragmentManager().findFragmentByTag("addComicFragment");
            month++;
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String date = simpleDateFormat.format(calendar.getTime());
            articleFrag.updateDate(date);
        }
    }
}
