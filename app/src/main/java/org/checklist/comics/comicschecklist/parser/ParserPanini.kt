package org.checklist.comics.comicschecklist.parser

import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.log.CCLogger
import org.checklist.comics.comicschecklist.log.ParserLog
import org.checklist.comics.comicschecklist.util.Constants

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.util.ArrayList
import java.util.Date
import java.util.StringTokenizer

class ParserPanini : Parser() {

    private enum class LINKS constructor(val url: String) {
        QUESTA_SETTIMANA("$BASE_URL/calendario/uscite-questa-settimana/"),
        PROSSIME_SETTIMANE("$BASE_URL/calendario/uscite-prossime-settimane/"),
        MAGAZINE_9L("$BASE_URL/store/pub_ita_it/magazines/9l.html"),
        PANINI_DISNEY("$BASE_URL/store/pub_ita_it/magazines/cmc-d.html"),
        PLANET_MANGA("$BASE_URL/store/pub_ita_it/magazines/manga.html"),
        MARVEL("$BASE_URL/store/pub_ita_it/magazines/cmc-m.html"),
        PANINI_COMICS("$BASE_URL/store/pub_ita_it/magazines/comics.html")
    }

    /**
     * Method used to search Panini comics.
     * @return a list found comic
     */
    override fun initParser(): ArrayList<ComicEntity> {
        CCLogger.i(TAG, "initParser - Start searching for Panini comics")
        val comicEntities = ArrayList<ComicEntity>()
        var foundComics: List<ComicEntity>?

        for (link in LINKS.values()) {
            foundComics = parseUrl(link.url)
            if (foundComics.isEmpty()) {
                CCLogger.w(TAG, "initParser - No results from link " + link.url)
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
            ParserLog.increaseWrongPaniniURL()
            return comicEntities
        }

        ParserLog.increaseParsedPaniniURL()

        val links: Elements
        // Select only a part of document
        val content = doc.getElementById("products-list")
        if (content != null) {
            links = content.getElementsByAttributeValueContaining("class", "row list-group-item")
            // Prompt number of comics found on document
            CCLogger.d(TAG, "parseUrl - Total links : " + links.size)
        } else {
            CCLogger.w(TAG, "parseUrl - Can't take a list of elements, because content 'products-list' return NULL\n" + doc.toString())
            ParserLog.increaseWrongPaniniElements()
            return comicEntities
        }

        // Init mandatory data
        var title: String?
        var releaseDate: Date
        // Init optional data
        var description: String
        var price: String
        var feature: String
        var coverUrl: String

        for (element in links) {
            title = searchTitle(element)
            if (title == null) {
                CCLogger.w(TAG, "parseUrl - Title not found!")
                ParserLog.increaseErrorOnParsingComic()
                continue
            }

            val rawReleaseDate = searchReleaseDate(element)
            if (rawReleaseDate == null) {
                CCLogger.w(TAG, "parseUrl - Release date not found!")
                ParserLog.increaseErrorOnParsingComic()
                continue
            } else {
                // Calculating date
                releaseDate = elaborateDate(rawReleaseDate)
            }

            CCLogger.d(TAG, "parseUrl - Results:\nComic title : $title\nRelease date : $releaseDate")

            val linkMoreInfo = element.getElementsByTag("a").attr("href")
            val docMoreInfo = searchMoreInfo(linkMoreInfo)
            if (docMoreInfo == null) {
                coverUrl = ""
                description = "N.D."
                feature = "N.D."
                price = "N.D."
            } else {
                // Getting only essential info for comic
                val docPath = docMoreInfo.select("div.product-essential").html()
                val divEssential = Jsoup.parse(docPath)

                coverUrl = searchCover(divEssential)
                description = searchDescription(divEssential)
                feature = searchFeature(divEssential)
                price = searchPrice(divEssential)

                CCLogger.d(TAG, "parseUrl - Results:\nCover url : $coverUrl\nFeature : $feature\nDescription : $description\nPrice : $price")
            }

            val comic = ComicEntity(title.toUpperCase(), releaseDate, description,
                    price, feature, coverUrl, Constants.Sections.PANINI.getName(), false, false, linkMoreInfo)

            comicEntities.add(comic)
        }

        CCLogger.v(TAG, "parseUrl - Found " + comicEntities.size + " comics!")

        return comicEntities
    }

    override fun searchTitle(`object`: Any): String? {
        val link = `object` as Element
        val title = link.getElementsByTag("h3").text()
        val tokenizer = StringTokenizer(title)
        val rawTitle = tokenizer.nextToken()
        return if (!rawTitle.equals("play", ignoreCase = true) && !rawTitle.equals("n.d.", ignoreCase = true)) {
            title
        } else {
            null
        }
    }

    override fun searchReleaseDate(`object`: Any): String? {
        val link = `object` as Element
        var rawReleaseDate = link.getElementsByTag("p").text()
        rawReleaseDate = rawReleaseDate.replace("Data d'uscita:", "").trim { it <= ' ' }

        return if (rawReleaseDate.isEmpty()) {
            null
        } else {
            rawReleaseDate
        }

    }

    override fun searchCover(`object`: Any): String {
        // Take cover URL N.B.: changed due to violation of copyright
        val div = `object` as Document
        val imageElement = div.getElementsByTag("a").first()
        val coverUrl: String

        if (imageElement != null) {
            coverUrl = imageElement.text()
            CCLogger.v(TAG, "searchCover - Cover $coverUrl")
        } else {
            CCLogger.w(TAG, "searchCover - Cover not found, setting default value!")
            coverUrl = ""
        }

        return coverUrl
    }

    override fun searchDescription(`object`: Any): String {
        val div = `object` as Document
        val descriptionElement = div.select("div#description").first()
        val description: String
        if (descriptionElement != null) {
            description = descriptionElement.text()
            CCLogger.v(TAG, "searchDescription - Description $description")
        } else {
            CCLogger.w(TAG, "searchDescription - Description not found, setting default value!")
            description = "N.D."
        }

        return description
    }

    override fun searchFeature(`object`: Any): String {
        val div = `object` as Document
        val featureElement = div.select("div.box-additional-info").first() ?: return "N.D."

        val features = featureElement.getElementsByClass("product")
        var feature = StringBuilder()
        for (element in features) {
            val id = element.id()
            when (id) {
                "authors", "pages", "format", "includes" ->
                    // For other info, take all text
                    feature.append(element.text()).append(" ")
            }
        }

        if (feature.toString() == "") {
            feature = StringBuilder("N.D.")
        }

        return feature.toString()
    }

    override fun searchPrice(`object`: Any): String {
        val div = `object` as Document
        // Getting description, price and feature
        val priceElement = div.select("p.old-price").first()
        val price: String

        if (priceElement != null) {
            price = priceElement.text()
            CCLogger.v(TAG, "searchPrice - Price $price")
        } else {
            CCLogger.w(TAG, "searchPrice - Price not found, setting default value!")
            price = "N.D."
        }

        return price
    }

    private fun searchMoreInfo(linkMoreInfo: String): Document? {
        val docMoreInfo: Document?
        docMoreInfo = try {
            // Connecting to URL for more info
            CCLogger.v(TAG, "parseUrl - Link more info : $linkMoreInfo")
            Jsoup.connect(linkMoreInfo)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .maxBodySize(0)
                    .timeout(30 * 1000) // timeout to 30 seconds
                    .get()
        } catch (e: Exception) {
            CCLogger.w(TAG, "parseUrl - Can't take more info " + linkMoreInfo + " " + e.toString())
            null
        }

        return docMoreInfo
    }

    companion object {

        private val TAG = ParserPanini::class.java.simpleName

        private const val BASE_URL = "http://comics.panini.it"
    }
}
