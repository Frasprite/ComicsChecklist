package org.checklist.comics.comicschecklist.parser

import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.log.ParserLog
import org.checklist.comics.comicschecklist.util.Constants

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.text.DateFormatSymbols
import java.util.ArrayList
import java.util.Locale

class ParserRW : Parser() {

    private var mCurrentReleaseDate: String? = null

    /**
     * Method used to search RW comics. <br></br>
     * This method will search URL by URL, which is supposed to have a list of comics.
     * @return a list found comic
     */
    override fun initParser(): ArrayList<ComicEntity> {
        CCLogger.i(TAG, "initParser - Start searching for RW comics")

        val comicEntities = ArrayList<ComicEntity>()
        var rawComicEntities: List<ComicEntity>?
        val dayToSearch = 5
        val dateTime = DateTime()
        for (i in -3..dayToSearch) {
            CCLogger.d(TAG, "initParser - Today is " + dateTime.toString() + " adding " + i + " day(s)")
            mCurrentReleaseDate = searchReleaseDate(i)
            val day = dateTime.plusDays(i).dayOfMonth

            // Example : http://www.rwedizioni.it/news/uscite-2-dicembre/
            val url = FIRST_RW + MIDDLE_RW + day + MIDDLE_RW + getTargetReadableMonth(i) + END_RW
            rawComicEntities = parseUrl(url)
            if (rawComicEntities.isNotEmpty()) {
                comicEntities.addAll(rawComicEntities)
            }
        }

        return comicEntities
    }

    override fun parseUrl(url: String): ArrayList<ComicEntity> {
        CCLogger.d(TAG, "parseUrl - Parsing $url")
        val comicEntities = ArrayList<ComicEntity>()

        // Take data from web and save it on document
        val doc: Document
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .maxBodySize(0)
                    .timeout(30 * 1000) // timeout to 30 seconds
                    .get()
        } catch (e: Exception) {
            CCLogger.w(TAG, "parseUrl - Error while parsing URL " + url + " " + e.toString())
            ParserLog.increaseWrongRWURL()
            return comicEntities
        }

        ParserLog.increaseParsedRWURL()

        // Take every potential comic as elements
        val content = doc.getElementById("content")
        if (content == null) {
            CCLogger.w(TAG, "parseUrl - Can't take a list of elements $url because content is NULL!")
            ParserLog.increaseWrongRWElements()
            return comicEntities
        }

        // Init mandatory data (we already have computed the release date)
        var title: String?
        val myDate = elaborateDate(mCurrentReleaseDate!!)
        // Init optional data
        var description: String
        var coverUrl: String
        var feature: String
        var price: String

        // Parse comic data
        val pElements = content.select("p")
        for (element in pElements) {
            val checkForImg = element.getElementsByTag("img")
            CCLogger.v(TAG, "parseUrl - Start inspecting element:\n" + element.toString())

            title = searchTitle(checkForImg)
            if (title == null) {
                CCLogger.w(TAG, "parseUrl - Title not found!")
                ParserLog.increaseErrorOnParsingComic()
                continue
            }

            CCLogger.d(TAG, "parseUrl - Results:\nComic title : $title\nRelease date : $mCurrentReleaseDate")

            coverUrl = searchCover(checkForImg)
            description = searchDescription(checkForImg)
            feature = searchFeature(element)
            price = searchPrice(element)

            CCLogger.d(TAG, "parseUrl - Results:\nCover url : $coverUrl\nFeature : $feature\nDescription : $description\nPrice : $price")

            // Insert found comic on list
            val comic = ComicEntity(title.toUpperCase(), myDate, description,
                    price, feature, coverUrl, Constants.Sections.RW.sectionName, false, false, url)

            comicEntities.add(comic)
        }

        CCLogger.v(TAG, "parseUrl - Found " + comicEntities.size + " comics!")

