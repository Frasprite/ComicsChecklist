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

    /**
     * Return a list of comics with given editor and containing specific character.
     * @param editorName one of editor listed on {@link org.checklist.comics.comicschecklist.util.Constants.Sections} raw name
     * @param textToSearch the text to search on DB
     */
    public void filterComics(String editorName, String textToSearch) {
        mObservableComics.addSource(mDatabase.comicDao().loadComicsContainingText(editorName, textToSearch),
                comicEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableComics.postValue(comicEntities);
                    }
                });
    }

    /**
     * Get specific comic by given ID.
     * @param productId the comic ID
     * @return the {@link ComicEntity} representing the comic itself
     */
    public LiveData<ComicEntity> loadComic(final int productId) {
        return mDatabase.comicDao().loadComic(productId);
    }

    public ComicEntity loadComicSync(int comicId) {
        return mDatabase.comicDao().loadComicSync(comicId);
    }

    public List<ComicEntity> loadComicsByEditorSync(String editor) {
        return mDatabase.comicDao().loadComicsByEditorSync(editor);
    }

    public void updateComic(ComicEntity comicEntity) {
        mDatabase.comicDao().update(comicEntity);
    }

    public long insertComic(ComicEntity comicEntity) {
        return mDatabase.comicDao().insert(comicEntity);
    }

    public void deleteComic(ComicEntity comicEntity) {
        mDatabase.comicDao().deleteComic(comicEntity);
    }

    public void getFavoriteComics() {
        mObservableComics.addSource(mDatabase.comicDao().loadFavoriteComics(),
                comicEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableComics.postValue(comicEntities);
                    }
                });
    }

    public void getWishlistComics() {
        mObservableComics.addSource(mDatabase.comicDao().loadWishlistComics(),
                comicEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableComics.postValue(comicEntities);
                    }
                });
    }
}
