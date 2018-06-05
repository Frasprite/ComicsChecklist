package org.checklist.comics.comicschecklist.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import org.checklist.comics.comicschecklist.database.converter.DateConverter;
import org.checklist.comics.comicschecklist.database.dao.ComicDao;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.service.DownloadService;
import org.checklist.comics.comicschecklist.util.Constants;

import java.util.ArrayList;
import java.util.Date;

@Database(entities = {ComicEntity.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = AppDatabase.class.getSimpleName();

    private static AppDatabase sInstance;

    @VisibleForTesting
    public static final String DATABASE_NAME = "comics-db";

    public abstract ComicDao comicDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static AppDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext());
                    sInstance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private static AppDatabase buildDatabase(final Context appContext) {
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);

                        // Populate database when it is created, using an Intent service
                        Intent intent = new Intent(appContext, DownloadService.class);
                        intent.putExtra(Constants.CREATE_DATABASE, true);
                        appContext.startService(intent);

                        migrateOldData();
                    }

                    /**
                     * Migrating database from old SQL to Room.
                     */
                    private void migrateOldData() {
                        String[] projection = {ComicDatabase.ID, ComicDatabase.COMICS_NAME_KEY,
                                ComicDatabase.COMICS_DATE_KEY, ComicDatabase.COMICS_EDITOR_KEY,
                                ComicDatabase.COMICS_CART_KEY, ComicDatabase.COMICS_DESCRIPTION_KEY,
                                ComicDatabase.COMICS_FAVORITE_KEY, ComicDatabase.COMICS_FEATURE_KEY,
                                ComicDatabase.COMICS_PRICE_KEY, ComicDatabase.COMICS_URL_KEY,
                                ComicDatabase.COMICS_COVER_KEY};

                        Cursor cursor = ComicDatabaseManager.query(appContext, ComicContentProvider.CONTENT_URI, projection, null, null, null);
                        if (cursor != null) {
                            CCLogger.v(TAG, "onCreate - Entries on cursor : " + cursor.getCount());
                            ArrayList<ComicEntity> comicEntities = new ArrayList<>();
                            while (cursor.moveToNext()) {
                                ComicEntity comicEntity = new ComicEntity(
                                        cursor.getString(cursor.getColumnIndex(ComicDatabase.COMICS_NAME_KEY)),
                                        new Date(cursor.getInt(cursor.getColumnIndex(ComicDatabase.COMICS_DATE_KEY))),
                                        cursor.getString(cursor.getColumnIndex(ComicDatabase.COMICS_DESCRIPTION_KEY)),
                                        cursor.getString(cursor.getColumnIndex(ComicDatabase.COMICS_PRICE_KEY)),
                                        cursor.getString(cursor.getColumnIndex(ComicDatabase.COMICS_FEATURE_KEY)),
                                        cursor.getString(cursor.getColumnIndex(ComicDatabase.COMICS_COVER_KEY)),
                                        cursor.getString(cursor.getColumnIndex(ComicDatabase.COMICS_EDITOR_KEY)),
                                        elaborateBoolean(cursor.getString(cursor.getColumnIndex(ComicDatabase.COMICS_FAVORITE_KEY))),
                                        elaborateBoolean(cursor.getString(cursor.getColumnIndex(ComicDatabase.COMICS_CART_KEY))),
                                        cursor.getString(cursor.getColumnIndex(ComicDatabase.COMICS_URL_KEY))
                                );
                                CCLogger.v(TAG, "onCreate - Migrating comic : " + comicEntity.getName() + " " + comicEntity.getReleaseDate().toString());
                                comicEntities.add(comicEntity);
                            }

                            cursor.close();

                            if (comicEntities.size() > 0) {
                                CCLogger.v(TAG, "onCreate - Migrating total comics : " + comicEntities.size());
                                AsyncTask.execute(() -> sInstance.comicDao().insertAll(comicEntities));
                            }
                        }

                        appContext.deleteDatabase("comictable.db");
                    }

                    /**
                     * Support method which evaluate a boolean from string.
                     * @param raw the string to evaluate
                     * @return true if raw is 'yes'; false otherwise or by default
                     */
                    private boolean elaborateBoolean(String raw) {
                        return raw.equalsIgnoreCase("yes");
                    }
                }).build();
    }

    /**
     * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
     */
    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    public void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }
}
