package org.checklist.comics.comicschecklist.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.receiver.AlarmReceiver;
import org.checklist.comics.comicschecklist.util.CCLogger;
import org.checklist.comics.comicschecklist.util.CCNotificationManager;
import org.checklist.comics.comicschecklist.util.DateCreator;

/**
 * Class which fire a Notification for incoming comic.
 */
public class AlarmService extends IntentService {

    private static final String TAG = AlarmService.class.getSimpleName();

    public AlarmService() {
        super("AlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        CCLogger.d(TAG, "onHandleIntent - start");
        // Do the work that requires your app to keep the CPU running.
        String[] columns = new String[]{ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_EDITOR_KEY, ComicDatabase.COMICS_RELEASE_KEY};
        String selection = ComicDatabase.COMICS_RELEASE_KEY + " LIKE ? AND " + ComicDatabase.COMICS_FAVORITE_KEY + " LIKE ?";
        String[] selectionArguments = new String[]{DateCreator.getTodayString(), "yes"};
        String order = ComicDatabase.COMICS_DATE_KEY + " " + "ASC";
        Cursor cursor = ComicDatabaseManager.query(this, ComicContentProvider.CONTENT_URI, columns, selection,
                selectionArguments, order);
        if (cursor != null) {
            if (cursor.getCount() == 1) {
                CCLogger.i(TAG, "onHandleIntent - Favorite comic found");
                CCNotificationManager.createNotification(this, getResources().getString(R.string.notification_comic_out), false);
            } else if (cursor.getCount() > 1) {
                CCLogger.i(TAG, "onHandleIntent - Founded more favorite comics");
                String text = getResources().getString(R.string.notification_comics_out_1) + " " + cursor.getCount() + " " +
                                getResources().getString(R.string.notification_comics_out_2);
                CCNotificationManager.createNotification(this, text, false);
            }
            cursor.close();
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        AlarmReceiver.completeWakefulIntent(intent);
    }
}
