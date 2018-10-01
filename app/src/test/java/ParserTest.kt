import org.checklist.comics.comicschecklist.parser.*

import org.jsoup.nodes.Element
import org.junit.Assert.*
import org.junit.Test

import java.util.*

class ParserTest {

    @Test
    fun startParsePaniniTest() {
        val parser = ParserPanini()
        val result = parser.initParser()
        assertNotNull(result)
    }

    @Test
    fun startParseRWTest() {
        val parser = ParserRW()
        val result = parser.initParser()
        assertNotNull(result)
    }

    @Test
    fun startParseBonelliTest() {
        val parser = ParserBonelli()
        val result = parser.initParser()
        assertNotNull(result)
    }

    @Test
    fun startParseStarTest() {
        val parser = ParserStar()
        val result = parser.initParser()
        assertNotNull(result)
    }

    @Test
    fun elaborateDateTest() {
        val parser = ParserStar()
        val result = parser.elaborateDate("06/08/2018")
        val date = Date(1533506400000)
        assertEquals(result, date)
    }

    @Test
    fun searchMoreInfoTest() {
        // Using reflection in order to test private method
        val method = ParserPanini::class.java.getDeclaredMethod(
                "searchMoreInfo",
                String::class.java
        )
        method.isAccessible = true
        val document = method.invoke(ParserPanini(), "www.opengeek.it")
        val goodDocument = method.invoke(ParserPanini(), "http://comics.panini.it/calendario/uscite-questa-settimana/")

        assertNull(document)
        assertNotNull(goodDocument)
    }

    @Test
    fun inspectElementTest() {
        val method = ParserRW::class.java.getDeclaredMethod(
                "inspectElement",
                Element::class.java
        )
        method.isAccessible = true
        val element = Element("anElement").text("dc")
        val result = method.invoke(ParserRW(), element)
        val wrongElement = Element("anElement").text("marvel")
        val wrongResult = method.invoke(ParserRW(), wrongElement)

        assertEquals("marvel", wrongResult)
        assertNull(result)
    }

    @Test
    fun getTargetReadableMonthTest() {
        // ATTENTION: BE CAREFUL WITH CURRENT MONTH
        val method = ParserRW::class.java.getDeclaredMethod(
                "getTargetReadableMonth",
                Int::class.java
        )
        method.isAccessible = true

        val result = method.invoke(ParserRW(), 10)

        assertEquals("agosto", result)
        assertNotEquals("luglio", result)
    }

    @Test
    fun elaborateTitleTest() {
        val method = ParserBonelli::class.java.getDeclaredMethod(
                "elaborateTitle",
                String::class.java
        )
        method.isAccessible = true

        val result = method.invoke(ParserBonelli(), "N°.244 - Raccolta Zagor n°244")

        assertEquals("N°.244 - Raccolta Zagor 244", result)
        assertNotEquals("Raccolta Zagor 244", result)
    }
}
