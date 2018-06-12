package org.checklist.comics.comicschecklist.parser;

import org.checklist.comics.comicschecklist.database.entity.ComicEntity;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.log.ParserLog;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.DateCreator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Date;

public class ParserBonelli extends Parser {

    private static final String TAG = ParserBonelli.class.getSimpleName();

    private static final String BASE_URL = "http://www.sergiobonelli.it";

    private enum LINKS {
        EDICOLA_INEDITI        (BASE_URL + "/sezioni/1025/inediti"),
        EDICOLA_RISTAMPE       (BASE_URL + "/sezioni/1016/ristampe"),
        EDICOLA_RACCOLTE       (BASE_URL + "/sezioni/1017/raccolte"),
        PROSSIMAMENTE_INEDITI  (BASE_URL + "/sezioni/1026/inediti1026"),
        PROSSIMAMENTE_RISTAMPE (BASE_URL + "/sezioni/1018/ristampe1018"),
        PROSSIMAMENTE_RACCOLTE (BASE_URL + "/sezioni/1019/raccolte1019");

        private final String _url;

        LINKS(String url) {
            _url = url;
        }

        public String getUrl() {
            return _url;
        }
    }

    /**
     * Method used to start search for Bonneli comics.
     * @return a list found comic
     */
    @Override
    public ArrayList<ComicEntity> initParser() {
        CCLogger.i(TAG, "initParser - Start searching Bonelli comics");
        ArrayList<ComicEntity> comicEntities = new ArrayList<>();
        ArrayList<ComicEntity> foundComics;

        for (LINKS link : LINKS.values()) {
            foundComics = parseUrl(link.getUrl());
            if (foundComics == null) {
                CCLogger.w(TAG, "initParser - No results from link " + link.getUrl());
                continue;
            }
            comicEntities.addAll(foundComics);
        }

        return comicEntities;
    }

    @Override
    public ArrayList<ComicEntity> parseUrl(String url) {
        CCLogger.d(TAG, "parseUrl - Parsing " + url);

        // Take data from web and save it on document
        Document doc;
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .maxBodySize(0)
                    .timeout(30 * 1000) // timeout to 30 seconds
                    .get();
        } catch (Exception e) {
            CCLogger.w(TAG, "parseUrl - This url does not exists " + url + " " + e.toString());
            ParserLog.increaseWrongBonelliURL();
            return null;
        }

        ParserLog.increaseParsedBonelliURL();

        // Finding release date
        String releaseTag = doc.select("p.tag_2").html();
        Document releasePart = Jsoup.parse(releaseTag);
        Elements spanRelease = releasePart.select("span.valore");

        // Finding link for other info and title if needed
        Elements spanOther = doc.select("div.cont_foto");

        if (spanRelease.size() != spanOther.size()) {
            CCLogger.w(TAG, "parseUrl - List of elements have different size!\n" +
                    "Total release " + spanRelease.size() + "\nTotal entries " + spanOther.size());
            ParserLog.increaseWrongBonelliElements();
            return null;
        }

        // Init mandatory data
        String title, releaseDate;
        Date myDate;
        // Init optional data
        String description, price, feature, coverUrl, moreInfoUrl;

        ArrayList<ComicEntity> comicEntities = new ArrayList<>();

        for (int i = 0; i < spanRelease.size(); i++) {
            try {
                moreInfoUrl = BASE_URL + "/" + spanOther.get(i).select("a").first().attr("href");
                // On spanOther element is possible to find title and URL to other info
                CCLogger.v(TAG, "parseUrl - More info URL " + moreInfoUrl);

                // Creating doc file from URL
                Document docMoreInfo = Jsoup.connect(moreInfoUrl)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .maxBodySize(0)
                        .timeout(30 * 1000) // timeout to 30 seconds
                        .get();

                title = searchTitle(docMoreInfo);
                if (title == null) {
                    CCLogger.w(TAG, "parseUrl - Title not found!");
                    ParserLog.increaseErrorOnParsingComic();
                    continue;
                }

                releaseDate = searchReleaseDate(spanRelease.get(i));
                if (releaseDate == null) {
                    CCLogger.w(TAG, "parseUrl - Release date not found!");
                    ParserLog.increaseErrorOnParsingComic();
                    continue;
                } else {
                    // Calculating date for SQL
                    myDate = DateCreator.elaborateDate(releaseDate);
                }

                CCLogger.d(TAG, "parseUrl - Results:\nComic title : " + title + "\nRelease date : " + releaseDate);

                coverUrl = searchCover(docMoreInfo);
                description = searchDescription(docMoreInfo);
                feature = searchFeature(docMoreInfo);
                price = searchPrice(docMoreInfo);

                CCLogger.d(TAG, "parseUrl - Results:\nCover url : " + coverUrl + "\nFeature : " + feature + "\nDescription : " + description + "\nPrice : " + price);

                // Insert found comic on list
                ComicEntity comic = new ComicEntity(title.toUpperCase(), myDate, description,
                        price, feature, coverUrl, Constants.Sections.BONELLI.getName(), false, false, url);

                comicEntities.add(comic);
            } catch (Exception e) {
                CCLogger.w(TAG, "parseUrl - Can't take more info from " + e.toString() + " comic not fetched", e);
                ParserLog.increaseErrorOnParsingComic();
            }
        }

