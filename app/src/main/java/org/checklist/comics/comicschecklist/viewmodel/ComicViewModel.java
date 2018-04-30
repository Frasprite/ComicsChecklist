package org.checklist.comics.comicschecklist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import org.checklist.comics.comicschecklist.CCApp;
import org.checklist.comics.comicschecklist.DataRepository;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;

public class ComicViewModel extends AndroidViewModel {

    private final LiveData<ComicEntity> mObersavableComic;

    public ObservableField<ComicEntity> comic = new ObservableField<>();

    private final int mComicId;

    public ComicViewModel(@NonNull Application application, DataRepository repository, final int comicId) {
        super(application);
        mComicId = comicId;

        mObersavableComic = repository.loadComic(mComicId);
    }

    public LiveData<ComicEntity> getObersavableComic() {
        return mObersavableComic;
    }

    public void setComic(ComicEntity comic) {
        this.comic.set(comic);
    }

    /**
     * A creator is used to inject the comic ID into the ViewModel.
     * <p>
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the product ID can be passed in a public method.
     */
    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final int mProductId;

        private final DataRepository mRepository;

        public Factory(@NonNull Application application, int productId) {
            mApplication = application;
            mProductId = productId;
            mRepository = ((CCApp) application).getRepository();
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new ComicViewModel(mApplication, mRepository, mProductId);
        }
    }
}
