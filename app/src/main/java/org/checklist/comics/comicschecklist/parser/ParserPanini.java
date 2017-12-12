package org.checklist.comics.comicschecklist.parser;

import android.content.Context;

import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.log.CCLogger;
import org.checklist.comics.comicschecklist.log.ParserLog;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.DateCreator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        CCLogger.i(TAG, "startParsePanini - Start searching for Panini comics");
        boolean parseError;

        parseError = parseUrl(BASE_URL + "/calendario/uscite-questa-settimana/") &&
                     parseUrl(BASE_URL + "/calendario/uscite-prossime-settimane/");

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
        try {
            // Select only a part of document
            Element content = doc.getElementById("products-list");
            links = content.getElementsByAttributeValueContaining("class", "row list-group-item");
            // Prompt number of comics found on document
            CCLogger.d(TAG, "parseUrl - Total links : " + links.size());
        } catch (Exception e) {
            CCLogger.w(TAG, "parseUrl - Can't take a list of elements " + url + " " + e.toString());
            ParserLog.increaseWrongPaniniElements();
            return true;
        }

        // Init mandatory data
        String title, releaseDate;
        Date myDate;
        // Init optional data
        String description, price, feature, coverUrl;

        for (Element element : links) {
            try {
                title = searchTitle(element);
                if (title == null) {
                    CCLogger.w(TAG, "parseUrl - Title not found!");
                    ParserLog.increaseErrorOnParsingComic();
                    continue;
                }

                releaseDate = searchReleaseDate(element);
                if (releaseDate == null) {
                    CCLogger.w(TAG, "parseUrl - Release date not found!");
                    ParserLog.increaseErrorOnParsingComic();
                    continue;
                } else {
                    // Calculating date for SQL
                    myDate = DateCreator.elaborateDate(releaseDate);
                }

                CCLogger.d(TAG, "parseUrl - Results:\nComic title : " + title + "\nRelease date : " + releaseDate);

                String linkMoreInfo = element.getElementsByTag("a").attr("href");
                Document docMoreInfo = searchMoreInfo(linkMoreInfo);
                if (docMoreInfo == null) {
                    coverUrl = "";
                    description = "N.D.";
                    feature = "N.D.";
                    price = "N.D.";
                    // Insert only mandatory info on database
                    ComicDatabaseManager.insert(mContext, title.toUpperCase(), Constants.Sections.PANINI.getName(), description, releaseDate,
                            myDate, coverUrl, feature, price, "no", "no", linkMoreInfo);
                    continue;
                }

                // Getting only essential info for comic
                String docPath = docMoreInfo.select("div.product-essential").html();
                Document divEssential = Jsoup.parse(docPath);

                coverUrl = searchCover(divEssential);
                description = searchDescription(divEssential);
                feature = searchFeature(divEssential);
                price = searchPrice(divEssential);

                CCLogger.d(TAG, "parseUrl - Results:\nCover url : " + coverUrl + "\nFeature : " + feature + "\nDescription : " + description + "\nPrice : " + price);

                // Insert data on database
                ComicDatabaseManager.insert(mContext, title.toUpperCase(), Constants.Sections.PANINI.getName(), description, releaseDate,
                        myDate, coverUrl, feature, price, "no", "no", linkMoreInfo);
            } catch (Exception e) {
                CCLogger.w(TAG, "parseUrl - Error while comic fetching " + element.toString() + " " + e.toString());
                ParserLog.increaseErrorOnParsingComic();
            }
        }

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

        return rawReleaseDate.replace("Data d'uscita:", "").trim();
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
