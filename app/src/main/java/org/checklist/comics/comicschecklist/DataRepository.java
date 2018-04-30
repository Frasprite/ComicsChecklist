package org.checklist.comics.comicschecklist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import org.checklist.comics.comicschecklist.database.AppDatabase;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;

import java.util.List;

/**
 * Repository handling the work with comics.
 */
public class DataRepository {

    private static DataRepository sInstance;

    private final AppDatabase mDatabase;
    private MediatorLiveData<List<ComicEntity>> mObservableComics;

    private DataRepository(final AppDatabase database) {
        mDatabase = database;
        mObservableComics = new MediatorLiveData<>();

        mObservableComics.addSource(mDatabase.comicDao().loadAllComics(),
                productEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableComics.postValue(productEntities);
                    }
                });
    }

    public static DataRepository getInstance(final AppDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database);
                }
            }
        }
        return sInstance;
    }

    /**
     * Get the list of products from the database and get notified when the data changes.
     */
    public LiveData<List<ComicEntity>> getComics() {
        return mObservableComics;
    }

    public LiveData<ComicEntity> loadComic(final int productId) {
        return mDatabase.comicDao().loadComic(productId);
    }
}
