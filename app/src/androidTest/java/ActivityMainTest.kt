import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.drawerlayout.widget.DrawerLayout
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.action.ViewActions.typeText

import android.view.Gravity

import org.checklist.comics.comicschecklist.R
import org.checklist.comics.comicschecklist.ui.ActivityAddComic
import org.checklist.comics.comicschecklist.ui.ActivityMain

import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.notNullValue

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.anything


@RunWith(AndroidJUnit4::class)
class ActivityMainTest {

    @Rule
    @JvmField
    val mActivityRule = ActivityTestRule(ActivityMain::class.java)

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Intents.init()
    }

    @Test
    fun checkRateDialogTest() {
        onView(withText(R.string.dialog_rate_text)).check(doesNotExist())
    }

    @Test
    fun searchStoreTest() {
        onView(withId(R.id.searchStore)).perform(click())
    }

    @Test
    fun addComicTest() {
        onView(withId(R.id.addComic)).perform(click())
        intended(hasComponent(ActivityAddComic::class.java.name))
    }

    @Test
    @Throws(Exception::class)
    fun ensureDrawerLayoutTest() {
        val activity = mActivityRule.activity
        val viewById = activity.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawerLayout)
        assertThat(viewById, notNullValue())
        assertThat(viewById, instanceOf(androidx.drawerlayout.widget.DrawerLayout::class.java))
    }

    @Test
    fun navigationViewTest() {
        // Open Drawer to click on navigation
        onView(withId(R.id.drawerLayout))
                .check(matches(isClosed(Gravity.LEFT))) // Left Drawer should be closed
                .perform(DrawerActions.open()) // Open Drawer

        // Start the screen of your activity
        onView(withId(R.id.navigationView))
                .perform(NavigationViewActions.navigateTo(R.id.list_cart))

        // Check that you Activity was opened
        val expectedNoStatisticsText = InstrumentationRegistry.getTargetContext()
                .getString(R.string.empty_cart_list)
        onView(withId(R.id.emptyTextView)).check(matches(withText(expectedNoStatisticsText)))
    }

    @Test
    fun scrollToBottomTest() {
        onView(withClassName(`is`(androidx.recyclerview.widget.RecyclerView::class.java.canonicalName)))
                .perform(scrollToPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(45))
                .check(matches(anything()))
    }

    @Test
    fun clickItemTest() {
        // First scroll to the position that needs to be matched and click on it.
        onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(1, click()))
    }

    @Test
    fun searchTest() {
        onView(withId(R.id.search)).perform(click())
        onView(withId(com.google.android.material.R.id.search_src_text)).perform(typeText("spider"))
        onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<androidx.recyclerview.widget.RecyclerView.ViewHolder>(0, click()))
    }

}
