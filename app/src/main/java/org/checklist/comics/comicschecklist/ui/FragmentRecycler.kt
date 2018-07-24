package org.checklist.comics.comicschecklist.ui

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import kotlinx.android.synthetic.main.fragment_recycler_view.*

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.databinding.FragmentRecyclerViewBinding
import org.checklist.comics.comicschecklist.widget.WidgetService
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.util.Constants
import org.checklist.comics.comicschecklist.viewmodel.ComicListViewModel

import org.jetbrains.anko.doAsync

/**
 * A fragment representing a list of Comics. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a [FragmentDetail].
 */
class FragmentRecycler : Fragment() {

    private lateinit var mComicAdapter: ComicAdapter
    private var mBinding: FragmentRecyclerViewBinding? = null

    private val sItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            // Remove swiped item from list
            val position = viewHolder.adapterPosition
            val comic = mComicAdapter.mComicList[position]
            CCLogger.v(TAG, "onSwiped - Comic ID " + comic.id)
            updateComic(comic)
        }
    }

    /**
     * Returns whether the [android.support.v4.widget.SwipeRefreshLayout] is currently
     * refreshing or not.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout.isRefreshing
     */
    /**
     * Set whether the [android.support.v4.widget.SwipeRefreshLayout] should be displaying
     * that it is refreshing or not.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout.setRefreshing
     */
    var isRefreshing: Boolean
        get() = swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing
        set(refreshing) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.isRefreshing = refreshing
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        CCLogger.d(TAG, "onCreateView - start")
        // Create the list fragment's content view by calling the super method
        super.onCreateView(inflater, container, savedInstanceState)

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recycler_view, container, false)

        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find editor
        val editor = arguments!!.getSerializable(Constants.ARG_EDITOR) as Constants.Sections
        CCLogger.d(TAG, "onViewCreated - name $editor")

        // Set proper empty view
        when (editor) {
            Constants.Sections.FAVORITE -> {
                emptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_empty_list_stub, 0, 0)
                emptyTextView.text = getString(R.string.empty_favorite_list)
            }
            Constants.Sections.CART -> {
                emptyTextView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_empty_list_stub, 0, 0)
                emptyTextView.text = getString(R.string.empty_cart_list)
            }
            else -> emptyTextView.text = getString(R.string.empty_editor_list)
        }

        // Init adapter
        mComicAdapter = ComicAdapter(object : ComicClickCallback {
            override fun onClick(comic: ComicEntity) {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    (activity as ActivityMain).launchDetailView(comic)
                }
            }
        })
        recyclerView.adapter = mComicAdapter

        // Attach swipe to delete (right / left) if we are in CART or FAVORITE section
        if (editor == Constants.Sections.CART || editor == Constants.Sections.FAVORITE) {
            val itemTouchHelper = ItemTouchHelper(sItemTouchCallback)
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }

        // Enable swipe to refresh if we are on other categories
        if (editor == Constants.Sections.FAVORITE || editor == Constants.Sections.CART) {
            CCLogger.v(TAG, "onViewCreated - Locking swipe to refresh")
            swipeRefreshLayout.isEnabled = false
        }

        // Set color of progress view
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_light, R.color.primary, R.color.primary_dark, R.color.accent)

        setRecyclerViewLayoutManager(recyclerView)

        /*
          Implement {@link SwipeRefreshLayout.OnRefreshListener}. When users do the "swipe to
          refresh" gesture, SwipeRefreshLayout invokes
          {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}. In
          {@link SwipeRefreshLayout.OnRefreshListener#onRefresh onRefresh()}, call a method that
          refreshes the content. Call the same method in response to the Refresh action from the
          action bar.
         */
        setOnRefreshListener { (activity as ActivityMain).initiateRefresh(editor) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(ComicListViewModel::class.java)

        subscribeUi(viewModel, null, arguments!!.getSerializable(Constants.ARG_EDITOR) as Constants.Sections)
    }

    /**
     * Set the layout type of [RecyclerView].
     * @param recyclerView the current recycler view where to set layout
     */
    private fun setRecyclerViewLayoutManager(recyclerView: RecyclerView) {
        var scrollPosition = 0

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.layoutManager != null) {
            scrollPosition = (recyclerView.layoutManager as LinearLayoutManager)
                    .findFirstCompletelyVisibleItemPosition()
        }

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.scrollToPosition(scrollPosition)
    }

    /**
     * Method which observe the status of database and update UI when data is available.<br></br>
     * If {@param text} is not null, the list will be filtered based on editor and part of text specified.
     * @param viewModel the view model which store and manage the data to show
     * @param text the part of text to search on database
     */
    private fun subscribeUi(viewModel: ComicListViewModel, text: String?, editor: Constants.Sections) {
        // Update the list when the data changes
        if (text == null) {
            when (editor) {
                Constants.Sections.FAVORITE -> viewModel.getFavoriteComics()
                Constants.Sections.CART -> viewModel.getWishlistComics()
                else -> viewModel.filterByEditor(editor.getName())
            }
        } else {
            viewModel.filterComicsContainingText(editor.getName(), text)
        }

        viewModel.comics.observe(this, Observer<List<ComicEntity>> {
            myComics ->
            if (myComics != null) {
                mBinding!!.isLoading = myComics.isEmpty()
                mComicAdapter.setComicList(myComics)
            } else {
                mBinding!!.isLoading = true
            }
            // Espresso does not know how to wait for data binding's loop so we execute changes
            // sync.
            mBinding!!.executePendingBindings()
        })
    }

    /**
     * Method used to update the list after user gave an input on search view.
     * @param newText the text to use for filter data
     */
    fun updateList(newText: String) {
        val viewModel = ViewModelProviders.of(this).get(ComicListViewModel::class.java)

        subscribeUi(viewModel, newText, arguments!!.getSerializable(Constants.ARG_EDITOR) as Constants.Sections)
    }

    /**
     * Update a changes of favorite or wished comic on database.
     * @param comic the entry to update on database
     */
    fun updateComic(comic: ComicEntity) {
        if (comic.editor == Constants.Sections.FAVORITE.getName()) {
            CCLogger.d(TAG, "deleteComic - Removing favorite comic with ID " + comic.id)
            // Remove comic from favorite
            comic.isFavorite = !comic.isFavorite
            updateData(comic)
        } else if (comic.editor == Constants.Sections.CART.getName()) {
            CCLogger.d(TAG, "onContextItemSelected - Removing comic in cart with ID " + comic.id)
            // Remove comic from cart
            removeComicFromCart(comic)
        }

        WidgetService.updateWidget(activity)
    }

    /**
     * Method used to handle update operation on comic which is on cart list.<br></br>
     * If the comic was created by user, it will be deleted; otherwise it is just updated.
     * @param comic the comic to update or remove
     */
    private fun removeComicFromCart(comic: ComicEntity) {
        // Evaluate if comic was created by user or it is coming from network
        val editor = Constants.Sections.getEditorFromName(comic.editor)
        when (editor) {
            Constants.Sections.CART -> {
                // Simply delete comic because was created by user
                deleteData(comic)
                CCLogger.v(TAG, "removeComicFromCart - Comic deleted!")
            }
            else -> {
                // Update comic because it is created from web data
                comic.setToCart(!comic.isOnCart)
                updateData(comic)
                CCLogger.v(TAG, "removeComicFromCart - Comic updated!")
            }
        }
    }

    /**
     * Updating data of comic.
     * @param comicEntity the comic to update
     */
    private fun updateData(comicEntity: ComicEntity) {
        doAsync { (activity!!.application as CCApp).repository.updateComic(comicEntity) }

        WidgetService.updateWidget(activity)
    }

    /**
     * Completely delete the comic from database.
     * @param comicEntity the comic to delete
     */
    private fun deleteData(comicEntity: ComicEntity) {
        doAsync { (activity!!.application as CCApp).repository.deleteComic(comicEntity) }

        WidgetService.updateWidget(activity)
    }

    /* ****************************************************************************************
     * SwipeRefreshLayout methods
     ******************************************************************************************/

    /**
     * Set the [android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener] to listen for
     * initiated refreshes.
     *
     * @see android.support.v4.widget.SwipeRefreshLayout.setOnRefreshListener
     */
    private fun setOnRefreshListener(listener: () -> Unit) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(listener)
        }
    }

    companion object {

        private val TAG = FragmentRecycler::class.java.simpleName

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        fun newInstance(section: Constants.Sections): FragmentRecycler {
            CCLogger.v(TAG, "newInstance - " + section.toString())
            val fragment = FragmentRecycler()
            val args = Bundle()
            args.putSerializable(Constants.ARG_EDITOR, section)
            fragment.arguments = args
            return fragment
        }
    }
}
