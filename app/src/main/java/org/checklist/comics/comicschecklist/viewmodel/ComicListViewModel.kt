package org.checklist.comics.comicschecklist.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.database.entity.ComicEntity

class ComicListViewModel(application: Application) : AndroidViewModel(application) {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private val mObservableComics: MediatorLiveData<List<ComicEntity>> = MediatorLiveData()

    // LiveData of comics
    private var mComics: LiveData<List<ComicEntity>>? = null

    /**
     * Expose the LiveData Comics query so the UI can observe it.
     */
    val comics: LiveData<List<ComicEntity>>
        get() = mObservableComics

    init {

        // Set by default null, until we get data from the database.
        mObservableComics.value = null

        mComics = (application as CCApp).repository.comics

        // Observe the changes of the comics from the database and forward them
        mObservableComics.addSource(mComics!!) { mObservableComics.setValue(it) }
    }

    fun getFavoriteComics() {
        (getApplication<Application>() as CCApp).repository.getFavoriteComics()
        mComics = (getApplication<Application>() as CCApp).repository.comics
    }

    fun getWishlistComics() {
        (getApplication<Application>() as CCApp).repository.getWishlistComics()
        mComics = (getApplication<Application>() as CCApp).repository.comics
    }

    fun filterByEditor(editorName: String) {
        (getApplication<Application>() as CCApp).repository.filterComics(editorName)
        mComics = (getApplication<Application>() as CCApp).repository.comics
    }

    fun filterComicsContainingText(editorName: String, text: String) {
        (getApplication<Application>() as CCApp).repository.filterComics(editorName, text)
        mComics = (getApplication<Application>() as CCApp).repository.comics
    }
}
