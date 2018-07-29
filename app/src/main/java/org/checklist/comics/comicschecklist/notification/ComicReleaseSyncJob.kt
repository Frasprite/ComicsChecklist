package org.checklist.comics.comicschecklist.notification

import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobRequest

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.extensions.lazyLogger

import org.jetbrains.anko.debug
import org.jetbrains.anko.info
import org.jetbrains.anko.verbose

import org.joda.time.DateTime

import java.util.concurrent.TimeUnit

/**
 * Class which manage the scheduling of jobs.
 */
class ComicReleaseSyncJob : DailyJob() {

    override fun onRunDailyJob(params: Params): DailyJobResult {
        LOG.debug("onRunDailyJob - start")
        // Do the work that requires your app to keep the CPU running
        val today = DateTime()
        LOG.verbose("onRunDailyJob - Today is " + today.toString())
        val totalItems = (context.applicationContext as CCApp).repository.checkFavoritesRelease(today.millis)

        val message = when (totalItems) {
            0 -> { return DailyJobResult.SUCCESS }
            1 -> {
                LOG.info("onRunDailyJob - Favorite comic found")
                context.resources.getString(R.string.notification_comic_out)
            }
            else -> {
                LOG.info("onRunDailyJob - Founded more favorite comics")
                context.resources.getString(R.string.notification_comics_out, totalItems)
            }
        }

        CCNotificationManager.createNotification(context, message, false)

        return DailyJobResult.SUCCESS
    }

    companion object {

        private val LOG by lazyLogger()

        fun scheduleJob() {
            val builder = JobRequest.Builder("ComicReleaseSyncJob")
            builder.setUpdateCurrent(true)
            val jobId = DailyJob.schedule(builder, TimeUnit.HOURS.toMillis(8), TimeUnit.HOURS.toMillis(10))
            LOG.debug("scheduleJob - Created new reminder with ID $jobId")
        }
    }
}
