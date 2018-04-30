import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.checklist.comics.comicschecklist.ui.ActivityMain;
import org.checklist.comics.comicschecklist.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;

@RunWith(AndroidJUnit4.class)
public class ActivityMainTest {

    @Rule
    public ActivityTestRule<ActivityMain> mActivityRule = new ActivityTestRule<>(ActivityMain.class);

    @Test
    public void checkRateDialogTest() {
        onView(withText(R.string.dialog_rate_text)).check(doesNotExist());
        //onView(withId(android.R.id.button1)).perform(click());
        //onView(withId(android.R.id.button2)).perform(click());
        //onView(withId(android.R.id.button3)).perform(click());
    }

    @Test
    public void listItemClickTest() {
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(0).perform(click());
    }

}
