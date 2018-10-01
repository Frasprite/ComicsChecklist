import org.checklist.comics.comicschecklist.util.Constants

import org.junit.Assert.assertEquals
import org.junit.Test

class SectionsTest {

    @Test
    fun fromCodeTest() {
        val resultPanini = Constants.Sections.fromCode(2)
        val resultStar = Constants.Sections.fromCode(3)
        val resultBonelli = Constants.Sections.fromCode(4)
        val resultRW = Constants.Sections.fromCode(5)

        assertEquals(resultPanini, Constants.Sections.PANINI)
        assertEquals(resultStar, Constants.Sections.STAR)
        assertEquals(resultBonelli, Constants.Sections.BONELLI)
        assertEquals(resultRW, Constants.Sections.RW)
    }

    @Test
    fun fromNameTest() {
        val resultPanini = Constants.Sections.fromName("paninicomics")
        val resultStar = Constants.Sections.fromName("star")
        val resultBonelli = Constants.Sections.fromName("bonelli")
        val resultRW = Constants.Sections.fromName("rw")

        assertEquals(resultPanini, Constants.Sections.PANINI)
        assertEquals(resultStar, Constants.Sections.STAR)
        assertEquals(resultBonelli, Constants.Sections.BONELLI)
        assertEquals(resultRW, Constants.Sections.RW)
    }

    @Test
    fun fromTitleTest() {
        val resultPanini = Constants.Sections.fromTitle("Panini Comics")
        val resultStar = Constants.Sections.fromTitle("Star Comics")
        val resultBonelli = Constants.Sections.fromTitle("Sergio Bonelli")
        val resultRW = Constants.Sections.fromTitle("RW Edizioni")

        assertEquals(resultPanini, Constants.Sections.PANINI)
        assertEquals(resultStar, Constants.Sections.STAR)
        assertEquals(resultBonelli, Constants.Sections.BONELLI)
        assertEquals(resultRW, Constants.Sections.RW)
    }
}