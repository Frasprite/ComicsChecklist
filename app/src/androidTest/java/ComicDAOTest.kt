import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.matcher.ViewMatchers.assertThat

import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull

import org.checklist.comics.comicschecklist.DataRepository
import org.checklist.comics.comicschecklist.database.AppDatabase
import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.util.Constants

import org.hamcrest.Matchers.greaterThan

import org.junit.After
import org.junit.Before
import org.junit.Test

import java.util.*

class ComicDAOTest {

    private lateinit var appDatabase: AppDatabase
    private lateinit var comic: LiveData<ComicEntity>

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getTargetContext()
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @Test
    fun openDAOTest() {
        val comicDAO = appDatabase.comicDao()

        assertNotNull(comicDAO)
    }

    @Test
    fun loadComicTest() {
        val repository = DataRepository.getInstance(appDatabase)
        comic = repository.loadComic(911)
        assertNull(comic.value)
    }

    @Test
    fun insertComicTest() {
        val repository = DataRepository.getInstance(appDatabase)

        val comicEntity = ComicEntity("A custom note", Date(), "SpiderMan: all numbers of 2017",
                            "", "", "", Constants.Sections.CART.sectionName, false, true, "")

        val newId = repository.insertComic(comicEntity)
        assertNotNull(newId)
    }

    @Test
    fun deleteComicTest() {
        val repository = DataRepository.getInstance(appDatabase)

        val comicsDeleted = repository.deleteComics(Constants.Sections.CART.sectionName)
        assertThat(comicsDeleted, greaterThan(0))
    }

    @After
    fun tearDown() {
        appDatabase.close()
    }
}