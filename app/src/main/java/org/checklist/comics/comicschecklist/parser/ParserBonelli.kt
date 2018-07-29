package org.checklist.comics.comicschecklist.parser

import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.log.ParserLog
import org.checklist.comics.comicschecklist.util.Constants

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

import java.util.ArrayList
import java.util.Date

class ParserBonelli : Parser() {

    /**
     * Method used to start search for Bonneli comics.
     * @return a list found comic
     */
    override fun initParser(): ArrayList<ComicEntity> {
        CCLogger.i(TAG, "initParser - Start searching Bonelli comics")
        val comicEntities = ArrayList<ComicEntity>()
        var foundComics: ArrayList<ComicEntity>?

        for (link in LINKS) {
            foundComics = parseUrl(link)
            if (foundComics.isEmpty()) {
                CCLogger.w(TAG, "initParser - No results from link $link")
                continue
            }
            comicEntities.addAll(foundComics)
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
            CCLogger.w(TAG, "parseUrl - This url does not exists " + url + " " + e.toString())
            ParserLog.increaseWrongBonelliURL()
            return comicEntities
        }

        ParserLog.increaseParsedBonelliURL()

        // Finding release date
        val releaseTag = doc.select("p.tag_2").html()
        val releasePart = Jsoup.parse(releaseTag)
        val spanRelease = releasePart.select("span.valore")

        // Finding link for other info and title if needed
        val spanOther = doc.select("div.cont_foto")

        if (spanRelease.size != spanOther.size) {
            CCLogger.w(TAG, "parseUrl - List of elements have different size!\n" +
                    "Total release " + spanRelease.size + "\nTotal entries " + spanOther.size)
            ParserLog.increaseWrongBonelliElements()
            return comicEntities
        }

        // Init mandatory data
        var title: String?
        var releaseDate: String?
        var myDate: Date
        // Init optional data
        var description: String
        var price: String
        var feature: String
        var coverUrl: String
        var moreInfoUrl: String

        for (i in spanRelease.indices) {
            try {
                moreInfoUrl = BASE_URL + "/" + spanOther[i].select("a").first().attr("href")
                // On spanOther element is possible to find title and URL to other info
                CCLogger.v(TAG, "parseUrl - More info URL $moreInfoUrl")

                // Creating doc file from URL
                val docMoreInfo = Jsoup.connect(moreInfoUrl)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .maxBodySize(0)
                        .timeout(30 * 1000) // timeout to 30 seconds
                        .get()

                title = searchTitle(docMoreInfo)
                if (title.isNullOrBlank()) {
                    CCLogger.w(TAG, "parseUrl - Title not found!")
                    ParserLog.increaseErrorOnParsingComic()
                    continue
                }

                releaseDate = searchReleaseDate(spanRelease[i])
                if (releaseDate.isNullOrBlank()) {
                    CCLogger.w(TAG, "parseUrl - Release date not found!")
                    ParserLog.increaseErrorOnParsingComic()
                    continue
                } else {
                    // Calculating date for SQL
                    myDate = elaborateDate(releaseDate)
                }

                CCLogger.d(TAG, "parseUrl - Results:\nComic title : $title\nRelease date : $releaseDate")

                coverUrl = searchCover(docMoreInfo)
                description = searchDescription(docMoreInfo)
                feature = searchFeature(docMoreInfo)
                price = searchPrice(docMoreInfo)

                CCLogger.d(TAG, "parseUrl - Results:\nCover url : $coverUrl\nFeature : $feature\nDescription : $description\nPrice : $price")

                // Insert found comic on list
                val comic = ComicEntity(title.toUpperCase(), myDate, description,
                        price, feature, coverUrl, Constants.Sections.BONELLI.sectionName, false, false, url)

                comicEntities.add(comic)
            } catch (e: Exception) {
                CCLogger.w(TAG, "parseUrl - Can't take more info from " + e.toString() + " comic not fetched", e)
                ParserLog.increaseErrorOnParsingComic()
            }

        }

        CCLogger.v(TAG, "parseUrl - Found " + comicEntities.size + " comics!")

        return comicEntities
    }

    override fun searchTitle(`object`: Any): String {
        val docMoreInfo = `object` as Document

        var rawTitle = docMoreInfo.select("var.atc_title").text()

        if (rawTitle == "") {
            // Title is empty, take it from page title
            rawTitle = docMoreInfo.title()
        }

        return elaborateTitle(rawTitle)
    }

