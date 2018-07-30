import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.contrib.DrawerMatchers.isClosed
import android.support.test.espresso.contrib.NavigationViewActions
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v4.widget.DrawerLayout
import android.support.test.InstrumentationRegistry

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
        val viewById = activity.findViewById<DrawerLayout>(R.id.drawerLayout)
        assertThat(viewById, notNullValue())
        assertThat(viewById, instanceOf(DrawerLayout::class.java))
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

}
