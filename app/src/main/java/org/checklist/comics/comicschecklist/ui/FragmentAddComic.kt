package org.checklist.comics.comicschecklist.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.transitionseverywhere.ChangeBounds
import com.transitionseverywhere.Fade
import com.transitionseverywhere.TransitionManager
import com.transitionseverywhere.TransitionSet
import com.transitionseverywhere.extra.Scale

import kotlinx.android.synthetic.main.fragment_add_comic.*

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.extensions.lazyLogger
import org.checklist.comics.comicschecklist.widget.WidgetService
import org.checklist.comics.comicschecklist.util.Constants

import org.jetbrains.anko.debug
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jetbrains.anko.uiThread
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*


/**
 * Fragment which manage the comic creation.
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class FragmentAddComic : Fragment() {

    private var mComicId = -1
    private var mDateTime: DateTime = DateTime()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_comic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonChangeData.setOnClickListener {
                    openCalendar()
        }

        datePicker.setFirstVisibleDate(mDateTime.year, (mDateTime.monthOfYear - 1), mDateTime.dayOfMonth)

        datePicker.setOnDateSelectedListener { year, month, day, _ -> updateDate(year, month, day) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (arguments!!.containsKey(Constants.ARG_COMIC_ID)) {
            // Load comic content specified by the fragment arguments from ComicContentProvider.
            mComicId = arguments!!.getInt(Constants.ARG_COMIC_ID, -1)
            LOG.debug("onActivityCreated - mComicId (initiated from ARGUMENTS) = $mComicId")
        }

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mComicId = savedInstanceState.getInt(ARG_SAVED_COMIC_ID, -1)
            LOG.debug("onActivityCreated - mComicId (initiated from BUNDLE) = $mComicId")
        }
    }

    override fun onResume() {
        super.onResume()
        // Load data from database if ID is passed from activity
        if (mComicId > -1) {
            doAsync {
                val comicEntity = (activity!!.application as CCApp).repository.loadComicSync(mComicId)
                LOG.debug("loadComicWithID - Comic : $comicEntity")
                uiThread {
                    nameEditText.setText(comicEntity.name)
                    infoEditText.setText(comicEntity.description)
                    mDateTime = DateTime(comicEntity.releaseDate.time)
                    datePicker.setFirstVisibleDate(mDateTime.year, (mDateTime.monthOfYear -1), mDateTime.dayOfMonth)
                    val dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
                    buttonChangeData.text = dateTimeFormatter.print(mDateTime)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        LOG.debug("onSaveInstanceState - saving mComicId $mComicId")
        outState.putInt(ARG_SAVED_COMIC_ID, mComicId)
    }

    override fun onPause() {
        var name = nameEditText!!.text.toString()
        val info = infoEditText!!.text.toString()
        LOG.info { "onPause - Saving data $name $info" }

        if (info.isNotEmpty()) {
            // Save data if there is at least some info and put a default title
            if (name.isEmpty()) {
                name = getString(R.string.text_default_title)
            }

            when (mComicId) {
                -1 -> {
                    doAsync {
                        // Create new entry
                        val comicEntity = ComicEntity(name,
                                Date(mDateTime.millis),
                                info,
                                "N.D.",
                                "N.D.",
                                "N.D.",
                                Constants.Sections.CART.sectionName,
                                false, false,
                                "")
                        mComicId = CCApp.instance.repository.insertComic(comicEntity).toInt()
                        LOG.info("onPause - INSERTED new entry on database with ID $mComicId")
                    }
                }
                else -> {
                    doAsync {
                        CCApp.instance.repository.updateComic(mComicId, name, info, Date(mDateTime.millis))
                        LOG.info("onPause - UPDATED entry on database with ID $mComicId")
                    }
                }
            }

            // Update widget
            WidgetService.updateWidget(context)
        }
        super.onPause()
    }

    private fun openCalendar() {
        val transitionForGrid = TransitionSet()
                .addTransition(Scale(0.5f))
                .addTransition(Fade())
                .setInterpolator(LinearOutSlowInInterpolator())

        val transitionForCardView = TransitionSet()
                .addTransition(ChangeBounds())
                .setInterpolator(LinearOutSlowInInterpolator())

        TransitionManager.beginDelayedTransition(cardContainer, transitionForGrid)
        TransitionManager.beginDelayedTransition(datePickerContainer, transitionForCardView)

        datePicker.visibility = if (datePicker.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun updateDate(year: Int, month: Int, day: Int) {
        mDateTime = DateTime(year, (month + 1), day, 0, 0)
        val dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        buttonChangeData.text = dateTimeFormatter.print(mDateTime)
    }

    companion object {

        val LOG by lazyLogger()
        private const val ARG_SAVED_COMIC_ID = "comic_id"
    }
}
