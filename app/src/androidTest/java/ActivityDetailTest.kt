import android.support.test.espresso.intent.Intents
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.checklist.comics.comicschecklist.ui.ActivityDetail
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.RootMatchers.withDecorView
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import org.checklist.comics.comicschecklist.R
import org.hamcrest.Matchers.not
import org.junit.Test


@RunWith(AndroidJUnit4::class)
class ActivityDetailTest {

    @Rule
    @JvmField
    var mActivityRule: ActivityTestRule<ActivityDetail> = object : ActivityTestRule<ActivityDetail>(ActivityDetail::class.java) {
        override fun getActivityIntent(): Intent {
            val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
            val result = Intent(targetContext, ActivityDetail::class.java)
            result.putExtra("comic_id", 6)
            return result
        }
    }

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Intents.init()
    }

    @Test
    fun addToFavoriteTest() {
        // This will fail or succeed based on comic favorite flag
        onView(ViewMatchers.withId(R.id.favorite)).perform(ViewActions.click())
        onView(withText(R.string.comic_added_favorite)).inRoot(withDecorView(not(mActivityRule.activity.window.decorView))).check(matches(isDisplayed()))
    }

    @Test
    fun addToCartTest() {
        // This will fail or succeed based on comic cart flag
        onView(ViewMatchers.withId(R.id.buy)).perform(ViewActions.click())
        onView(withText(R.string.comic_added_cart)).inRoot(withDecorView(not(mActivityRule.activity.window.decorView))).check(matches(isDisplayed()))
    }
}