package org.checklist.comics.comicschecklist.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
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

    // Room maps boolean 'true' to 1 and 'false' to 0.
    @Query("SELECT * FROM comics WHERE isFavorite = 1 ORDER BY release_date")
    LiveData<List<ComicEntity>> loadFavoriteComics();

    @Query("SELECT * FROM comics WHERE isOnCart = 1 ORDER BY release_date")
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

    @Update
    int update(ComicEntity comic);
}
