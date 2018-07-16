package org.checklist.comics.comicschecklist.parser

import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.log.ParserLog
import org.checklist.comics.comicschecklist.util.Constants

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import java.net.URL
import java.util.ArrayList
import java.util.Date

class ParserStar : Parser() {

    /**
     * Method used to start search for SC comics. <br></br>
     * This method will launch a search for each comic URL.
     * @return a list found comic
     */
    override fun initParser(): ArrayList<ComicEntity> {
        CCLogger.i(TAG, "initParser - Start searching Star Comics comics")

        return parseUrl(ROOT)
    }

    override fun parseUrl(url: String): ArrayList<ComicEntity> {
        CCLogger.v(TAG, "parseUrl - Parsing $url")
        val comicEntities = ArrayList<ComicEntity>()

        // First part: arse root page and find the first comic of the month
        val doc: Document
        try {
            doc = Jsoup.parse(URL(url).openStream(), "UTF-8", url)
        } catch (e: Exception) {
            // Unable to find data for Star Comics
            CCLogger.w(TAG, "parseUrl - Error while searching release from Star Comics site " + e.toString())
            ParserLog.increaseWrongStarURL()
            return comicEntities
        }

        // Second part: elaborate first index where to start parse comic one by one
        val firstIndex = elaborateFirstComicIndex(doc)
        if (firstIndex <= 0) {
            return comicEntities
        }

        // Third part: parse every single URL to find comic data
        val lastIndex = firstIndex + 40
        CCLogger.v(TAG, "parseUrl - Parsing from $firstIndex to $lastIndex")

        var comicEntity: ComicEntity?
        for (i in firstIndex..lastIndex) {
            val foundUrl = COMIC_ROOT + i
            comicEntity = parseSingleComicURL(foundUrl)
            if (comicEntity != null) {
                comicEntities.add(comicEntity)
            }
        }

        CCLogger.v(TAG, "parseUrl - Found " + comicEntities.size + " comics!")

        return comicEntities
    }

    override fun searchTitle(`object`: Any): String? {
        val content = `object` as Element
        var header: String? = null
        var number: String? = null
        var title: String? = null

        // Take various info (header and number)
        val li = content.select("li") // select all li
        for (aLi in li) {
            if (aLi.text().startsWith("Testata")) {
                header = aLi.text().substring("Testata".length).trim { it <= ' ' }
            }
            if (aLi.text().startsWith("Numero")) {
                number = aLi.text().substring("Numero".length).trim { it <= ' ' }
            }
        }

        if (header != null && number != null) {
            title = "$header $number"
        }

        CCLogger.v(TAG, "parseUrlStarC - Title found " + title!!)

        return title
    }

    override fun searchReleaseDate(`object`: Any): String? {
        val content = `object` as Element
        var releaseDate: String? = null

        // Take various info (header and number)
        val li = content.select("li") // select all li
        for (aLi in li) {
            if (aLi.text().startsWith("Data di uscita")) {
                releaseDate = aLi.text().substring("Data di uscita".length).trim { it <= ' ' }
            }
        }

        return releaseDate
    }

    override fun searchCover(`object`: Any): String {
        // Take cover URL N.B.: changed due to violation of copyright
        val content = `object` as Element
        val linkPathImg = content.select("img").first()
        val rawCover = linkPathImg.attr("src")
        val coverUrl: String
        if (rawCover != "") {
            coverUrl = BASE_URL + rawCover
            CCLogger.v(TAG, "searchCover - Cover $coverUrl")
        } else {
            CCLogger.w(TAG, "searchCover - Cover not found, setting default value!")
            coverUrl = ""
        }

        return coverUrl
    }

    override fun searchDescription(`object`: Any): String {
        val content = `object` as Element
        val pList = content.select("p")
        val rawDescription = pList[1].text()
        val description: String
        if (rawDescription != "") {
            description = rawDescription
            CCLogger.v(TAG, "searchDescription - Description $description")
        } else {
            CCLogger.w(TAG, "searchDescription - Description not found, setting default value!")
            description = "N.D."
        }

        return description
    }

    override fun searchFeature(`object`: Any): String {
        return "N.D."
    }

