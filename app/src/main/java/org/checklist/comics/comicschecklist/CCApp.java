package org.checklist.comics.comicschecklist;

import android.app.Application;

import com.evernote.android.job.JobManager;

import org.checklist.comics.comicschecklist.database.AppDatabase;
import org.checklist.comics.comicschecklist.notification.ComicReleaseJobCreator;

/**
 * Class which extends the main class Application of project.
 */
public class CCApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new ComicReleaseJobCreator());
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase());
    }
}
