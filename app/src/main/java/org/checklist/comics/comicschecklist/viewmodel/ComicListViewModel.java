package org.checklist.comics.comicschecklist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import org.checklist.comics.comicschecklist.CCApp;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;

import java.util.List;

public class ComicListViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<ComicEntity>> mObservableComics;

    // LiveData of comics
    private LiveData<List<ComicEntity>> mComics;

    public ComicListViewModel(Application application) {
        super(application);

        mObservableComics = new MediatorLiveData<>();
        // Set by default null, until we get data from the database.
        mObservableComics.setValue(null);

        mComics = ((CCApp) application).getRepository().getComics();

        // Observe the changes of the comics from the database and forward them
        mObservableComics.addSource(mComics, mObservableComics::setValue);
    }

    /**
     * Expose the LiveData Comics query so the UI can observe it.
     */
    public LiveData<List<ComicEntity>> getComics() {
        return mObservableComics;
    }

    public void filterByEditor(String editorName) {
        ((CCApp) getApplication()).getRepository().filterComics(editorName);
        mComics = ((CCApp) getApplication()).getRepository().getComics();
    }

    public void filterComicsContainingText(String editorName, String text) {
        ((CCApp) getApplication()).getRepository().filterComics(editorName, text);
        mComics = ((CCApp) getApplication()).getRepository().getComics();
    }
}
