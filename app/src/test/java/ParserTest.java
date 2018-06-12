import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.parser.ParserBonelli;
import org.checklist.comics.comicschecklist.parser.ParserPanini;
import org.checklist.comics.comicschecklist.parser.ParserRW;
import org.checklist.comics.comicschecklist.parser.ParserStar;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertNotNull;

public class ParserTest {

    @Test
    public void startParsePaniniTest() {
        ParserPanini parser = new ParserPanini();
        ArrayList<ComicEntity> result = parser.initParser();
        assertNotNull(result);
    }

    @Test
    public void startParseRWTest() {
        ParserRW parser = new ParserRW();
        ArrayList<ComicEntity> result = parser.initParser();
        assertNotNull(result);
    }

    @Test
    public void startParseBonelliTest() {
        ParserBonelli parser = new ParserBonelli();
        ArrayList<ComicEntity> result = parser.initParser();
        assertNotNull(result);
    }

    @Test
    public void startParseStarTest() {
        ParserStar parser = new ParserStar();
        ArrayList<ComicEntity> result = parser.initParser();
        assertNotNull(result);
    }
}
