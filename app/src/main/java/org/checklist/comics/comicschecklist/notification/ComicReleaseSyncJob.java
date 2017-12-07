package org.checklist.comics.comicschecklist.notification;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;

import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.util.CCLogger;
import org.checklist.comics.comicschecklist.util.CCNotificationManager;
import org.checklist.comics.comicschecklist.util.DateCreator;

import java.util.concurrent.TimeUnit;

/**
 * Class which manage the scheduling of jobs.
 */
public class ComicReleaseSyncJob extends DailyJob {

    public static final String TAG = "ComicReleaseSyncJob";

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {
        // Run your job here
        CCLogger.d(TAG, "onRunJob - start");
        // Do the work that requires your app to keep the CPU running.
        String[] columns = new String[]{ComicDatabase.COMICS_NAME_KEY, ComicDatabase.COMICS_EDITOR_KEY, ComicDatabase.COMICS_RELEASE_KEY};
        String selection = ComicDatabase.COMICS_RELEASE_KEY + " LIKE ? AND " + ComicDatabase.COMICS_FAVORITE_KEY + " LIKE ?";
        String[] selectionArguments = new String[]{DateCreator.getTodayString(), "yes"};
        String order = ComicDatabase.COMICS_DATE_KEY + " " + "ASC";
        Cursor cursor = ComicDatabaseManager.query(getContext(), ComicContentProvider.CONTENT_URI, columns, selection,
                selectionArguments, order);
        if (cursor != null) {
            if (cursor.getCount() == 1) {
                CCLogger.i(TAG, "onRunJob - Favorite comic found");
                CCNotificationManager.createNotification(getContext(), getContext().getResources().getString(R.string.notification_comic_out), false);
            } else if (cursor.getCount() > 1) {
                CCLogger.i(TAG, "onRunJob - Founded more favorite comics");
                String text = getContext().getResources().getString(R.string.notification_comics_out_1) + " " + cursor.getCount() + " " +
                        getContext().getResources().getString(R.string.notification_comics_out_2);
                CCNotificationManager.createNotification(getContext(), text, false);
            }
            cursor.close();
        }

        return DailyJobResult.SUCCESS;
    }

    public static void scheduleJob() {
        JobRequest.Builder builder = new JobRequest.Builder(ComicReleaseSyncJob.TAG);
        builder.setUpdateCurrent(true);
        int jobId = DailyJob.schedule(builder, TimeUnit.HOURS.toMillis(8), TimeUnit.HOURS.toMillis(10));
        CCLogger.d(TAG, "scheduleJob - Created new reminder with ID " + jobId);
    }
}
