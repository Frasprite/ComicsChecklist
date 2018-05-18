package org.checklist.comics.comicschecklist.parser;

import android.content.Context;

import org.checklist.comics.comicschecklist.CCApp;
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
import java.util.StringTokenizer;

public class ParserPanini extends Parser {

    private static final String TAG = ParserPanini.class.getSimpleName();

    private static final String BASE_URL = "http://comics.panini.it";

    public ParserPanini(Context context) {
        super(context);
    }

    /**
     * Method used to search Panini comics.
     * @return true if search was successful, false otherwise
     */
    @Override
    public boolean startParsing() {
        CCLogger.i(TAG, "startParsing - Start searching for Panini comics");
        boolean parseError;

        parseError = parseUrl(BASE_URL + "/calendario/uscite-questa-settimana/");
        CCLogger.v(TAG, "startParsing - Result on this week is " + parseError);
        parseError = parseUrl(BASE_URL + "/calendario/uscite-prossime-settimane/");
        CCLogger.v(TAG, "startParsing - Result on next week is " + parseError);
        parseError = parseUrl(BASE_URL + "/store/pub_ita_it/magazines/9l.html");
        CCLogger.v(TAG, "startParsing - Result on 9L is" + parseError);
        parseError = parseUrl(BASE_URL + "/store/pub_ita_it/magazines/cmc-d.html");
        CCLogger.v(TAG, "startParsing - Result on Panini Disney is " + parseError);
        parseError = parseUrl(BASE_URL + "/store/pub_ita_it/magazines/manga.html");
        CCLogger.v(TAG, "startParsing - Result on Planet Manga is " + parseError);
        parseError = parseUrl(BASE_URL + "/store/pub_ita_it/magazines/cmc-m.html");
        CCLogger.v(TAG, "startParsing - Result on Marvel is " + parseError);
        parseError = parseUrl(BASE_URL + "/store/pub_ita_it/magazines/comics.html");
        CCLogger.v(TAG, "startParsing - Result on Panini Comics is " + parseError);

        return parseError;
    }

    @Override
    public boolean parseUrl(String url) {
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
            ParserLog.increaseWrongPaniniURL();
            return true;
        }

        ParserLog.increaseParsedPaniniURL();

        Elements links;
        // Select only a part of document
        Element content = doc.getElementById("products-list");
        if (content != null) {
            links = content.getElementsByAttributeValueContaining("class", "row list-group-item");
            // Prompt number of comics found on document
            CCLogger.d(TAG, "parseUrl - Total links : " + links.size());
        } else {
            CCLogger.w(TAG, "parseUrl - Can't take a list of elements, because content 'products-list' return NULL\n" + doc.toString());
            ParserLog.increaseWrongPaniniElements();
            return true;
        }

        // Init mandatory data
        String title;
        Date releaseDate;
        // Init optional data
        String description, price, feature, coverUrl;

        ArrayList<ComicEntity> comicsList = new ArrayList<>();

        for (Element element : links) {
            title = searchTitle(element);
            if (title == null) {
                CCLogger.w(TAG, "parseUrl - Title not found!");
                ParserLog.increaseErrorOnParsingComic();
                continue;
            }

            String rawReleaseDate = searchReleaseDate(element);
            if (rawReleaseDate == null) {
                CCLogger.w(TAG, "parseUrl - Release date not found!");
                ParserLog.increaseErrorOnParsingComic();
                continue;
            } else {
                // Calculating date
                releaseDate = DateCreator.elaborateDate(rawReleaseDate);
            }

            CCLogger.d(TAG, "parseUrl - Results:\nComic title : " + title + "\nRelease date : " + releaseDate);

            String linkMoreInfo = element.getElementsByTag("a").attr("href");
            Document docMoreInfo = searchMoreInfo(linkMoreInfo);
            if (docMoreInfo == null) {
                coverUrl = "";
                description = "N.D.";
                feature = "N.D.";
                price = "N.D.";
            } else {
                // Getting only essential info for comic
                String docPath = docMoreInfo.select("div.product-essential").html();
                Document divEssential = Jsoup.parse(docPath);

                coverUrl = searchCover(divEssential);
                description = searchDescription(divEssential);
                feature = searchFeature(divEssential);
                price = searchPrice(divEssential);

                CCLogger.d(TAG, "parseUrl - Results:\nCover url : " + coverUrl + "\nFeature : " + feature + "\nDescription : " + description + "\nPrice : " + price);
            }

            ComicEntity comic = new ComicEntity(title.toUpperCase(), releaseDate, description,
                    price, feature, coverUrl, Constants.Sections.PANINI.getName(), false, false, linkMoreInfo);

            comicsList.add(comic);
        }

