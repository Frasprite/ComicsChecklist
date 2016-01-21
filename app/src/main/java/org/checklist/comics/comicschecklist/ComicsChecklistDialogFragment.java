package org.checklist.comics.comicschecklist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.database.ComicDatabaseHelper;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.util.Constants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Francesco Bevilacqua on 16/10/2014.
 * This code is part of ComicsChecklist project.
 */
public class ComicsChecklistDialogFragment extends DialogFragment {

    private static final String TAG = ComicsChecklistDialogFragment.class.getSimpleName();

    /**
     * The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it.
     **/
    public interface ComicsChecklistDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, int dialogId);
        void onDialogNegativeClick(DialogFragment dialog, int dialogId);
        void onDialogNeutralClick(DialogFragment dialog, int dialogId);
        void onDialogListItemClick(DialogFragment dialog, int dialogId, long id, String search);
    }

    // Use this instance of the interface to deliver action events
    ComicsChecklistDialogListener mListener;
    private EditText mNameEditText;
    private EditText mInfoEditText;
    private DatePicker mDatePicker;

    public static ComicsChecklistDialogFragment newInstance(int type) {
        ComicsChecklistDialogFragment frag = new ComicsChecklistDialogFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        frag.setArguments(args);
        return frag;
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ComicsChecklistDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ComicsChecklistDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // We have 4 type of dialog on CC: 0 help, 1 guide, 2 add, 3 rater, 4 search result
        int type = getArguments().getInt("type");
        Log.d(TAG, "Creating " + type + "(0 help, 1 guide, 2 add, 3 rater, 4 search result)");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        switch (type) {
            case Constants.DIALOG_GUIDE:
                builder.setNegativeButton(R.string.dialog_confirm_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClick(ComicsChecklistDialogFragment.this, Constants.DIALOG_GUIDE);
                    }
                });
                // Get the layout inflater
                LayoutInflater inflaterHelp = getActivity().getLayoutInflater();
                // Inflate and set the layout for the dialog; pass null as the parent view because its going in the dialog layout
                builder.setView(inflaterHelp.inflate(R.layout.dialog_help, null));
                // Set title
                builder.setTitle(R.string.dialog_help_title);
                break;
            case Constants.DIALOG_INFO:
                builder.setNegativeButton(R.string.dialog_confirm_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClick(ComicsChecklistDialogFragment.this, Constants.DIALOG_INFO);
                    }
                });
                // Get the layout inflater
                LayoutInflater inflaterInfo = getActivity().getLayoutInflater();
                // Inflate and set the layout for the dialog; pass null as the parent view because its going in the dialog layout
                builder.setView(inflaterInfo.inflate(R.layout.dialog_info, null));
                // Set title
                builder.setTitle(R.string.dialog_info_title);
                break;
            case Constants.DIALOG_ADD_COMIC:
                // Get the layout inflater
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_add_comic, null);

                mNameEditText = (EditText) view.findViewById(R.id.name_edit_text);
                mInfoEditText = (EditText) view.findViewById(R.id.info_edit_text);
                mDatePicker = (DatePicker) view.findViewById(R.id.date_picker);

                // Inflate and set the layout for the dialog; pass null as the parent view because its going in the dialog layout
                builder.setView(view)
                        // Add action buttons
                        .setPositiveButton(R.string.dialog_confirm_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // Save new data and the positive button event back to the host activity
                                String name = mNameEditText.getText().toString();
                                String info = mInfoEditText.getText().toString();
                                String date = mDatePicker.getDayOfMonth() + "/" + mDatePicker.getMonth() + "/" + mDatePicker.getYear();
                                if (name.length() > 0) {
                                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                    Date myDate;
                                    try {
                                        myDate = formatter.parse(date);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        myDate = new Date();
                                    }
                                    // Set the format to sql date time
                                    ContentValues values = new ContentValues();
                                    values.put(ComicDatabase.COMICS_NAME_KEY, name);
                                    values.put(ComicDatabase.COMICS_EDITOR_KEY, Constants.Editors.CART.name());
                                    values.put(ComicDatabase.COMICS_DESCRIPTION_KEY, info);
                                    values.put(ComicDatabase.COMICS_RELEASE_KEY, date);
                                    values.put(ComicDatabase.COMICS_DATE_KEY, myDate.getTime());
                                    values.put(ComicDatabase.COMICS_COVER_KEY, "error");
                                    values.put(ComicDatabase.COMICS_FEATURE_KEY, "N.D.");
                                    values.put(ComicDatabase.COMICS_PRICE_KEY, "N.D.");
                                    values.put(ComicDatabase.COMICS_CART_KEY, "yes");
                                    values.put(ComicDatabase.COMICS_FAVORITE_KEY, "no");

                                    getActivity().getContentResolver().insert(ComicContentProvider.CONTENT_URI, values);
                                    mListener.onDialogPositiveClick(ComicsChecklistDialogFragment.this, Constants.DIALOG_ADD_COMIC);
                                } else {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_fill_data_alert), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.dialog_undo_button, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Send the negative button event back to the host activity
                                mListener.onDialogNegativeClick(ComicsChecklistDialogFragment.this, Constants.DIALOG_ADD_COMIC);
                            }
                        });

                // Set title
                builder.setTitle(R.string.title_section2);
                break;
            case Constants.DIALOG_RATE:
                // Launch Google Play page
                builder.setPositiveButton(R.string.dialog_rate_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(ComicsChecklistDialogFragment.this, Constants.DIALOG_RATE);
                    }
                });
                builder.setNegativeButton(R.string.dialog_no_thanks_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(ComicsChecklistDialogFragment.this, Constants.DIALOG_RATE);
                    }
                });
                builder.setNeutralButton(R.string.dialog_late_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNeutralClick(ComicsChecklistDialogFragment.this, Constants.DIALOG_RATE);
                    }
                });

                // Add decoration
                builder.setTitle(R.string.app_name);
                builder.setMessage(R.string.dialog_rate_text);
                break;
            case Constants.DIALOG_RESULT_LIST:
                // Launch a dialog with a list
                builder.setNegativeButton(R.string.dialog_undo_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClick(ComicsChecklistDialogFragment.this, Constants.DIALOG_RESULT_LIST);
                    }
                });

                LayoutInflater listInflater = getActivity().getLayoutInflater();
                View listView = listInflater.inflate(R.layout.dialog_search_list, null);

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String mQuery = sp.getString(Constants.PREF_SEARCH_QUERY, "error");
                final Cursor cursor = getActivity().getContentResolver().query(ComicContentProvider.CONTENT_URI, null, ComicDatabase.COMICS_NAME_KEY + " LIKE ?",
                        new String[] {"%" + mQuery + "%"}, null);
                final ListView mList = (ListView)listView.findViewById(R.id.searchListView);

                // Fields from the database (projection) must include the id column for the adapter to work
                String[] from = new String[] {ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_RELEASE_KEY};
                // Fields on the UI to which we map
                int[] to = new int[] {android.R.id.text1, android.R.id.text2};

                SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_activated_2, cursor, from, to, 0);
                mList.setAdapter(adapter);
                mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView tv = (TextView) view.findViewById(android.R.id.text1);
                        String name = tv.getText().toString();
                        ComicDatabaseHelper database = new ComicDatabaseHelper(getActivity());
                        // Using SQLiteQueryBuilder instead of query() method
                        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
                        // Set the table
                        queryBuilder.setTables(ComicDatabase.COMICS_TABLE);
                        // Adding name and release to the original query
                        queryBuilder.appendWhere(ComicDatabase.COMICS_NAME_KEY + "='" + name + "'");
                        SQLiteDatabase db = database.getWritableDatabase();
                        Cursor comicCursor = queryBuilder.query(db, null, null, null, null, null, null);

                        if (comicCursor != null) {
                            comicCursor.moveToFirst();
                            mListener.onDialogListItemClick(ComicsChecklistDialogFragment.this, Constants.DIALOG_RESULT_LIST,
                                                            comicCursor.getLong(comicCursor.getColumnIndex(ComicDatabase.ID)), "search");
                            comicCursor.close();
                            cursor.close();
                        } else
                            Toast.makeText(getActivity(), getResources().getText(R.string.search_error), Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setTitle(R.string.search_result).setView(listView);

                break;
            default:
                return null;
        }

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
