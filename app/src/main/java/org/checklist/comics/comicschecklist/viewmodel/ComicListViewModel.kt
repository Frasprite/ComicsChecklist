package org.checklist.comics.comicschecklist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.database.entity.ComicEntity

class ComicListViewModel(application: Application) : AndroidViewModel(application) {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private val mObservableComics: MediatorLiveData<List<ComicEntity>> = MediatorLiveData()

    /**
     * Expose the LiveData Comics query so the UI can observe it.
     */
    val comics: LiveData<List<ComicEntity>>
        get() = mObservableComics

    init {
        // Set by default null, until we get data from the database.
        mObservableComics.value = null

        val comics = (application as CCApp).repository.comics

        // Observe the changes of the comics from the database and forward them
        mObservableComics.addSource(comics!!) { mObservableComics.setValue(it) }
    }
}
