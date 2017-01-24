import org.checklist.comics.comicschecklist.util.DateCreator;
import org.junit.Test;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DateCreatorTest {

    @Test
    public void elaborateDateFromString() {
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse("01/02/2017");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assertEquals(DateCreator.elaborateDate("01/02/2017"), date);
    }

    @Test
    public void elaborateDateFromIntegers() {
        assertEquals(DateCreator.elaborateDate(2017, 1, 1), "01/02/2017");
    }

    @Test
    public void elaborateHumanDate() {
        assertEquals(DateCreator.elaborateHumanDate("1/2/2017"), "01/02/2017");
    }

    @Test
    public void elaborateTodayString() {
        assertEquals(DateCreator.getTodayString(), "16/01/2017");
    }

    @Test
    public void elaborateGetCurrentDay() {
        assertEquals(DateCreator.getCurrentDay(), 23);
    }

    @Test
    public void elaborateGetCurrentMonth() {
        assertEquals(DateCreator.getCurrentMonth(), 0);
    }

    @Test
    public void elaborateGetCurrentYear() {
        assertEquals(DateCreator.getCurrentYear(), 2017);
    }

    @Test
    public void elaborateGetCurrentReadableMonth() {
        assertEquals(DateCreator.getCurrentReadableMonth(), "marzo");
    }

    @Test
    public void elaborateGetTimeInMillis() {
        // Check if date is correct (superior to 1/1/1970)
        assertTrue("Failed, check the date!", DateCreator.getTimeInMillis("16/01/1988") > 0);
    }

    @Test
    public void elaborateGetPastDay() {
        // This will give an error, due to the difference of hours/minutes/seconds
        assertEquals(DateCreator.getPastDay(5), DateCreator.elaborateDate("11/01/2017"));
    }
}