    override fun searchPrice(`object`: Any): String {
        val content = `object` as Element
        val pList = content.select("p")
        var price: String
        try {
            price = pList[2].select("span")[1].text()
            CCLogger.v(TAG, "searchPrice - Price $price")
        } catch (e: Exception) {
            CCLogger.w(TAG, "searchPrice - Price not found, setting default value!")
            price = "N.D."
        }

        return price
    }

    private fun parseSingleComicURL(url: String): ComicEntity? {
        // Take data from web and save it on document
        val docSingleComic: Document
        try {
            // Create doc (ISO-8859-1 CP1252 UTF-8)
            docSingleComic = Jsoup.parse(URL(url)
                    .openStream(), "UTF-8", url)
        } catch (e: Exception) {
            CCLogger.w(TAG, "parseSingleComicURL - This url does not exists " + url + " " + e.toString())
            ParserLog.increaseWrongStarURL()
            return null
        }

        ParserLog.increaseParsedStarURL()

        // Take every info about comic on element
        val content: Element
        try {
            content = docSingleComic.getElementsByTag("article").first()
        } catch (e: Exception) {
            CCLogger.w(TAG, "parseSingleComicURL - Can't take a list of elements " + url + " " + e.toString())
            ParserLog.increaseWrongStarElements()
            return null
        }

        // Init mandatory data
        val title: String?
        val releaseDate: String?
        val myDate: Date
        // Init optional data
        val description: String
        val price: String
        val feature: String
        val coverUrl: String

        try {
            // Take various info (name, release date and number)
            CCLogger.v(TAG, "parseSingleComicURL - Start inspecting element:\n" + content.toString())

            title = searchTitle(content)
            if (title == null) {
                CCLogger.w(TAG, "parseSingleComicURL - Title not found!")
                ParserLog.increaseErrorOnParsingComic()
                return null
            }

            releaseDate = searchReleaseDate(content)
            if (releaseDate == null) {
                CCLogger.w(TAG, "parseSingleComicURL - Release date not found!")
                ParserLog.increaseErrorOnParsingComic()
                return null
            } else {
                // Calculating date for SQL
                myDate = elaborateDate(releaseDate)
            }

            CCLogger.d(TAG, "parseSingleComicURL - Results:\nComic title : $title\nRelease date : $releaseDate")

            coverUrl = searchCover(content)
            description = searchDescription(content)
            feature = searchFeature(content)
            price = searchPrice(content)

            CCLogger.d(TAG, "parseSingleComicURL - Results:\nCover url : $coverUrl\nFeature : $feature\nDescription : $description\nPrice : $price")

            return ComicEntity(title.toUpperCase(), myDate, description,
                    price, feature, coverUrl, Constants.Sections.STAR.getName(), false, false, url)
        } catch (e: Exception) {
            CCLogger.w(TAG, "parseSingleComicURL - Error while searching data for comic id " + e.toString() + "\n" + url)
            ParserLog.increaseErrorOnParsingComic()
            return null
        }

    }

    private fun elaborateFirstComicIndex(doc: Document): Int {
        val content = doc.select("div.content.clearfix").first()
        if (content == null) {
            CCLogger.w(TAG, "elaborateFirstComicIndex - Element 'content' is NULL!")
            return -1
        }

        val photo = content.select("a[href]").first()
        if (photo == null) {
            CCLogger.w(TAG, "elaborateFirstComicIndex - Element 'photo' is NULL!")
            return -1
        }

        val myLink = photo.attr("href").replace("fumetto.aspx?Fumetto=", "")
        return try {
            Integer.parseInt(myLink)
        } catch (e: Exception) {
            CCLogger.w(TAG, "elaborateFirstComicIndex - Element 'href' is NULL! " + e.toString())
            -1
        }

    }

    companion object {

        private val TAG = ParserStar::class.java.simpleName

        private const val BASE_URL = "https://www.starcomics.com"
        private const val ROOT = "$BASE_URL/UsciteMensili.aspx?AspxAutoDetectCookieSupport=1"
        private const val COMIC_ROOT = "$BASE_URL/fumetto.aspx?Fumetto="
    }
}
