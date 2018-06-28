package org.checklist.comics.comicschecklist

import android.app.Application

import com.evernote.android.job.JobManager

import org.checklist.comics.comicschecklist.database.AppDatabase
import org.checklist.comics.comicschecklist.extensions.DelegatesExt
import org.checklist.comics.comicschecklist.notification.ComicReleaseJobCreator

/**
 * Class which extends the main class Application of project.
 */
class CCApp : Application() {

    companion object {
        var instance: CCApp by DelegatesExt.notNullSingleValue()
    }

    val database: AppDatabase
        get() = AppDatabase.getInstance(this)

    val repository: DataRepository
        get() = DataRepository.getInstance(database)

    override fun onCreate() {
        super.onCreate()
        instance = this
        JobManager.create(this).addJobCreator(ComicReleaseJobCreator())
    }
}