        return comicEntities
    }

    override fun searchTitle(`object`: Any): String? {
        val elements = `object` as Elements
        var title: String? = null

        if (!elements.isEmpty()) {
            val src = elements[0]
            val coverUrl = src.attr("src")
            if (coverUrl.startsWith(MEDIA_RW)) {
                title = coverUrl.replace(MEDIA_RW, "").replace("_", " ").replace(".jpg", "")
                if (title.contains("-")) {
                    title = title.substring(0, title.indexOf("-"))
                }

                // Title elaborated
                CCLogger.v(TAG, "searchTitle - Title $title")
            }
        }

        return title
    }

    override fun searchReleaseDate(`object`: Any): String {
        val daysToAdd = `object` as Int
        val dateTime = DateTime()
        val dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        val releaseDate = dateTimeFormatter.print(dateTime.plusDays(daysToAdd))

        CCLogger.v(TAG, "searchReleaseDate - Computed release date $releaseDate")
        return releaseDate
    }

    override fun searchCover(`object`: Any): String {
        val elements = `object` as Elements
        var coverUrl: String? = null

        if (!elements.isEmpty()) {
            val src = elements[0]
            val rawCoverUrl = src.attr("src")
            if (rawCoverUrl.startsWith(MEDIA_RW)) {
                // Saving cover. N.B.: changed due to violation of copyright
                coverUrl = rawCoverUrl
                CCLogger.v(TAG, "searchCover - Cover URL " + coverUrl!!)
            }
        }

        if (coverUrl == null) {
            CCLogger.w(TAG, "searchCover - Cover not found, setting default value!")
            coverUrl = ""
        }

        return coverUrl
    }

    override fun searchDescription(`object`: Any): String {
        return "N.D."
    }

    override fun searchFeature(`object`: Any): String {
        val text = inspectElement(`object` as Element)
        val feature: String

        if (text != null && text.contains("col.") || text != null && text.contains("b/n")) {
            // Found a <p> with more info
            feature = text
            CCLogger.v(TAG, "searchFeature - Feature $feature")
        } else {
            CCLogger.w(TAG, "searchFeature - Feature not found, setting default value!")
            feature = "N.D."
        }

        return feature
    }

    override fun searchPrice(`object`: Any): String {
        val text = inspectElement(`object` as Element)
        val price: String

        if (text != null && text.contains("€")) {
            // Found price text on <p>
            price = text
            CCLogger.v(TAG, "searchPrice - Price $price")
        } else {
            CCLogger.w(TAG, "searchPrice - Price not found, setting default value!")
            price = "N.D."
        }

        return price
    }

    private fun inspectElement(element: Element): String? {
        val elementText = element.text()
        return if (!elementText.equals("dc", ignoreCase = true) && !elementText.equals("rw-lion", ignoreCase = true)
                && !elementText.equals("lineachiara", ignoreCase = true) && !elementText.equals("goen", ignoreCase = true)
                && !elementText.equals(" ", ignoreCase = true) && !elementText.equals("vertigo presenta", ignoreCase = true)
                && !elementText.equals("dc deluxe", ignoreCase = true) && !elementText.equals("vertigo", ignoreCase = true)
                && !elementText.equals("vertigo deluxe", ignoreCase = true) && !elementText.equals("rw-goen", ignoreCase = true)
                && !elementText.equals("DC Universe Presenta", ignoreCase = true) && !elementText.equals("DC Presenta", ignoreCase = true)
                && !elementText.equals("dc all star presenta", ignoreCase = true) && elementText.length > 1) {
            elementText
        } else {
            null
        }
    }

    /**
     * Return the current month in [String] format.
     * @param days the days to add
     * @return current month in a readable form
     */
    private fun getTargetReadableMonth(days: Int): String {
        val dateTime = DateTime()
        dateTime.plusDays(days)
        val month = dateTime.monthOfYear
        val monthString = DateFormatSymbols.getInstance(Locale.ITALIAN).months[month].toLowerCase()
        CCLogger.d(TAG, "getCurrentReadableMonth - Month is $monthString")
        return monthString
    }

    companion object {

        private val TAG = ParserRW::class.java.simpleName

        private const val BASE_URL = "http://www.rwedizioni.it"
        private const val FIRST_RW = "$BASE_URL/news/uscite"
        private const val MIDDLE_RW = "-"
        private const val END_RW = "/"
        private const val MEDIA_RW = "$BASE_URL/media/"
    }
}
