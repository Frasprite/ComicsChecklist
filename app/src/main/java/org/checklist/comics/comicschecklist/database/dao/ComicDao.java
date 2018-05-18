package org.checklist.comics.comicschecklist.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.checklist.comics.comicschecklist.database.entity.ComicEntity;

import java.util.List;

@Dao
public interface ComicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ComicEntity> comics);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ComicEntity comic);

    @Delete
    void deleteComic(ComicEntity comic);

    @Query("DELETE FROM comics WHERE release_date <= :time AND isFavorite = 0 AND isOnCart = 0")
    int deleteOldComics(long time);

    @Query("DELETE FROM comics WHERE editor LIKE :editor AND isFavorite = 0 AND isOnCart = 0")
    int deleteComics(String editor);

    @Query("SELECT * FROM comics WHERE isFavorite = 1 ORDER BY release_date")
    LiveData<List<ComicEntity>> loadFavoriteComics();

    @Query("SELECT * FROM comics WHERE isOnCart = 1 OR editor LIKE 'da comprare' ORDER BY release_date")
    LiveData<List<ComicEntity>> loadWishlistComics();

    @Query("SELECT * FROM comics WHERE editor LIKE :editorName ORDER BY release_date")
    LiveData<List<ComicEntity>> loadComicsByEditor(String editorName);

    @Query("SELECT * FROM comics WHERE editor LIKE :editorName AND comic_name LIKE '%' || :character || '%' ORDER BY release_date")
    LiveData<List<ComicEntity>> loadComicsContainingText(String editorName, String character);

    @Query("SELECT * FROM comics WHERE id = :id")
    LiveData<ComicEntity> loadComic(int id);

    @Query("SELECT * FROM comics WHERE id = :id")
    ComicEntity loadComicSync(int id);

    @Query("SELECT * FROM comics WHERE editor LIKE :editorName ORDER BY release_date")
    List<ComicEntity> loadComicsByEditorSync(String editorName);

    @Query("SELECT * FROM comics WHERE isFavorite = 1 AND release_date = :time")
    int checkFavoritesRelease(long time);

    @Update
    int update(ComicEntity comic);
}
