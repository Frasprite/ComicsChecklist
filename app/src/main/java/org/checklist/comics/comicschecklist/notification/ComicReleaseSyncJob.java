package org.checklist.comics.comicschecklist.notification;

import android.support.annotation.NonNull;

import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;

import org.checklist.comics.comicschecklist.CCApp;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.log.CCLogger;
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
        CCLogger.d(TAG, "onRunDailyJob - start");
        // Do the work that requires your app to keep the CPU running
        String today = DateCreator.getTodayString();
        CCLogger.v(TAG, "onRunDailyJob - Today is " + today);
        int totalItems = ((CCApp) getContext().getApplicationContext()).getRepository().checkFavoritesRelease(DateCreator.getTimeInMillis(today));

        String message = getContext().getResources().getString(R.string.notification_comic_out);
        if (totalItems == 1) {
            CCLogger.i(TAG, "onRunDailyJob - Favorite comic found");
        } else if (totalItems > 1) {
            CCLogger.i(TAG, "onRunDailyJob - Founded more favorite comics");
            message = getContext().getResources().getString(R.string.notification_comics_out, totalItems);
        }

        CCNotificationManager.createNotification(getContext(), message, false);

        return DailyJobResult.SUCCESS;
    }

    public static void scheduleJob() {
        JobRequest.Builder builder = new JobRequest.Builder(ComicReleaseSyncJob.TAG);
        builder.setUpdateCurrent(true);
        int jobId = DailyJob.schedule(builder, TimeUnit.HOURS.toMillis(8), TimeUnit.HOURS.toMillis(10));
        CCLogger.d(TAG, "scheduleJob - Created new reminder with ID " + jobId);
    }
}
