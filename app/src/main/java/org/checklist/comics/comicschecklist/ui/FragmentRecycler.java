package org.checklist.comics.comicschecklist.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.checklist.comics.comicschecklist.CCApp;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.databinding.FragmentRecyclerViewBinding;
import org.checklist.comics.comicschecklist.widget.WidgetService;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.viewmodel.ComicListViewModel;

/**
 * A fragment representing a list of Comics. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link FragmentDetail}.
 * <p>
 */
public class FragmentRecycler extends Fragment {

    private static final String TAG = FragmentRecycler.class.getSimpleName();

    private ComicAdapter mComicAdapter;
    private FragmentRecyclerViewBinding mBinding;

    private ItemTouchHelper.SimpleCallback sItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            // Remove swiped item from list
            int position = viewHolder.getAdapterPosition();
            ComicEntity comic = mComicAdapter.mComicList.get(position);
            CCLogger.v(TAG, "onSwiped - Comic ID " + comic.getId());
            updateComic(comic);
        }
    };

    private final ComicClickCallback mComicClickCallback = comic -> {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            ((ActivityMain) getActivity()).launchDetailView(comic);
        }
    };

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static FragmentRecycler newInstance(Constants.Sections section) {
        CCLogger.v(TAG, "newInstance - " + section.toString());
        FragmentRecycler fragment = new FragmentRecycler();
        Bundle args = new Bundle();
        args.putSerializable(Constants.ARG_EDITOR, section);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CCLogger.d(TAG, "onCreateView - start");
        // Create the list fragment's content view by calling the super method
        super.onCreateView(inflater, container, savedInstanceState);

        // Find editor
        Constants.Sections editor = (Constants.Sections) getArguments().getSerializable(Constants.ARG_EDITOR);
        CCLogger.d(TAG, "onCreateView - name " + editor);

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recycler_view, container, false);

        // Set proper empty view
        if (editor.equals(Constants.Sections.FAVORITE)) {
            mBinding.emptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_empty_list_stub, 0, 0);
            mBinding.emptyTextView.setText(getString(R.string.empty_favorite_list));
        } else if (editor.equals(Constants.Sections.CART)) {
            mBinding.emptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_empty_list_stub, 0, 0);
            mBinding.emptyTextView.setText(getString(R.string.empty_cart_list));
        } else {
            mBinding.emptyTextView.setText(getString(R.string.empty_editor_list));
        }

        // Init adapter
        mComicAdapter = new ComicAdapter(mComicClickCallback);
        mBinding.recyclerView.setAdapter(mComicAdapter);

        // Attach swipe to delete (right / left) if we are in CART or FAVORITE section
        if (editor.equals(Constants.Sections.CART) || editor.equals(Constants.Sections.FAVORITE)) {
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(sItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(mBinding.recyclerView);
        }

        // Set title into toolbar
        getActivity().setTitle(Constants.Sections.getTitle(editor));

        // Enable swipe to refresh if we are on other categories
        if (editor.equals(Constants.Sections.FAVORITE) || editor.equals(Constants.Sections.CART)) {
            CCLogger.v(TAG, "onCreateView - Locking swipe to refresh");
            mBinding.swipeRefreshLayout.setEnabled(false);
        }

        // Set color of progress view
        mBinding.swipeRefreshLayout.setColorSchemeResources(R.color.primary_light, R.color.primary, R.color.primary_dark, R.color.accent);

        setRecyclerViewLayoutManager(mBinding.recyclerView);

        /*
          Implement {@link SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
          refresh" gesture, SwipeRefreshLayout invokes
          {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
          {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
          refreshes the content. Call the same method in response to the Refresh action from the
          action bar.
         */
        setOnRefreshListener( () -> ((ActivityMain) getActivity()).initiateRefresh(editor) );

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ComicListViewModel viewModel =
                ViewModelProviders.of(this).get(ComicListViewModel.class);

        subscribeUi(viewModel, null, (Constants.Sections) getArguments().getSerializable(Constants.ARG_EDITOR));
    }

    /**
     * Set the layout type of {@link RecyclerView}.
     * @param recyclerView the current recycler view where to set layout
     */
    private void setRecyclerViewLayoutManager(RecyclerView recyclerView) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.scrollToPosition(scrollPosition);
    }

    /**
     * Method which observe the status of database and update UI when data is available.<br>
     * If {@param text} is not null, the list will be filtered based on editor and part of text specified.
     * @param viewModel the view model which store and manage the data to show
     * @param text the part of text to search on database
     */
    private void subscribeUi(ComicListViewModel viewModel, String text, Constants.Sections editor) {
        // Update the list when the data changes
        if (text == null) {
            switch (editor) {
                case FAVORITE:
                    viewModel.getFavoriteComics();
                    break;
                case CART:
                    viewModel.getWishlistComics();
                    break;
                default:
                    viewModel.filterByEditor(editor.getName());
                    break;
            }
        } else {
            viewModel.filterComicsContainingText(editor.getName(), text);
        }

        viewModel.getComics().observe(this, myComics -> {
            if (myComics != null) {
                if (myComics.size() == 0) {
                    mBinding.setIsLoading(true);
                } else {
                    mBinding.setIsLoading(false);
                }
                mComicAdapter.setComicList(myComics);
            } else {
                mBinding.setIsLoading(true);
            }
            // Espresso does not know how to wait for data binding's loop so we execute changes
            // sync.
            mBinding.executePendingBindings();
        });
    }

    /**
     * Method used to update the list after user gave an input on search view.
     * @param newText the text to use for filter data
     */
    public void updateList(String newText) {
        final ComicListViewModel viewModel =
                ViewModelProviders.of(this).get(ComicListViewModel.class);

        subscribeUi(viewModel, newText, (Constants.Sections) getArguments().getSerializable(Constants.ARG_EDITOR));
    }

    /**
     * Update a changes of favorite or wished comic on database.
     * @param comic the entry to update on database
     */
    public void updateComic(ComicEntity comic) {
        if (comic.getEditor().equals(Constants.Sections.FAVORITE.getName())) {
            CCLogger.d(TAG, "deleteComic - Removing favorite comic with ID " + comic.getId());
            // Remove comic from favorite
            comic.setFavorite(!comic.isFavorite());
            updateData(comic);
        } else if (comic.getEditor().equals(Constants.Sections.CART.getName())) {
            CCLogger.d(TAG, "onContextItemSelected - Removing comic in cart with ID " + comic.getId());
            // Remove comic from cart
            removeComicFromCart(comic);
        }

        WidgetService.Companion.updateWidget(getActivity());
    }

    /**
     * Method used to handle update operation on comic which is on cart list.<br>
     * If the comic was created by user, it will be deleted; otherwise it is just updated.
     * @param comic the comic to update or remove
     */
    private void removeComicFromCart(ComicEntity comic) {
        // Evaluate if comic was created by user or it is coming from network
        Constants.Sections editor = Constants.Sections.getEditorFromName(comic.getEditor());
        switch (editor) {
            case CART:
                // Simply delete comic because was created by user
                deleteData(comic);
                CCLogger.v(TAG, "removeComicFromCart - Comic deleted!");
                break;
            default:
                // Update comic because it is created from web data
                comic.setToCart(!comic.isOnCart());
                updateData(comic);
                CCLogger.v(TAG, "removeComicFromCart - Comic updated!");
                break;
        }
    }

    /**
     * Updating data of comic.
     * @param comicEntity the comic to update
     */
    private void updateData(ComicEntity comicEntity) {
        AsyncTask.execute(() -> ((CCApp) getActivity().getApplication()).getRepository().updateComic(comicEntity));

        WidgetService.Companion.updateWidget(getActivity());
    }

    /**
     * Completely delete the comic from database.
     * @param comicEntity the comic to delete
     */
    private void deleteData(ComicEntity comicEntity) {
        AsyncTask.execute(() -> ((CCApp) getActivity().getApplication()).getRepository().deleteComic(comicEntity));

        WidgetService.Companion.updateWidget(getActivity());
    }

    /* ****************************************************************************************
     * SwipeRefreshLayout methods
     ******************************************************************************************/

    /**
     * Set the {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener} to listen for
     * initiated refreshes.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout#setOnRefreshListener(android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener)
     */
    private void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        if (mBinding.swipeRefreshLayout != null) {
            mBinding.swipeRefreshLayout.setOnRefreshListener(listener);
        }
    }

    /**
     * Returns whether the {@link android.support.v4.widget.SwipeRefreshLayout} is currently
     * refreshing or not.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout#isRefreshing()
     */
    public boolean isRefreshing() {
        return mBinding.swipeRefreshLayout != null && mBinding.swipeRefreshLayout.isRefreshing();
    }

    /**
     * Set whether the {@link android.support.v4.widget.SwipeRefreshLayout} should be displaying
     * that it is refreshing or not.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout#setRefreshing(boolean)
     */
    public void setRefreshing(boolean refreshing) {
        if (mBinding.swipeRefreshLayout != null) {
            mBinding.swipeRefreshLayout.setRefreshing(refreshing);
        }
    }
}
