package org.checklist.comics.comicschecklist.notification;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Class which manage the creation of a {@link android.app.job.JobScheduler}.
 */

public class ComicReleaseJobCreator implements JobCreator {

    @Override
    @Nullable
    public Job create(@NonNull String tag) {
        switch (tag) {
            case ComicReleaseSyncJob.TAG:
                return new ComicReleaseSyncJob();
            default:
                return null;
        }
    }
}
