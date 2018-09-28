package org.checklist.comics.comicschecklist.ui

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
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
import org.checklist.comics.comicschecklist.service.Message
import org.checklist.comics.comicschecklist.service.ServiceEvents
import org.checklist.comics.comicschecklist.util.Constants
import org.checklist.comics.comicschecklist.util.Filter
import org.checklist.comics.comicschecklist.viewmodel.ComicListViewModel

import org.jetbrains.anko.doAsync

/**
 * A fragment representing a list of Comics. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a [FragmentDetail].
 */
class FragmentRecycler : androidx.fragment.app.Fragment() {

    private lateinit var mComicAdapter: ComicAdapter
    private var mBinding: FragmentRecyclerViewBinding? = null

    private val sItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, swipeDir: Int) {
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
    private var isRefreshing: Boolean
        get() = swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing
        set(refreshing) {
            activity?.runOnUiThread {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.isRefreshing = refreshing
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Listen for MessageEvents only
        ServiceEvents.listen(Message::class.java).subscribe {
            isRefreshing = when (it.result) {
                Constants.RESULT_START -> true
                else -> false
            }
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

        enableSwipeToRefresh(editor)

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
        setOnRefreshListener { (activity as ActivityMain).initiateRefresh() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(ComicListViewModel::class.java)

        subscribeUi(viewModel = viewModel, editor = arguments!!.getSerializable(Constants.ARG_EDITOR) as Constants.Sections)
    }

    override fun onPause() {
        super.onPause()
        // Stop animation
        isRefreshing = false
    }

    /**
     * Set the layout type of [RecyclerView].
     * @param recyclerView the current recycler view where to set layout
     */
    private fun setRecyclerViewLayoutManager(recyclerView: androidx.recyclerview.widget.RecyclerView) {
        var scrollPosition = 0

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.layoutManager != null) {
            scrollPosition = (recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager)
                    .findFirstCompletelyVisibleItemPosition()
        }

        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)
        recyclerView.scrollToPosition(scrollPosition)
    }

    /**
     * Method which observe the status of database and update UI when data is available.<br></br>
     * If {@param text} is not null, the list will be filtered based on editor and part of text specified.
     * @param viewModel the view model which store and manage the data to show
     * @param text the part of text to search on database
     */
    private fun subscribeUi(viewModel: ComicListViewModel, text: String = "", editor: Constants.Sections) {
        CCLogger.v(TAG, "subscribeUi - Text $text and editor $editor")

        CCApp.instance.repository.filterComics(Filter(editor, text))

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
     * Update a changes of favorite or wished comic on database.
     * @param comic the entry to update on database
     */
    fun updateComic(comic: ComicEntity) {
        val editor = Constants.Sections.fromName(comic.editor)
        when (editor) {
            Constants.Sections.FAVORITE -> {
                CCLogger.d(TAG, "deleteComic - Removing favorite comic with ID " + comic.id)
                // Remove comic from favorite
                doAsync {
                    (activity?.application as CCApp).repository.updateFavorite(comic.id, !comic.isFavorite)
                }
            }
            Constants.Sections.CART -> {
                CCLogger.d(TAG, "onContextItemSelected - Removing comic in cart with ID " + comic.id)
                // Remove comic from cart
                removeComicFromCart(comic)
            }
            else -> {
                // Do nothing for other cases
            }
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
        val editor = Constants.Sections.fromName(comic.editor)
        when (editor) {
            Constants.Sections.CART -> {
                // Simply delete comic because was created by user
                deleteData(comic)
                CCLogger.v(TAG, "removeComicFromCart - Comic deleted!")
            }
            else -> {
                // Update comic because it is created from web data
                doAsync {
                    (activity?.application as CCApp).repository.updateCart(comic.id, !comic.isOnCart)
                }
                CCLogger.v(TAG, "removeComicFromCart - Comic updated!")
            }
        }
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

    fun enableSwipeToRefresh(editor: Constants.Sections) {
        CCLogger.v(TAG, "enableSwipeToRefresh - Checking if swipe to refresh must be locked for $editor")
        // Enable swipe to refresh if we are on other categories
        swipeRefreshLayout?.isEnabled = !(editor == Constants.Sections.FAVORITE || editor == Constants.Sections.CART)
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
