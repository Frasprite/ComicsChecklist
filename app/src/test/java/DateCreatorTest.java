import org.checklist.comics.comicschecklist.util.DateCreator;
import org.junit.Test;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class DateCreatorTest {

    @Test
    public void elaborateDateValidator() {
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse("01/02/2017");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assertEquals(DateCreator.elaborateDate("01/02/2017"), date);
    }
}
