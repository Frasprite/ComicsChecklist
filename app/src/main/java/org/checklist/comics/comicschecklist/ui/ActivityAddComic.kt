package org.checklist.comics.comicschecklist.ui

import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.app_bar_detail.*

import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.util.Constants

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
        supportFragmentManager.beginTransaction().add(R.id.comicDetailContainer, fragment, "addComicFragment").commit()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            NavUtils.navigateUpTo(this, Intent(this, ActivityMain::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
