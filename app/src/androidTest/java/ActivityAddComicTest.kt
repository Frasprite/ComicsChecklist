import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4

import org.checklist.comics.comicschecklist.R

import org.checklist.comics.comicschecklist.ui.ActivityAddComic

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityAddComicTest {

    @Rule
    @JvmField
    val mActivityRule = ActivityTestRule(ActivityAddComic::class.java)

    @Test
    fun checkRateDialogTest() {
        onView(withId(R.id.nameEditText)).perform(typeText("SpiderMan"), closeSoftKeyboard())
        val description = "With great power there must also come great responsibility..\nFrom 09/2014 til now."
        onView(withId(R.id.infoEditText)).perform(typeText(description), closeSoftKeyboard())
        onView(isRoot()).perform(ViewActions.pressBack())
    }
}