package org.checklist.comics.comicschecklist.notification

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

/**
 * Class which manage the creation of a [android.app.job.JobScheduler].
 */

class ComicReleaseJobCreator : JobCreator {

    override fun create(tag: String): Job? {
        return when (tag) {
            ComicReleaseSyncJob.TAG -> ComicReleaseSyncJob()
            else -> null
        }
    }
}
