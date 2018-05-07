package org.checklist.comics.comicschecklist.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.checklist.comics.comicschecklist.R;
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
public class FragmentDetail extends Fragment {

    private static final String TAG = FragmentDetail.class.getSimpleName();

    // The comic content this fragment is presenting.
    private boolean mIsAFavoriteComic;
    private boolean mIsComicOnCart;

    private Menu mMenu;

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

        return mBinding.getRoot();
    }

    // TODO create bottom menu for other options and move URL into card
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity() instanceof ActivityMain) {
            CCLogger.d(TAG, "onCreateOptionsMenu - Inflating detail menu on main toolbar");
            inflater.inflate(R.menu.menu_detail, menu);
            mMenu = menu;
            updateMenuItems(mIsAFavoriteComic, mIsComicOnCart);
        } else {
            CCLogger.d(TAG, "onCreateOptionsMenu - Fragment is launched from ActivityDetail, creating menu");
            // Create detail toolbar
            Toolbar toolbarDetail = getActivity().findViewById(R.id.toolbarDetail);
            // Inflate a menu to be displayed in the toolbar
            if (toolbarDetail != null) {
                toolbarDetail.inflateMenu(R.menu.menu_detail);
                mMenu = toolbarDetail.getMenu();
                updateMenuItems(mIsAFavoriteComic, mIsComicOnCart);

                toolbarDetail.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return manageItemSelected(item);
                    }
                });
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return manageItemSelected(item);
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

    // TODO fix multiple menu entries
    private void updateMenuItems(Boolean favoriteFlag, Boolean cartFlag) {
        if (favoriteFlag) {
            // Comic can be added to favorite
            mMenu.getItem(2).setVisible(true);
            mMenu.getItem(3).setVisible(false);
        } else {
            // Comic can be removed from favorite
            mMenu.getItem(2).setVisible(false);
            mMenu.getItem(3).setVisible(true);
        }

        if (cartFlag) {
            // Comic can be added to cart
            mMenu.getItem(4).setVisible(true);
            mMenu.getItem(5).setVisible(false);
        } else {
            // Comic can be removed from cart
            mMenu.getItem(4).setVisible(false);
            mMenu.getItem(5).setVisible(true);
        }

        // TODO implement feature
        /*String rawEditor = mBinding.getComicViewModel().getObservableComic().getValue().getEditor();
        if (Constants.Sections.getEditorFromName(rawEditor) == Constants.Sections.CART) {
            mMenu.getItem(1).setVisible(false);
        } else {
            mMenu.getItem(1).setVisible(true);
        }*/
    }

    private boolean manageItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.calendar:
                try {
                    CCLogger.i(TAG, "manageItemSelected - Add event on calendar");
                    // ACTION_INSERT does not work on all phones; use Intent.ACTION_EDIT in this case
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setType("vnd.android.cursor.item/event");
                    intent.putExtra(CalendarContract.Events.TITLE,
                            mBinding.getComicViewModel().getObservableComic().getValue().getName());
                    intent.putExtra(CalendarContract.Events.DESCRIPTION, getString(R.string.calendar_release));

                    // Setting dates
                    long timeInMillis = mBinding.getComicViewModel().getObservableComic().getValue().getReleaseDate().getTime();
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
                return true;
            case R.id.comicSite:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                        mBinding.getComicViewModel().getObservableComic().getValue().getURL()
                ));
                startActivity(browserIntent);
                return true;
            case R.id.favorite:
                if (!mIsAFavoriteComic) {
                    CCLogger.i(TAG, "manageItemSelected - Add comic to favorite");
                    // Add comic to favorite
                    // TODO implement feature
                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_added_favorite), Toast.LENGTH_SHORT).show();
                } else {
                    CCLogger.v(TAG, "manageItemSelected - Comic already on favorite");
                }

                updateMenuItems(mIsAFavoriteComic, mIsComicOnCart);

                WidgetService.updateWidget(getActivity());
                return true;
            case R.id.remove_favorite:
                if (mIsAFavoriteComic) {
                    CCLogger.i(TAG, "manageItemSelected - Delete from favorite");
                    // Delete from favorite
                    // TODO implement feature
                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_deleted_favorite), Toast.LENGTH_SHORT).show();
                } else {
                    CCLogger.v(TAG, "manageItemSelected - Comic already deleted from favorite");
                }

                updateMenuItems(mIsAFavoriteComic, mIsComicOnCart);

                WidgetService.updateWidget(getActivity());
                return true;
            case R.id.buy:
                if (!mIsComicOnCart) {
                    CCLogger.i(TAG, "manageItemSelected - Update entry on comic database: add to cart");
                    // Update entry on comic database
                    // TODO implement feature
                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_added_cart), Toast.LENGTH_SHORT).show();
                } else {
                    CCLogger.v(TAG, "manageItemSelected - Comic already on cart");
                }

                updateMenuItems(mIsAFavoriteComic, mIsComicOnCart);

                WidgetService.updateWidget(getActivity());
                return true;
            case R.id.remove_buy:
                if (mIsComicOnCart) {
                    CCLogger.i(TAG, "manageItemSelected - Update entry on comic database: remove from cart");
                    // Update entry on comic database
                    // TODO implement feature
                    Toast.makeText(getActivity(), getResources().getString(R.string.comic_deleted_cart), Toast.LENGTH_SHORT).show();
                } else {
                    CCLogger.v(TAG, "manageItemSelected - Comic already removed from cart");
                }

                updateMenuItems(mIsAFavoriteComic, mIsComicOnCart);

                WidgetService.updateWidget(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
