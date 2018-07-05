package org.checklist.comics.comicschecklist.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import kotlinx.android.synthetic.main.fragment_add_comic.*

import org.checklist.comics.comicschecklist.CCApp
import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.extensions.lazyLogger
import org.checklist.comics.comicschecklist.widget.WidgetService
import org.checklist.comics.comicschecklist.util.Constants
import org.checklist.comics.comicschecklist.util.DateCreator

import org.jetbrains.anko.debug
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Fragment which manage the comic creation.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class FragmentAddComic : Fragment() {

    private var mComicId = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_comic, container, false)
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
                    dateTextView.text = DateCreator.elaborateDate(comicEntity.releaseDate)
                }
            }
        } else {
            // Leave all form in blank and set default date
            updateDate(DateCreator.getTodayString())
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
        val date = dateTextView!!.text.toString()

        if (info.isNotEmpty()) {
            // Save data if there is at least some info and put a default title
            if (name.isEmpty()) {
                name = getString(R.string.text_default_title)
            }

            // Insert new entry
            val comicEntity = ComicEntity(name,
                    DateCreator.elaborateDate(date),
                    info,
                    "N.D.",
                    "N.D.",
                    "N.D.",
                    Constants.Sections.getName(Constants.Sections.CART),
                    false, false,
                    "")

            when (mComicId) {
                -1 -> {
                    doAsync {
                        mComicId = CCApp.instance.repository.insertComic(comicEntity).toInt()
                        LOG.debug("onPause - INSERTED new entry on database with ID $mComicId")
                    }
                }
                else -> {
                    doAsync {
                        CCApp.instance.repository.updateComic(comicEntity)
                        LOG.debug("onPause - UPDATED entry on database with ID $mComicId")
                    }
                }
            }

            // Update widget
            WidgetService.updateWidget(activity)
        }
        super.onPause()
    }

    fun updateDate(date: String) {
        dateTextView.text = date
    }

    companion object {

        val LOG by lazyLogger()
        private const val ARG_SAVED_COMIC_ID = "comic_id"
    }
}
