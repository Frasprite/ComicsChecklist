package org.checklist.comics.comicschecklist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import org.checklist.comics.comicschecklist.database.AppDatabase;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.util.Constants;

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

        mObservableComics.addSource(mDatabase.comicDao().loadComicsByEditor(Constants.Sections.FAVORITE.getName()),
                comicEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableComics.postValue(comicEntities);
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
     * Get the list of all comics from the database and get notified when the data changes.
     */
    public LiveData<List<ComicEntity>> getComics() {
        return mObservableComics;
    }

    /**
     * Get a list of comics with given editor.
     * @param editorName one of editor listed on {@link org.checklist.comics.comicschecklist.util.Constants.Sections} raw name
     */
    public void filterComics(String editorName) {
        mObservableComics.addSource(mDatabase.comicDao().loadComicsByEditor(editorName),
                comicEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableComics.postValue(comicEntities);
                    }
                });
    }

    public LiveData<ComicEntity> loadComic(final int productId) {
        return mDatabase.comicDao().loadComic(productId);
    }
}
