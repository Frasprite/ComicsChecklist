package org.checklist.comics.comicschecklist.parser

import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.log.CCLogger

import java.util.ArrayList

class ParserMockEditor : Parser() {

    override fun initParser(): ArrayList<ComicEntity> {
        return parseUrl("This is a test!!!")
    }

    override fun parseUrl(url: String): ArrayList<ComicEntity> {
        CCLogger.d(TAG, "startParseFreeComics - start")

        val comicEntities = ArrayList<ComicEntity>()

        // Adding comics to cart
        comicEntities.add(ComicEntity("Sport Stars 4",
                elaborateDate("29/06/2018"),
                "da comprare",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/27228.jpg",
                "N.D.",
                "Free",
                true, false,
                "http://digitalcomicmuseum.com/"))
        comicEntities.add(ComicEntity("Football Thrills 1",
                elaborateDate("02/07/2018"),
                "da comprare",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/27227.jpg",
                "N.D.",
                "Free",
                true, false,
                "http://digitalcomicmuseum.com/"))
        comicEntities.add(ComicEntity("True Confidence 3",
                elaborateDate("07/07/2018"),
                "da comprare",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/27223.jpg",
                "N.D.",
                "Free",
                true, false,
                "http://digitalcomicmuseum.com/"))

        // Adding favorite comics
        comicEntities.add(ComicEntity("Wanted Comics 11",
                elaborateDate("01/07/2018"),
                "preferiti",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/20652.jpg",
                "N.D.",
                "Free",
                false, true,
                "http://digitalcomicmuseum.com/"))
        comicEntities.add(ComicEntity("Billy The Kid 13",
                elaborateDate("03/07/2018"),
                "preferiti",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/27228.jpg",
                "N.D.",
                "Free",
                false, true,
                "http://digitalcomicmuseum.com/"))
        comicEntities.add(ComicEntity("Woman in red 10",
                elaborateDate("04/07/2018"),
                "preferiti",
                "N.D.",
                "http://digitalcomicmuseum.com/thumbnails/17932.jpg",
                "N.D.",
                "Free",
                false, true,
                "http://digitalcomicmuseum.com/"))

        CCLogger.v(TAG, "startParseFreeComics - stop")

        return comicEntities
    }

    override fun searchTitle(`object`: Any): String? {
        return null
    }

    override fun searchReleaseDate(`object`: Any): String? {
        return null
    }

    override fun searchDescription(`object`: Any): String? {
        return null
    }

    override fun searchCover(`object`: Any): String? {
        return null
    }

    override fun searchFeature(`object`: Any): String? {
        return null
    }

    override fun searchPrice(`object`: Any): String? {
        return null
    }

    companion object {

        private val TAG = ParserMockEditor::class.java.simpleName
    }
}
