package org.checklist.comics.comicschecklist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import org.checklist.comics.comicschecklist.database.AppDatabase;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.Filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Repository handling the work with comics.
 */
public class DataRepository {

    private static DataRepository sInstance;

    private final AppDatabase mDatabase;
    private MediatorLiveData<List<ComicEntity>> mObservableComics;
    private MutableLiveData<Filter> mFilter = new MutableLiveData<>();

    private DataRepository(final AppDatabase database) {
        mDatabase = database;
        mObservableComics = new MediatorLiveData<>();

        LiveData<List<ComicEntity>> comicsList = Transformations.switchMap(mFilter, input -> {

            if (!input.getTextToSearch().isEmpty()) {
                return mDatabase.comicDao().loadComicsContainingText(input.getTextToSearch());
            }

            switch (input.getSections()) {
                case FAVORITE:
                    return mDatabase.comicDao().loadFavoriteComics();
                case CART:
                    return mDatabase.comicDao().loadWishlistComics();
                default:
                    return mDatabase.comicDao().loadComicsByEditor(input.getSections().getSectionName());
            }
        });

        filterComics(new Filter(Constants.Sections.FAVORITE, ""));

        mObservableComics.addSource(comicsList,
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
     * Get a list of comics with given filter.
     * @param filter data class composed by 2 main info listed on {@link org.checklist.comics.comicschecklist.util.Filter}
     */
    public void filterComics(Filter filter) {
        this.mFilter.setValue(filter);
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

    public void updateFavorite(int comicId, Boolean flag) {
        mDatabase.comicDao().updateFavorite(comicId, flag);
    }

    public void updateCart(int comicId, Boolean flag) {
        mDatabase.comicDao().updateCart(comicId, flag);
    }

    public void updateComic(int comicId, String name, String info, Date date) {
        mDatabase.comicDao().update(comicId, name, info, date);
    }

    public long insertComic(ComicEntity comicEntity) {
        return mDatabase.comicDao().insert(comicEntity);
    }

    public void deleteComic(ComicEntity comicEntity) {
        mDatabase.comicDao().deleteComic(comicEntity);
    }

    public void insertComics(ArrayList<ComicEntity> comicsList) {
        mDatabase.runInTransaction(() -> mDatabase.comicDao().insertAll(comicsList));
    }

    public int deleteOldComics(long time) {
        return mDatabase.comicDao().deleteOldComics(time);
    }

    public int deleteComics(String editor) {
        return mDatabase.comicDao().deleteComics(editor);
    }

    public int checkFavoritesRelease(long time) {
        return mDatabase.comicDao().checkFavoritesRelease(time);
    }
}
