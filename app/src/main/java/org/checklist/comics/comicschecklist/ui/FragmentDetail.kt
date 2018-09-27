package org.checklist.comics.comicschecklist.ui

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_detail.*

import org.checklist.comics.comicschecklist.CCApp

import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.databinding.FragmentDetailBinding
import org.checklist.comics.comicschecklist.widget.WidgetService
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.util.Constants
import org.checklist.comics.comicschecklist.viewmodel.ComicViewModel

import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast

/**
 * A fragment representing a single Comic detail screen.
 * This fragment is either contained in a [ActivityMain]
 * in two-pane mode (on tablets) or a [ActivityDetail]
 * on handsets.
 */
class FragmentDetail : androidx.fragment.app.Fragment(), View.OnClickListener {

    private var mBinding: FragmentDetailBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false)

        return mBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        site.setOnClickListener(this)
        calendar.setOnClickListener(this)
        favorite.setOnClickListener(this)
        buy.setOnClickListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val comicId = arguments?.getInt(Constants.ARG_COMIC_ID, -1) ?: -1
        CCLogger.i(TAG, "onCreate - comic with ID $comicId")

        val factory = ComicViewModel.Factory(activity!!.application, comicId)

        val model = ViewModelProviders.of(this, factory)
                .get(ComicViewModel::class.java)

        mBinding?.comicViewModel = model

        subscribeToModel(model)
    }

    /**
     * Method which observe the item data.
     * @param model the view model which store and manage the data to show
     */
    private fun subscribeToModel(model: ComicViewModel) {
        model.observableComic.observe(this, Observer<ComicEntity> {
            if (it != null) {
                model.setComic(it)
            }
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.site -> goToSite()
            R.id.calendar -> createEvent()
            R.id.favorite -> manageFavorite()
            R.id.buy -> manageWishlist()
        }
    }

    private fun goToSite() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(
                mBinding?.comicViewModel?.comic?.get()?.url
        ))
        startActivity(browserIntent)
    }

    private fun createEvent() {
        try {
            CCLogger.i(TAG, "createEvent - Add event on calendar")
            // ACTION_INSERT does not work on all phones; use Intent.ACTION_EDIT in this case
            val intent = Intent(Intent.ACTION_INSERT)
            intent.type = "vnd.android.cursor.item/event"
            intent.putExtra(CalendarContract.Events.TITLE, mBinding?.comicViewModel?.comic?.get()?.name)
            intent.putExtra(CalendarContract.Events.DESCRIPTION, getString(R.string.calendar_release))

            // Setting dates
            val timeInMillis = mBinding?.comicViewModel?.comic?.get()?.releaseDate?.time
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, timeInMillis)
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, timeInMillis)

            // Make it a full day event
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)

            // Making it private and shown as busy
            intent.putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
            intent.putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            startActivity(intent)
        } catch (e: Exception) {
            activity?.toast(R.string.calendar_error)
        }

    }

    private fun manageFavorite() {
        var isAFavoriteComic = mBinding?.comicViewModel?.comic?.get()?.isFavorite ?: false

        if (!isAFavoriteComic) {
            CCLogger.i(TAG, "manageFavorite - Add comic to favorite")
            isAFavoriteComic = true
            // Add comic to favorite
            activity?.toast(resources.getString(R.string.comic_added_favorite))
        } else {
            CCLogger.i(TAG, "manageFavorite - Delete from favorite")
            isAFavoriteComic = false
            // Delete from favorite
            activity?.toast(resources.getString(R.string.comic_deleted_favorite))
        }

        val comicEntity = mBinding?.comicViewModel?.comic?.get()

        doAsync {
            (activity?.application as CCApp).repository.updateFavorite(comicEntity!!.id, isAFavoriteComic)
        }

        WidgetService.updateWidget(activity)
    }

    private fun manageWishlist() {
        var isComicOnCart = mBinding?.comicViewModel?.comic?.get()?.isOnCart ?: false

        if (!isComicOnCart) {
            CCLogger.i(TAG, "manageWishlist - Update entry on comic database: add to cart")
            isComicOnCart = true
            // Update entry on comic database
            activity?.toast(resources.getString(R.string.comic_added_cart))
        } else {
            CCLogger.i(TAG, "manageWishlist - Update entry on comic database: remove from cart")
            isComicOnCart = false
            // Update entry on comic database
            activity?.toast(resources.getString(R.string.comic_deleted_cart))
        }

        val comicEntity = mBinding?.comicViewModel?.comic?.get()

        doAsync {
            (activity?.application as CCApp).repository.updateCart(comicEntity!!.id, isComicOnCart)
        }

        WidgetService.updateWidget(activity)
    }

    companion object {

        private val TAG = FragmentDetail::class.java.simpleName
    }
}
