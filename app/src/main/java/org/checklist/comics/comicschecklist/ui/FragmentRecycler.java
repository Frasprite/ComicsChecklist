package org.checklist.comics.comicschecklist.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.checklist.comics.comicschecklist.CCApp;
import org.checklist.comics.comicschecklist.R;
import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.databinding.FragmentRecyclerViewBinding;
import org.checklist.comics.comicschecklist.model.Comic;
import org.checklist.comics.comicschecklist.service.DownloadService;
import org.checklist.comics.comicschecklist.service.WidgetService;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.viewmodel.ComicListViewModel;

import java.util.List;

/**
 * A fragment representing a list of Comics. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link FragmentDetail}.
 * <p>
 */
public class FragmentRecycler extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = FragmentRecycler.class.getSimpleName();

    private ComicAdapter mComicAdapter;
    private FragmentRecyclerViewBinding mBinding;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Constants.Sections mEditor;

    private ItemTouchHelper.SimpleCallback sItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            // Remove swiped item from list
            int position = viewHolder.getAdapterPosition();
            Comic comic = mComicAdapter.mComicList.get(position);
            CCLogger.v(TAG, "onSwiped - Comic ID " + comic.getId());
            updateComic(comic);
        }
    };

    private final ComicClickCallback mComicClickCallback = new ComicClickCallback() {
        @Override
        public void onClick(Comic comic) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                ((ActivityMain) getActivity()).launchDetailView(comic);
            }
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Find editor
        mEditor = (Constants.Sections) getArguments().getSerializable(Constants.ARG_EDITOR);
        CCLogger.d(TAG, "onCreate - name " + mEditor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CCLogger.d(TAG, "onCreateView - start");
        // Create the list fragment's content view by calling the super method
        super.onCreateView(inflater, container, savedInstanceState);

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recycler_view, container, false);

        // Init adapter
        // TODO add Paging from architecture, because content will be updated each time an item changes
        mComicAdapter = new ComicAdapter(mComicClickCallback);
        mBinding.recyclerView.setAdapter(mComicAdapter);

        // Attach listener to navigation bottom
        mBinding.bottomNavigation.setOnNavigationItemSelectedListener(this);

        // Init swipe refresh layout
        RecyclerView recyclerView = mBinding.getRoot().findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = mBinding.getRoot().findViewById(R.id.swipe_refresh_layout);

        // Attach swipe to delete (right / left) if we are in CART or FAVORITE section
        if (mEditor.equals(Constants.Sections.CART) || mEditor.equals(Constants.Sections.FAVORITE)) {
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(sItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }

        // Set title into toolbar
        getActivity().setTitle(Constants.Sections.getTitle(mEditor));

        // Enable swipe to refresh if we are on other categories
        if (mEditor.equals(Constants.Sections.FAVORITE) || mEditor.equals(Constants.Sections.CART)) {
            CCLogger.v(TAG, "onCreateView - Locking swipe to refresh");
            mSwipeRefreshLayout.setEnabled(false);
        }

        // Set color of progress view
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary_light, R.color.primary, R.color.primary_dark, R.color.accent);

        setRecyclerViewLayoutManager(recyclerView);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CCLogger.d(TAG, "onViewCreated - start");

        TextView emptyText = view.findViewById(R.id.empty_text_view);

        if (mEditor.equals(Constants.Sections.FAVORITE)) {
            emptyText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_empty_list_stub, 0, 0);
            emptyText.setText(getString(R.string.empty_favorite_list));
        } else if (mEditor.equals(Constants.Sections.CART)) {
            emptyText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_empty_list_stub, 0, 0);
            emptyText.setText(getString(R.string.empty_cart_list));
        } else {
            emptyText.setText(getString(R.string.empty_editor_list));
        }

        /*
          Implement {@link SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
          refresh" gesture, SwipeRefreshLayout invokes
          {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
          {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
          refreshes the content. Call the same method in response to the Refresh action from the
          action bar.
         */
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initiateRefresh(mEditor);
            }
        });

        CCLogger.v(TAG, "onViewCreated - end");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ComicListViewModel viewModel =
                ViewModelProviders.of(this).get(ComicListViewModel.class);

        subscribeUi(viewModel, null);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        ActivityMain activityMain = (ActivityMain) getActivity();
        switch (item.getItemId()) {
            case R.id.searchStore:
                // This method can be called from shortcut (Android 7.1 and above)
                activityMain.searchStore();
                break;
            case R.id.addComic:
                // This method can be called from shortcut (Android 7.1 and above)
                activityMain.addComic();
                break;
            case R.id.refresh:
                initiateRefresh(mEditor);
                break;
        }
        return true;
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
    private void subscribeUi(ComicListViewModel viewModel, String text) {
        // Update the list when the data changes
        if (text == null) {
            switch (mEditor) {
                case FAVORITE:
                    viewModel.getFavoriteComics();
                    break;
                case CART:
                    viewModel.getWishlistComics();
                    break;
                default:
                    viewModel.filterByEditor(mEditor.getName());
                    break;
            }
        } else {
            viewModel.filterComicsContainingText(mEditor.getName(), text);
        }
        viewModel.getComics().observe(this, new Observer<List<ComicEntity>>() {
            @Override
            public void onChanged(@Nullable List<ComicEntity> myComics) {
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
            }
        });
    }

    /**
     * Method used to update the list after user gave an input on search view.
     * @param newText the text to use for filter data
     */
    public void updateList(String newText) {
        final ComicListViewModel viewModel =
                ViewModelProviders.of(this).get(ComicListViewModel.class);

        subscribeUi(viewModel, newText);
    }

    /**
     * Update a changes of favorite or wished comic on database.
     * @param comic the entry to update on database
     */
    public void updateComic(Comic comic) {
        if (mEditor.equals(Constants.Sections.FAVORITE)) {
            CCLogger.d(TAG, "deleteComic - Removing favorite comic with ID " + comic.getId());
            // Remove comic from favorite
            ComicEntity comicEntity = (ComicEntity) comic;
            comicEntity.setFavorite(!comicEntity.isFavorite());
            updateData(comicEntity);
        } else if (mEditor.equals(Constants.Sections.CART)) {
            CCLogger.d(TAG, "onContextItemSelected - Removing comic in cart with ID " + comic.getId());
            // Remove comic from cart
            removeComicFromCart(comic);
        }

        WidgetService.updateWidget(getActivity());
    }

    /**
     * Method used to handle update operation on comic which is on cart list.<br>
     * If the comic was created by user, it will be deleted; otherwise it is just updated.
     * @param comic the comic to update or remove
     */
    private void removeComicFromCart(Comic comic) {
        // Evaluate if comic was created by user or it is coming from network
        ComicEntity comicEntity = (ComicEntity) comic;
        Constants.Sections editor = Constants.Sections.getEditorFromName(comicEntity.getEditor());
        switch (editor) {
            case CART:
                // Simply delete comic because was created by user
                deleteData(comicEntity);
                CCLogger.v(TAG, "removeComicFromCart - Comic deleted!");
                break;
            default:
                // Update comic because it is created from web data
                comicEntity.setToCart(!comicEntity.isOnCart());
                updateData(comicEntity);
                CCLogger.v(TAG, "removeComicFromCart - Comic updated!");
                break;
        }
    }

    /**
     * Updating data of comic.
     * @param comicEntity the comic to update
     */
    private void updateData(ComicEntity comicEntity) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ((CCApp) getActivity().getApplication()).getRepository().updateComic(comicEntity);
            }
        });

        WidgetService.updateWidget(getActivity());
    }

    /**
     * Completely delete the comic from database.
     * @param comicEntity the comic to delete
     */
    private void deleteData(ComicEntity comicEntity) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ((CCApp) getActivity().getApplication()).getRepository().deleteComic(comicEntity);
            }
        });

        WidgetService.updateWidget(getActivity());
    }

    /* ****************************************************************************************
     * SwipeRefreshLayout methods
     ******************************************************************************************/

    /**
     * By abstracting the refresh process to a single method, the app allows both the
     * SwipeGestureLayout onRefresh() method and the Refresh action item to refresh the content.
     * @param mEditor the editor picked by user
     */
    private void initiateRefresh(Constants.Sections mEditor) {
        CCLogger.i(TAG, "initiateRefresh - start for editor " + mEditor);

        if (mEditor.equals(Constants.Sections.FAVORITE) || mEditor.equals(Constants.Sections.CART)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_pick_editor_title)
                    .setItems(R.array.pref_available_editors, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position of the selected item
                            CCLogger.v(TAG, "onClick - Selected position " + which);
                            Constants.Sections pickedEditor = null;
                            switch (which) {
                                case 0:
                                    pickedEditor = Constants.Sections.PANINI;
                                    break;
                                case 1:
                                    pickedEditor = Constants.Sections.STAR;
                                    break;
                                case 2:
                                    pickedEditor = Constants.Sections.BONELLI;
                                    break;
                                case 3:
                                    pickedEditor = Constants.Sections.RW;
                                    break;
                            }

                            if (pickedEditor != null) {
                                startRefresh(pickedEditor);
                                dialog.dismiss();
                            }
                        }
                    });
            builder.setNegativeButton(R.string.dialog_undo_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else {
            startRefresh(mEditor);
        }
    }

    /**
     * Method used to start refresh.
     * @param editor the editor picked by user
     */
    private void startRefresh(Constants.Sections editor) {
        // Execute the background task, used on DownloadService to load the data
        Intent intent = new Intent(getActivity(), DownloadService.class);
        intent.putExtra(Constants.ARG_EDITOR, editor);
        intent.putExtra(Constants.MANUAL_SEARCH, true);
        getActivity().startService(intent);

        // Update refresh spinner
        if (isRefreshing()) {
            setRefreshing(false);
        }
    }

    /**
     * Set the {@link android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener} to listen for
     * initiated refreshes.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout#setOnRefreshListener(android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener)
     */
    private void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(listener);
        }
    }

    /**
     * Returns whether the {@link android.support.v4.widget.SwipeRefreshLayout} is currently
     * refreshing or not.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout#isRefreshing()
     */
    public boolean isRefreshing() {
        SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();
        if (swipeRefreshLayout != null) {
            return swipeRefreshLayout.isRefreshing();
        } else {
            CCLogger.v(TAG, "isRefreshing - Returning FALSE because swipe refresh layout is null");
            return false;
        }
    }

    /**
     * Set whether the {@link android.support.v4.widget.SwipeRefreshLayout} should be displaying
     * that it is refreshing or not.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout#setRefreshing(boolean)
     */
    public void setRefreshing(boolean refreshing) {
        SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    /**
     * @return the fragment's {@link android.support.v4.widget.SwipeRefreshLayout} widget.
     */
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }
}