    override fun searchReleaseDate(`object`: Any): String {
        val element = `object` as Element
        return element.text()
    }

    override fun searchCover(`object`: Any): String {
        // N.B.: changed due to violation of copyright
        val docMoreInfo = `object` as Document
        val linkPathImg = docMoreInfo.select("div.bk-cover img[src]")
        val coverUrl: String
        if (!linkPathImg.isEmpty()) {
            coverUrl = BASE_URL + "/" + linkPathImg.attr("src")
            CCLogger.v(TAG, "searchCover - Cover $coverUrl")
        } else {
            CCLogger.w(TAG, "searchCover - Cover not found, setting default value!")
            coverUrl = ""
        }

        return coverUrl
    }

    override fun searchDescription(`object`: Any): String {
        val docMoreInfo = `object` as Document
        val desc = docMoreInfo.select("div[class=testo_articolo testo testoResize]")
        val description: String
        if (!desc.isEmpty()) {
            description = desc[0].text()
            CCLogger.v(TAG, "searchDescription - Description $description")
        } else {
            CCLogger.w(TAG, "searchDescription - Description not found, setting default value!")
            description = "N.D."
        }

        return description
    }

    override fun searchFeature(`object`: Any): String {
        val docMoreInfo = `object` as Document
        val periodicityTag = docMoreInfo.select("p.tag_3").html()
        val periodicityPart = Jsoup.parse(periodicityTag)
        val spanPeriod = periodicityPart.select("span.valore")
        val feature: String

        if (!spanPeriod.isEmpty()) {
            feature = spanPeriod[0].text()
            CCLogger.v(TAG, "searchFeature - Feature $feature")
        } else {
            CCLogger.w(TAG, "searchFeature - Feature not found, setting default value!")
            feature = "N.D."
        }

        return feature
    }

    override fun searchPrice(`object`: Any): String {
        return "N.D."
    }

    private fun elaborateTitle(rawTitle: String): String {
        var finalTitle = rawTitle
        CCLogger.v(TAG, "elaborateTitle - Comic title BEFORE: $rawTitle")
        // Defining comic name
        if (rawTitle.startsWith("N°.")) { // ex.: N°.244 - Raccolta Zagor n°244
            finalTitle = rawTitle.substring(rawTitle.indexOf("-") + 1).trim { it <= ' ' }
        }

        if (rawTitle.contains("N°.")) { // ex.: Almanacco Del West N°.2 - Tex Magazine 2017
            finalTitle = rawTitle.replace("N°.", "")
        }

        if (rawTitle.contains("n°")) { // ex.: Maxi Zagor N°.29 - Maxi Zagor n°29
            finalTitle = rawTitle.replace("n°", "")
        }

        if (rawTitle.contains("- Sergio Bonelli")) { // ex.: Montales el Desperado - Sergio Bonelli
            finalTitle = rawTitle.replace("- Sergio Bonelli", "").trim { it <= ' ' }
        }
        CCLogger.v(TAG, "elaborateTitle - Comic name AFTER: $rawTitle")

        return finalTitle
    }

    companion object {

        private val TAG = ParserBonelli::class.java.simpleName

        private const val BASE_URL = "http://www.sergiobonelli.it"

        private const val EDICOLA_INEDITI = "$BASE_URL/sezioni/1025/inediti"
        private const val EDICOLA_RISTAMPE = "$BASE_URL/sezioni/1016/ristampe"
        private const val EDICOLA_RACCOLTE = "$BASE_URL/sezioni/1017/raccolte"
        private const val PROSSIMAMENTE_INEDITI = "$BASE_URL/sezioni/1026/inediti1026"
        private const val PROSSIMAMENTE_RISTAMPE = "$BASE_URL/sezioni/1018/ristampe1018"
        private const val PROSSIMAMENTE_RACCOLTE = "$BASE_URL/sezioni/1019/raccolte1019"

        private val LINKS = arrayOf(EDICOLA_INEDITI, EDICOLA_RISTAMPE, EDICOLA_RACCOLTE, PROSSIMAMENTE_INEDITI, PROSSIMAMENTE_RISTAMPE, PROSSIMAMENTE_RACCOLTE)
    }
}