        // Get reference to repository and insert data
        CCLogger.v(TAG, "parseUrl - Inserting " + comicsList.size() + " comics on DB");
        ((CCApp) mContext.getApplicationContext()).getRepository().insertComics(comicsList);

        return false;
    }

    @Override
    public String searchTitle(Object object) {
        Element link = (Element) object;
        String title = link.getElementsByTag("h3").text();
        StringTokenizer tokenizer = new StringTokenizer(title);
        String rawTitle = tokenizer.nextToken();
        if (!rawTitle.equalsIgnoreCase("play") && !rawTitle.equalsIgnoreCase("n.d.")) {
            return title;
        } else {
            return null;
        }
    }

    @Override
    public String searchReleaseDate(Object object) {
        Element link = (Element) object;
        String rawReleaseDate = link.getElementsByTag("p").text();
        rawReleaseDate = rawReleaseDate.replace("Data d'uscita:", "").trim();

        if (rawReleaseDate.isEmpty()) {
            return null;
        } else {
            return rawReleaseDate;
        }

    }

    @Override
    public String searchCover(Object object) {
        // Take cover URL N.B.: changed due to violation of copyright
        Document div = (Document) object;
        Element imageElement = div.getElementsByTag("a").first();
        String coverUrl;

        if (imageElement != null) {
            coverUrl = imageElement.text();
            CCLogger.v(TAG, "searchCover - Cover " + coverUrl);
        } else {
            CCLogger.w(TAG, "searchCover - Cover not found, setting default value!");
            coverUrl = "";
        }

        return coverUrl;
    }

    @Override
    public String searchDescription(Object object) {
        Document div = (Document) object;
        Element descriptionElement = div.select("div#description").first();
        String description;
        if (descriptionElement != null) {
            description = descriptionElement.text();
            CCLogger.v(TAG, "searchDescription - Description " + description);
        } else {
            CCLogger.w(TAG, "searchDescription - Description not found, setting default value!");
            description = "N.D.";
        }

        return description;
    }

    @Override
    public String searchFeature(Object object) {
        Document div = (Document) object;
        Element featureElement = div.select("div.box-additional-info").first();
        if (featureElement == null) {
            return "N.D.";
        }

        Elements features = featureElement.getElementsByClass("product");
        StringBuilder feature = new StringBuilder();
        for (Element element : features) {
            String ID = element.id();
            switch (ID) {
                case "authors":
                case "pages":
                case "format":
                case "includes":
                    // For other info, take all text
                    feature.append(element.text());
                    break;
            }
        }

        if (feature.toString().equals("")) {
            feature = new StringBuilder("N.D.");
        }

        return feature.toString();
    }

    @Override
    public String searchPrice(Object object) {
        Document div = (Document) object;
        // Getting description, price and feature
        Element priceElement = div.select("p.old-price").first();
        String price;

        if (priceElement != null) {
            price = priceElement.text();
            CCLogger.v(TAG, "searchPrice - Price " + price);
        } else {
            CCLogger.w(TAG, "searchPrice - Price not found, setting default value!");
            price = "N.D.";
        }

        return price;
    }

    private Document searchMoreInfo(String linkMoreInfo) {
        Document docMoreInfo;
        try {
            // Connecting to URL for more info
            CCLogger.v(TAG, "parseUrl - Link more info : " + linkMoreInfo);
            docMoreInfo = Jsoup.connect(linkMoreInfo)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .maxBodySize(0)
                    .timeout(30 * 1000) // timeout to 30 seconds
                    .get();
        } catch (Exception e) {
            CCLogger.w(TAG, "parseUrl - Can't take more info " + linkMoreInfo + " " + e.toString());
            docMoreInfo = null;
        }

        return docMoreInfo;
    }
}
