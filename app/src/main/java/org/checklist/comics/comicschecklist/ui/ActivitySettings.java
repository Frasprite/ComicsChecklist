package org.checklist.comics.comicschecklist.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.evernote.android.job.JobManager;

import org.checklist.comics.comicschecklist.CCApp;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.notification.ComicReleaseSyncJob;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.DateCreator;

import java.util.List;

/**
 * A {@link android.preference.PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class ActivitySettings extends AppCompatPreferenceActivity {

    private static final String TAG = ActivitySettings.class.getSimpleName();

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            CCLogger.d(TAG, "onPreferenceChange - preference " + preference.getKey() + " value " + stringValue);

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //startActivity(new Intent(getActivity(), ActivitySettings.class));
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || DataFilterPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class NotificationPreferenceFragment extends PreferenceFragment {

        private static final String TAG = NotificationPreferenceFragment.class.getSimpleName();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);
            findPreference(Constants.PREF_FAVORITE_NOTIFICATION).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean boolValue = (boolean) newValue;
                    if (boolValue) {
                        CCLogger.d(TAG, "onCreate - Activate notification for favorite");
                        ComicReleaseSyncJob.scheduleJob();
                    } else {
                        CCLogger.d(TAG, "onCreate - Disable notification for favorite");
                        JobManager mJobManager = JobManager.instance();
                        mJobManager.cancelAll();
                    }
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().onBackPressed();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class DataSyncPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(Constants.PREF_SYNC_FREQUENCY));
            bindPreferenceSummaryToValue(findPreference(Constants.PREF_DELETE_FREQUENCY));

            Preference preference = findPreference(Constants.PREF_LAST_SYNC);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    CCLogger.d(TAG, "onPreferenceClick - preference " + preference.getKey());
                    launchLastSyncDialog(preference.getContext());
                    return true;
                }
            });
        }

        private void launchLastSyncDialog(final Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.dialog_last_sync_title)
                    .setMessage(composeLastSyncMessage(context))
                    .setPositiveButton(R.string.dialog_confirm_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builder.create().show();
        }

        private String composeLastSyncMessage(Context context) {
            String message;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            message = "Panini Comics : " + DateCreator.elaborateHumanDate(sp.getString(Constants.PREF_PANINI_LAST_SCAN, "01/01/2012")) + "\n";
            message = message + "RW Edizioni : " + DateCreator.elaborateHumanDate(sp.getString(Constants.PREF_RW_LAST_SCAN, "01/01/2012")) + "\n";
            message = message + "Bonelli : " + DateCreator.elaborateHumanDate(sp.getString(Constants.PREF_BONELLI_LAST_SCAN, "01/01/2012")) + "\n";
            message = message + "Star Comics : " + DateCreator.elaborateHumanDate(sp.getString(Constants.PREF_STAR_LAST_SCAN, "01/01/2012"));
            return message;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().onBackPressed();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class DataFilterPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_filter);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            MultiSelectListPreference multiSelectListPreference = (MultiSelectListPreference) findPreference(Constants.PREF_AVAILABLE_EDITORS);
            multiSelectListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    CCLogger.d(TAG, "onPreferenceChange - preference " + preference.getKey() + " value " + value.toString());
                    return true;
                }
            });

            Preference preference = findPreference(Constants.PREF_DELETE_CONTENT);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    CCLogger.d(TAG, "onPreferenceClick - preference " + preference.getKey());
                    launchDeleteContentDialog(preference.getContext());
                    return true;
                }
            });
        }

        private void launchDeleteContentDialog(final Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.dialog_delete_content_title)
                    .setItems(R.array.pref_available_editors, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position of the selected item
                            Constants.Sections section;
                            switch (which) {
                                case 0:
                                    // Delete Panini comics content
                                    section = Constants.Sections.PANINI;
                                    break;
                                case 1:
                                    // Delete Star Comics content
                                    section = Constants.Sections.STAR;
                                    break;
                                case 2:
                                    // Delete SB content
                                    section = Constants.Sections.BONELLI;
                                    break;
                                case 3:
                                    // Delete RW content
                                    section = Constants.Sections.RW;
                                    break;
                                default:
                                    section = null;
                            }
                            if (section != null) {
                                CCLogger.v(TAG, "Selected item on position " + which + " - obtaining section " + section);
                                deleteComics(context, section);
                            } else {
                                CCLogger.w(TAG, "No section found with index " + which);
                                dialog.dismiss();
                            }
                        }
                    });
            builder.create().show();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().onBackPressed();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void deleteComics(Context context, Constants.Sections section) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    int rowsDeleted = ((CCApp) context.getApplicationContext()).getRepository().deleteComics(section.getName());
                    CCLogger.d(TAG, "deleteOldRows - Entries deleted: " + rowsDeleted + " with given section " + section.getName());
                    if (rowsDeleted > 0) {
                        // Update widgets as well
                        WidgetService.updateWidget(context);
                    }
                }
            });
        }
    }
}
