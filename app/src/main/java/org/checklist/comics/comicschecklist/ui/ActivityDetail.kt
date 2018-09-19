package org.checklist.comics.comicschecklist.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

import kotlinx.android.synthetic.main.app_bar_detail.*

import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.service.DownloadService
import org.checklist.comics.comicschecklist.service.Message
import org.checklist.comics.comicschecklist.service.ServiceEvents
import org.checklist.comics.comicschecklist.util.Constants

/**
 * An activity representing a single Comic detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [ActivityMain].
 *
 *
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a [FragmentDetail].
 */
class ActivityDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_bar_detail)

        setSupportActionBar(toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity using a fragment transaction.
            val arguments = Bundle()
            arguments.putInt(Constants.ARG_COMIC_ID, intent.getIntExtra(Constants.ARG_COMIC_ID, -1))
            val fragment = FragmentDetail()
            fragment.arguments = arguments
            supportFragmentManager.beginTransaction().add(R.id.comicDetailContainer, fragment).commit()
        }

        // Listen for MessageEvents only
        ServiceEvents.listen(Message::class.java).subscribe {
            inspectResultCode(it.result, it.editor)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, Intent(this, ActivityMain::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /**
     * Method used to inspect messages from [DownloadService].
     * @param result the result indicating download status
     * @param currentEditor the editor searched
     */
    private fun inspectResultCode(result: Int, currentEditor: String?) {
        CCLogger.v(TAG, "inspectResultCode - Current editor $currentEditor")
        when (result) {
            Constants.RESULT_DESTROYED -> {
                // Close activity details due to entity primary key (could be changed)
                CCLogger.i(TAG, "inspectResultCode - Service destroyed, closing detail activity")
                this.finish()
            }
        }
    }

    companion object {
        private val TAG = ActivityDetail::class.java.simpleName
    }
}
