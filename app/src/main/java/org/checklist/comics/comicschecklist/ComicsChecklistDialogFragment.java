package org.checklist.comics.comicschecklist;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

/**
 * Created by Francesco Bevilacqua on 16/10/2014.
 * This code is part of ComicsChecklist project.
 */
public class ComicsChecklistDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface ComicsChecklistDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, String name, String info, String date);
        public void onDialogNegativeClick(DialogFragment dialog);
        public void onDialogRateClick(DialogFragment dialog);
        public void onDialogAbortRateClick(DialogFragment dialog);
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

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // We have 4 type of dialog on CC: 0 help, 1 guide, 2 add and 3 rater
        int type = getArguments().getInt("type");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        switch (type) {
            case 0:
                // Get the layout inflater
                LayoutInflater inflaterHelp = getActivity().getLayoutInflater();
                // Inflate and set the layout for the dialog; pass null as the parent view because its going in the dialog layout
                builder.setView(inflaterHelp.inflate(R.layout.dialog_help, null));
                // Set title
                builder.setTitle(R.string.guida);
                break;
            case 1:
                // Get the layout inflater
                LayoutInflater inflaterInfo = getActivity().getLayoutInflater();
                // Inflate and set the layout for the dialog; pass null as the parent view because its going in the dialog layout
                builder.setView(inflaterInfo.inflate(R.layout.dialog_info, null));
                // Set title
                builder.setTitle(R.string.info);
                break;
            case 2:
                // Get the layout inflater
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_add_comic, null);

                mNameEditText = (EditText) view.findViewById(R.id.name_edit_text);
                mInfoEditText = (EditText) view.findViewById(R.id.info_edit_text);
                mDatePicker = (DatePicker) view.findViewById(R.id.date_picker);

                // Inflate and set the layout for the dialog; pass null as the parent view because its going in the dialog layout
                builder.setView(view)
                        // Add action buttons
                        .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // Send the positive button event back to the host activity
                                String name = mNameEditText.getText().toString();
                                String info = mInfoEditText.getText().toString();
                                String date = mDatePicker.getDayOfMonth() + "/" + mDatePicker.getMonth() + "/" + mDatePicker.getYear();
                                mListener.onDialogPositiveClick(ComicsChecklistDialogFragment.this, name, info, date);
                            }
                        })
                        .setNegativeButton(R.string.dialog_undo, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Send the negative button event back to the host activity
                                mListener.onDialogNegativeClick(ComicsChecklistDialogFragment.this);
                            }
                        });

                // Set title
                builder.setTitle(R.string.title_section2);
                break;
            case 3:

                // Launch Google Play page
                builder.setPositiveButton(R.string.rate_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogRateClick(ComicsChecklistDialogFragment.this);
                    }
                });
                builder.setNegativeButton(R.string.no_thanks_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogAbortRateClick(ComicsChecklistDialogFragment.this);
                    }
                });
                builder.setNeutralButton(R.string.late_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(ComicsChecklistDialogFragment.this);
                    }
                });

                // Add decoration
                builder.setTitle(R.string.app_name);
                builder.setMessage(R.string.rate_text);
                break;
            default:
                return null;
        }

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
