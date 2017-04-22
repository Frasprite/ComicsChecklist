import android.content.Context;

import org.checklist.comics.comicschecklist.parser.Parser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ParserTest {

    @Mock
    Context mMockContext;

    @Test
    public void startParsePaniniTest() {
        Parser parser = new Parser(mMockContext);
        boolean result = parser.startParsePanini();
        assertThat(result, is(true));
    }

    @Test
    public void startParseRWTest() {
        Parser parser = new Parser(mMockContext);
        boolean result = parser.startParseRW();
        assertThat(result, is(true));
    }

    @Test
    public void startParseBonelliTest() {
        Parser parser = new Parser(mMockContext);
        boolean result = parser.startParseBonelli();
        assertThat(result, is(false));
    }

    @Test
    public void startParseStarTest() {
        Parser parser = new Parser(mMockContext);
        boolean result = parser.startParseStarC();
        assertThat(result, is(true));
    }
}
