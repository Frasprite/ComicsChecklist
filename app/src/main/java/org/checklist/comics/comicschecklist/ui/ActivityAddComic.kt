package org.checklist.comics.comicschecklist.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker

import kotlinx.android.synthetic.main.app_bar_detail.*

import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.util.Constants
import org.checklist.comics.comicschecklist.util.DateCreator

/**
 * Activity used to add a comic on database.
 */
class ActivityAddComic : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_bar_detail)

        setSupportActionBar(toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        // Create the detail fragment and add it to the activity using a fragment transaction.
        val arguments = Bundle()
        arguments.putInt(Constants.ARG_COMIC_ID, intent.getIntExtra(Constants.ARG_COMIC_ID, -1))
        val fragment = FragmentAddComic()
        fragment.arguments = arguments
        supportFragmentManager.beginTransaction().add(R.id.comic_detail_container, fragment, "addComicFragment").commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpTo(this, Intent(this, ActivityMain::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun changeDate(view: View) {
        when (view.id) {
            R.id.buttonChangeData -> {
                val newFragment = DatePickerFragment()
                newFragment.show(supportFragmentManager, "datePicker")
            }
        }
    }

    class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current date as the default date in the picker
            val year = DateCreator.getCurrentYear()
            val month = DateCreator.getCurrentMonth()
            val day = DateCreator.getCurrentDay()

            // Create a new instance of DatePickerDialog and return it
            CCLogger.d(TAG, "onCreateDialog - date is $day/$month/$year")
            return DatePickerDialog(activity!!, this, year, month, day)
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            // Set chosen date to text view
            val articleFrag = activity!!.supportFragmentManager.findFragmentByTag("addComicFragment") as FragmentAddComic
            val date = DateCreator.elaborateDate(year, month, day)
            CCLogger.i(TAG, "onDateSet - returning $date")
            articleFrag.updateDate(date)
        }
    }

    companion object {

        private val TAG = ActivityAddComic::class.java.simpleName
    }
}