        CCLogger.v(TAG, "parseUrl - Found " + comicEntities.size() + " comics!");

        return comicEntities;
    }

    @Override
    public String searchTitle(Object object) {
        Document docMoreInfo = (Document) object;

        String rawTitle = docMoreInfo.select("var.atc_title").text();

        if (rawTitle.equals("")) {
            // Title is empty, take it from page title
            rawTitle = docMoreInfo.title();
        }

        return elaborateTitle(rawTitle);
    }

    @Override
    public String searchReleaseDate(Object object) {
        Element element = (Element) object;
        return element.text();
    }

    @Override
    public String searchCover(Object object) {
        // N.B.: changed due to violation of copyright
        Document docMoreInfo = (Document) object;
        Elements linkPathImg = docMoreInfo.select("div.bk-cover img[src]");
        String coverUrl;
        if (!linkPathImg.isEmpty()) {
            coverUrl = BASE_URL + "/" + linkPathImg.attr("src");
            CCLogger.v(TAG, "searchCover - Cover " + coverUrl);
        } else {
            CCLogger.w(TAG, "searchCover - Cover not found, setting default value!");
            coverUrl = "";
        }

        return coverUrl;
    }

    @Override
    public String searchDescription(Object object) {
        Document docMoreInfo = (Document) object;
        Elements desc = docMoreInfo.select("div[class=testo_articolo testo testoResize]");
        String description;
        if (!desc.isEmpty()) {
            description = desc.get(0).text();
            CCLogger.v(TAG, "searchDescription - Description " + description);
        } else {
            CCLogger.w(TAG, "searchDescription - Description not found, setting default value!");
            description = "N.D.";
        }

        return description;
    }

    @Override
    public String searchFeature(Object object) {
        Document docMoreInfo = (Document) object;
        String periodicityTag = docMoreInfo.select("p.tag_3").html();
        Document periodicityPart = Jsoup.parse(periodicityTag);
        Elements spanPeriod = periodicityPart.select("span.valore");
        String feature;

        if (!spanPeriod.isEmpty()) {
            feature = spanPeriod.get(0).text();
            CCLogger.v(TAG, "searchFeature - Feature " + feature);
        } else {
            CCLogger.w(TAG, "searchFeature - Feature not found, setting default value!");
            feature = "N.D.";
        }

        return feature;
    }

    @Override
    public String searchPrice(Object object) {
        return "N.D.";
    }

    private String elaborateTitle(String rawTitle) {
        CCLogger.v(TAG, "elaborateTitle - Comic title BEFORE: " + rawTitle);
        // Defining comic name
        if (rawTitle.startsWith("N°.")) { // ex.: N°.244 - Raccolta Zagor n°244
            rawTitle = rawTitle.substring(rawTitle.indexOf("-") + 1).trim();
        }

        if (rawTitle.contains("N°.")) { // ex.: Almanacco Del West N°.2 - Tex Magazine 2017
            rawTitle = rawTitle.replace("N°.", "");
        }

        if (rawTitle.contains("n°")) { // ex.: Maxi Zagor N°.29 - Maxi Zagor n°29
            rawTitle = rawTitle.replace("n°", "");
        }

        if (rawTitle.contains("- Sergio Bonelli")) { // ex.: Montales el Desperado - Sergio Bonelli
            rawTitle = rawTitle.replace("- Sergio Bonelli", "").trim();
        }
        CCLogger.v(TAG, "elaborateTitle - Comic name AFTER: " + rawTitle);

        return rawTitle;
    }
}
