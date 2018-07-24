package org.checklist.comics.comicschecklist.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.databinding.ObservableField

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.DataRepository
import org.checklist.comics.comicschecklist.database.entity.ComicEntity

class ComicViewModel(application: Application, repository: DataRepository, mComicId: Int) : AndroidViewModel(application) {

    val observableComic: LiveData<ComicEntity> = repository.loadComic(mComicId)

    var comic = ObservableField<ComicEntity>()

    fun setComic(comic: ComicEntity) {
        this.comic.set(comic)
    }

    /**
     * A creator is used to inject the comic ID into the ViewModel.
     *
     *
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the comic ID can be passed in a public method.
     */
    class Factory(private val mApplication: Application, private val mComicId: Int) : ViewModelProvider.NewInstanceFactory() {

        private val mRepository: DataRepository = (mApplication as CCApp).repository

        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            return ComicViewModel(mApplication, mRepository, mComicId) as T
        }
    }
}
