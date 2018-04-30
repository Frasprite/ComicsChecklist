package org.checklist.comics.comicschecklist.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.checklist.comics.comicschecklist.database.entity.ComicEntity;

import java.util.List;

@Dao
public interface ComicDao {

    @Query("SELECT * FROM comics")
    LiveData<List<ComicEntity>> loadAllComics();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ComicEntity> comics);

    @Query("select * from comics where id = :id")
    LiveData<ComicEntity> loadComic(int id);

    @Query("select * from comics where id = :id")
    ComicEntity loadComicSync(int id);
}
