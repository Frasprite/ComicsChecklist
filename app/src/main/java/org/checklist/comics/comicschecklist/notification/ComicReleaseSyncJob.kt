package org.checklist.comics.comicschecklist.notification

import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobRequest

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.log.CCLogger

import org.joda.time.DateTime

import java.util.concurrent.TimeUnit

/**
 * Class which manage the scheduling of jobs.
 */
class ComicReleaseSyncJob : DailyJob() {

    override fun onRunDailyJob(params: Params): DailyJobResult {
        CCLogger.d(TAG, "onRunDailyJob - start")
        // Do the work that requires your app to keep the CPU running
        val today = DateTime()
        CCLogger.v(TAG, "onRunDailyJob - Today is " + today.toString())
        val totalItems = (context.applicationContext as CCApp).repository.checkFavoritesRelease(today.millis)

        val message = when (totalItems) {
            0 -> { return DailyJobResult.SUCCESS }
            1 -> {
                CCLogger.i(TAG, "onRunDailyJob - Favorite comic found")
                context.resources.getString(R.string.notification_comic_out)
            }
            else -> {
                CCLogger.i(TAG, "onRunDailyJob - Founded more favorite comics")
                context.resources.getString(R.string.notification_comics_out, totalItems)
            }
        }

        CCNotificationManager.createNotification(context, message, false)

        return DailyJobResult.SUCCESS
    }

    companion object {

        val TAG = ComicReleaseSyncJob::class.simpleName

        fun scheduleJob() {
            val builder = JobRequest.Builder("ComicReleaseSyncJob")
            builder.setUpdateCurrent(true)
            val jobId = DailyJob.schedule(builder, TimeUnit.HOURS.toMillis(8), TimeUnit.HOURS.toMillis(10))
            CCLogger.d(TAG, "scheduleJob - Created new reminder with ID $jobId")
        }
    }
}
