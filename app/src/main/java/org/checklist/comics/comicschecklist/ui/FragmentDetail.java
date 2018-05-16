package org.checklist.comics.comicschecklist.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.databinding.FragmentDetailBinding;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.viewmodel.ComicViewModel;

/**
 * A fragment representing a single Comic detail screen.
 * This fragment is either contained in a {@link ActivityMain}
 * in two-pane mode (on tablets) or a {@link ActivityDetail}
 * on handsets.
 */
public class FragmentDetail extends Fragment implements View.OnClickListener {

    private static final String TAG = FragmentDetail.class.getSimpleName();

    private FragmentDetailBinding mBinding;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FragmentDetail() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);

        mBinding.site.setOnClickListener(this);
        mBinding.calendar.setOnClickListener(this);
        mBinding.favorite.setOnClickListener(this);
        mBinding.buy.setOnClickListener(this);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int comicId = getArguments().getInt(Constants.ARG_COMIC_ID);
        CCLogger.i(TAG, "onCreate - comic with ID " + comicId);

        ComicViewModel.Factory factory = new ComicViewModel.Factory(getActivity().getApplication(), comicId);

        final ComicViewModel model = ViewModelProviders.of(this, factory)
                .get(ComicViewModel.class);

        mBinding.setComicViewModel(model);

        subscribeToModel(model);
    }

    /**
     * Method which observe the item data.
     * @param model the view model which store and manage the data to show
     */
    private void subscribeToModel(final ComicViewModel model) {
        model.getObservableComic().observe(this, comicEntity -> model.setComic(comicEntity));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.site:
                goToSite();
                break;
            case R.id.calendar:
                createEvent();
                break;
            case R.id.favorite:
                manageFavorite();
                break;
            case R.id.buy:
                manageWishlist();
                break;
        }
    }

    private void goToSite() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                mBinding.getComicViewModel().comic.get().getURL()
        ));
        startActivity(browserIntent);
    }

    private void createEvent() {
        try {
            CCLogger.i(TAG, "createEvent - Add event on calendar");
            // ACTION_INSERT does not work on all phones; use Intent.ACTION_EDIT in this case
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType("vnd.android.cursor.item/event");
            intent.putExtra(CalendarContract.Events.TITLE, mBinding.getComicViewModel().comic.get().getName());
            intent.putExtra(CalendarContract.Events.DESCRIPTION, getString(R.string.calendar_release));

            // Setting dates
            long timeInMillis = mBinding.getComicViewModel().comic.get().getReleaseDate().getTime();
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, timeInMillis);
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, timeInMillis);

            // Make it a full day event
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

            // Making it private and shown as busy
            intent.putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE);
            intent.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getActivity(), R.string.calendar_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void manageFavorite() {
        boolean isAFavoriteComic = mBinding.getComicViewModel().comic.get().isFavorite();

        if (!isAFavoriteComic) {
            CCLogger.i(TAG, "manageFavorite - Add comic to favorite");
            isAFavoriteComic = true;
            // Add comic to favorite
            Toast.makeText(getActivity(), getResources().getString(R.string.comic_added_favorite), Toast.LENGTH_SHORT).show();
        } else {
            CCLogger.i(TAG, "manageFavorite - Delete from favorite");
            isAFavoriteComic = false;
            // Delete from favorite
            Toast.makeText(getActivity(), getResources().getString(R.string.comic_deleted_favorite), Toast.LENGTH_SHORT).show();
        }

        ComicEntity comicEntity = mBinding.getComicViewModel().comic.get();
        comicEntity.setFavorite(isAFavoriteComic);

        updateData(comicEntity);
    }

    private void manageWishlist() {
        boolean isComicOnCart = mBinding.getComicViewModel().comic.get().isOnCart();

        if (!isComicOnCart) {
            CCLogger.i(TAG, "manageWishlist - Update entry on comic database: add to cart");
            isComicOnCart = true;
            // Update entry on comic database
            Toast.makeText(getActivity(), getResources().getString(R.string.comic_added_cart), Toast.LENGTH_SHORT).show();
        } else {
            CCLogger.i(TAG, "manageWishlist - Update entry on comic database: remove from cart");
            isComicOnCart = false;
            // Update entry on comic database
            Toast.makeText(getActivity(), getResources().getString(R.string.comic_deleted_cart), Toast.LENGTH_SHORT).show();
        }

        ComicEntity comicEntity = mBinding.getComicViewModel().comic.get();
        comicEntity.setToCart(isComicOnCart);

        updateData(comicEntity);
    }

    private void updateData(ComicEntity comicEntity) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBinding.getComicViewModel().updateComic(comicEntity);
            }
        });

        WidgetService.updateWidget(getActivity());
    }
}
