import android.content.Context;

import org.checklist.comics.comicschecklist.parser.ParserBonelli;
import org.checklist.comics.comicschecklist.parser.ParserPanini;
import org.checklist.comics.comicschecklist.parser.ParserRW;
import org.checklist.comics.comicschecklist.parser.ParserStar;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ParserTest {

    @Mock
    private Context mMockContext;

    @Test
    public void startParsePaniniTest() {
        ParserPanini parser = new ParserPanini(mMockContext);
        boolean result = parser.startParsing();
        assertThat(result, is(true));
    }

    @Test
    public void startParseRWTest() {
        ParserRW parser = new ParserRW(mMockContext);
        boolean result = parser.startParsing();
        assertThat(result, is(true));
    }

    @Test
    public void startParseBonelliTest() {
        ParserBonelli parser = new ParserBonelli(mMockContext);
        boolean result = parser.startParsing();
        assertThat(result, is(false));
    }

    @Test
    public void startParseStarTest() {
        ParserStar parser = new ParserStar(mMockContext);
        boolean result = parser.startParsing();
        assertThat(result, is(true));
    }
}
