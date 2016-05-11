package org.checklist.comics.comicschecklist.util;

import org.checklist.comics.comicschecklist.ActivityMain;
import org.checklist.comics.comicschecklist.ComicsChecklistDialogFragment;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Classe che lancia la finestra di dialogo per invitare l'utente a recensire l'app.
 * @author Francesco Bevilacqua
 */
public class AppRater {

    /**
     * Metodo che conta quante volte è stata lanciata l'app ed i giorni passati dall'installazione.
     */
    public static void app_launched (Context mContext) {
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
                FragmentManager fm = ((ActivityMain) mContext).getFragmentManager();
                DialogFragment rateDialog = ComicsChecklistDialogFragment.newInstance(Constants.DIALOG_RATE);
                rateDialog.show(fm, "ComicsChecklistDialogFragment");
            }
        }

        editor.apply();
    }
}
