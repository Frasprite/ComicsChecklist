package org.checklist.comics.comicschecklist.util;

import org.checklist.comics.comicschecklist.R;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

/**
 * Classe che lancia la finestra di dialogo per invitare l'utente a recensire l'app.
 * @author Francesco Bevilacqua
 */
public class AppRater {

    /**
     * Metodo che conta quante volte è stata lanciata l'app ed i giorni passati dall'installazione.
     */
    public static void app_launched (final Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences (Constants.PREF_APP_RATER, 0);
        if (prefs.getBoolean (Constants.PREF_USER_DONT_RATE, false)) {
            return ;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Incremento il contatore dei lanci
        long launch_count = prefs.getLong(Constants.PREF_LAUNCH_COUNT, 0) + 1;
        editor.putLong(Constants.PREF_LAUNCH_COUNT, launch_count);

        // Prendo la data del primo lancio dell'applicazione
        Long date_firstLaunch = prefs.getLong(Constants.PREF_DATE_FIRST_LAUNCH, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(Constants.PREF_DATE_FIRST_LAUNCH, date_firstLaunch);
        }

        // Aspetto almeno 7 giorni prima di aprire la finestra
        if (launch_count >= Constants.LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (Constants.DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {

                // Prepare dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);

                // Positive button: launch Google Play page
                builder.setPositiveButton(R.string.dialog_rate_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + mContext.getPackageName())));
                    }
                });

                // Negative button: avoid opening rate again
                builder.setNegativeButton(R.string.dialog_no_thanks_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREF_APP_RATER, 0);
                        final SharedPreferences.Editor editorPref = prefs.edit();
                        if (editorPref != null) {
                            editorPref.putBoolean(Constants.PREF_USER_DONT_RATE, true);
                            editorPref.apply();
                        }
                        dialog.dismiss();
                    }
                });

                // Neutral button: propose next time
                builder.setNeutralButton(R.string.dialog_late_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                // Add decoration
                builder.setTitle(R.string.app_name);
                builder.setMessage(R.string.dialog_rate_text);
                builder.show();
            }
        }

        editor.apply();
    }
}
