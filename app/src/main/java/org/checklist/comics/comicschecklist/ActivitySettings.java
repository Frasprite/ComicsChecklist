package org.checklist.comics.comicschecklist;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.receiver.AlarmReceiver;
import org.checklist.comics.comicschecklist.receiver.BootReceiver;
import org.checklist.comics.comicschecklist.util.Constants;

import java.util.Calendar;
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
            Log.d(TAG, "onPreferenceChange - preference " + preference.getKey() + " value " + stringValue);

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
                    Context mContext = getActivity();
                    if (boolValue) {
                        Log.d(TAG, "Activate notification for favorite");
                        // Activate notification for favorite
                        AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                        Intent mIntentReceiver = new Intent(mContext, AlarmReceiver.class);
                        PendingIntent alarmIntent = PendingIntent.getBroadcast(mContext, 0, mIntentReceiver, 0);

                        // Set the alarm to start at 10:00 a.m.
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        calendar.set(Calendar.HOUR_OF_DAY, 10);

                        // Specify a non-precise custom interval, in this case every days.
                        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                                AlarmManager.INTERVAL_DAY, alarmIntent);

                        // Enabling also receiver on boot
                        ComponentName receiver = new ComponentName(mContext, BootReceiver.class);
                        PackageManager pm = mContext.getPackageManager();

                        pm.setComponentEnabledSetting(receiver,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);
                    } else {
                        Log.d(TAG, "Disable notification for favorite");
                        // Disable notification for favorite
                        AlarmManager alarmMgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                        Intent mIntentReceiver = new Intent(mContext, AlarmReceiver.class);
                        PendingIntent alarmIntent = PendingIntent.getBroadcast(mContext, 0, mIntentReceiver, 0);
                        if (alarmMgr != null) {
                            alarmMgr.cancel(alarmIntent);
                        }

                        // Disable also receiver on boot
                        ComponentName receiver = new ComponentName(mContext, BootReceiver.class);
                        PackageManager pm = mContext.getPackageManager();

                        pm.setComponentEnabledSetting(receiver,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP);
                    }
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                //startActivity(new Intent(getActivity(), ActivitySettings.class));
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
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                //startActivity(new Intent(getActivity(), ActivitySettings.class));
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
            bindPreferenceSummaryToValue(findPreference(Constants.PREF_LIST_ORDER));
            MultiSelectListPreference multiSelectListPreference = (MultiSelectListPreference) findPreference(Constants.PREF_AVAILABLE_EDITORS);
            multiSelectListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    Log.d(TAG, "onPreferenceChange - preference " + preference.getKey() + " value " + value.toString());
                    return true;
                }
            });

            Preference preference = findPreference(Constants.PREF_DELETE_CONTENT);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    Log.d(TAG, "onPreferenceClick - preference " + preference.getKey());
                    launchDeleteContentDialog(preference.getContext());
                    return true;
                }
            });
        }

        private void launchDeleteContentDialog(final Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);//, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(R.string.dialog_delete_content_title)
                    .setItems(R.array.pref_available_editors, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position of the selected item
                            Constants.Sections section;
                            switch (which) {
                                case 0:
                                    // Delete Marvel content
                                    section = Constants.Sections.MARVEL;
                                    break;
                                case 1:
                                    // Delete Panini comics content
                                    section = Constants.Sections.PANINI;
                                    break;
                                case 2:
                                    // Delete Planet Manga content
                                    section = Constants.Sections.PLANET;
                                    break;
                                case 3:
                                    // Delete Star Comics content
                                    section = Constants.Sections.STAR;
                                    break;
                                case 4:
                                    // Delete SB content
                                    section = Constants.Sections.BONELLI;
                                    break;
                                case 5:
                                    // Delete RW content
                                    section = Constants.Sections.RW;
                                    break;
                                default:
                                    section = null;
                            }
                            if (section != null) {
                                Log.v(TAG, "Selected item on position " + which + " - obtaining section " + section);
                                String selection = ComicDatabase.COMICS_EDITOR_KEY + " =? AND " +
                                                    ComicDatabase.COMICS_CART_KEY + " =? AND " +
                                                    ComicDatabase.COMICS_FAVORITE_KEY + " =?";
                                String[] selectionArgs = new String[]{section.getName(), "no", "no"};
                                int result = ComicDatabaseManager.delete(context, ComicContentProvider.CONTENT_URI, selection, selectionArgs);
                                Log.d(TAG, "Deleted " + result + " entries of " + section + " section");
                            } else {
                                Log.w(TAG, "No section found with index " + which);
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
                //startActivity(new Intent(getActivity(), ActivitySettings.class));
                getActivity().onBackPressed();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
