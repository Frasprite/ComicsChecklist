package org.checklist.comics.comicschecklist.parser;

import android.content.Context;
import android.support.annotation.NonNull;

import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.util.CCLogger;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.DateCreator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by Francesco Bevilacqua on 20/10/2014.
 * This code is part of ComicsChecklist project.
 */
public class Parser {

    private static final String TAG = Parser.class.getSimpleName();

    private final Context mContext;
    private boolean comicErrorBonelli;
    private boolean comicErrorPanini;
    private boolean comicErrorRw;
    private boolean comicErrorStar;

    /**
     * Class constructor.
     */
    public Parser(Context context) {
        mContext = context;
    }

    /**
     * This method is used for elaborate screenshot of copyright free comics.
     */
    public void startParseFreeComics() {
        CCLogger.d(TAG, "startParseFreeComics - start");

        // Adding 3 comics to cart
        ComicDatabaseManager.insert(mContext, "Sport Stars 4", "da comprare", "N.D.", "29/06/2016",
                DateCreator.elaborateDate("29/06/2016"), "http://digitalcomicmuseum.com/thumbnails/27228.jpg", "N.D.", "Free", "yes", "no", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Football Thrills 1", "da comprare", "N.D.", "02/07/2016",
                DateCreator.elaborateDate("02/07/2016"), "http://digitalcomicmuseum.com/thumbnails/27227.jpg", "N.D.", "Free", "yes", "no", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "True Confidence 3", "da comprare", "N.D.", "07/07/2016",
                DateCreator.elaborateDate("07/07/2016"), "http://digitalcomicmuseum.com/thumbnails/27223.jpg", "N.D.", "Free", "yes", "no", "http://digitalcomicmuseum.com/");

        // Adding 7 comics to favorite
        ComicDatabaseManager.insert(mContext, "Wanted Comics 11", "preferiti", "N.D.", "01/07/2016",
                DateCreator.elaborateDate("01/07/2016"), "http://digitalcomicmuseum.com/thumbnails/20652.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Billy The Kid 13", "preferiti", "N.D.", "03/07/2016",
                DateCreator.elaborateDate("03/07/2016"), "http://digitalcomicmuseum.com/thumbnails/27228.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Woman in red 10", "preferiti", "N.D.", "04/07/2016",
                DateCreator.elaborateDate("04/07/2016"), "http://digitalcomicmuseum.com/thumbnails/17932.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Out of this world adventures 1", "preferiti", "N.D.", "10/07/2016",
                DateCreator.elaborateDate("10/07/2016"), "http://digitalcomicmuseum.com/thumbnails/1105.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Whiz Comics 6", "preferiti", "N.D.", "20/07/2016",
                DateCreator.elaborateDate("20/07/2016"), "http://digitalcomicmuseum.com/thumbnails/19861.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Ghost Comics 67", "preferiti", "N.D.", "26/07/2016",
                DateCreator.elaborateDate("26/07/2016"), "http://digitalcomicmuseum.com/thumbnails/1860.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        ComicDatabaseManager.insert(mContext, "Strange World 100", "preferiti", "N.D.", "01/08/2016",
                DateCreator.elaborateDate("01/08/2016"), "http://digitalcomicmuseum.com/thumbnails/7806.jpg", "N.D.", "Free", "no", "yes", "http://digitalcomicmuseum.com/");

        CCLogger.v(TAG, "startParseFreeComics - stop");
    }

    /**
     * Method used to search Panini comics.
     * @return true if search was successful, false otherwise
     */
    public boolean startParsePanini() {
        CCLogger.i(TAG, "startParsePanini - Start searching for Panini comics");
        comicErrorPanini = false;

        parseUrlPanini("http://comics.panini.it/calendario/uscite-scorsa-settimana/");
        parseUrlPanini("http://comics.panini.it/calendario/uscite-questa-settimana/");
        parseUrlPanini("http://comics.panini.it/calendario/uscite-prossime-settimane/");

        return comicErrorPanini;
    }

    /**
     * Private method used to parse a Panini URL
     * @param url the URL to parse
     */
    private void parseUrlPanini(String url) {
        CCLogger.d(TAG, "parseUrlPanini - Parsing Panini Comics " + url);

        try {
            // Take data from URL and save it on document
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .maxBodySize(0)
                    .timeout(30 * 1000) // timeout to 30 seconds
                    .get();

            // Select only a part of document
            Element content = doc.getElementById("products-list");
            Elements links = content.getElementsByAttributeValueContaining("class", "row list-group-item");
            // Prompt number of comics found on document
            CCLogger.d(TAG, "parseUrlPanini - Total links : " + links.size());
            for (Element link : links) {
                // Getting link to more info, comic name and release date
                String linkMoreInfo = link.getElementsByTag("a").attr("href");
                String comicName = link.getElementsByTag("h3").text();
                String rawReleaseDate = link.getElementsByTag("p").text();
                String releaseDate = rawReleaseDate.replace("Data d'uscita:", "").trim();
                CCLogger.d(TAG, "parseUrlPanini - Comic name : " + comicName+ "\nRelease date : " + releaseDate + "\nLink more info : " + linkMoreInfo);

                // Connecting to URL for more info
                Document docMoreInfo = Jsoup.connect(linkMoreInfo)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .maxBodySize(0)
                        .timeout(30 * 1000) // timeout to 30 seconds
                        .get();

                // Getting only essential info for comic
                String docPath = docMoreInfo.select("div.product-essential").html();
                Document divEssential = Jsoup.parse(docPath);

                // Getting description, price and feature (image is not used due to Copyright violation)
                Element descriptionElement = divEssential.select("div#description").first();
                Element priceElement = divEssential.select("p.old-price").first();
                String description = descriptionElement.text();
                String price = priceElement.text();
                //Element imageElement = divEssential.getElementsByTag("a").first(); // use .attr("href");
                CCLogger.d(TAG, "parseUrlPanini - Description : " + description + "\nPrice " + price);

                // Getting feature and editor
                Element featureElement = divEssential.select("div.box-additional-info").first();
                Elements features = featureElement.getElementsByClass("product");
                String feature = findFeature(features);
                String editor = Constants.Sections.PANINI.getName();
                CCLogger.d(TAG, "parseUrlPanini - Feature found : " + feature + "\n" + "Editor " + editor);

                // Insert data on database
                StringTokenizer tokenizer = new StringTokenizer(comicName);
                String title = tokenizer.nextToken();
                if (!title.equalsIgnoreCase("play") && !comicName.equalsIgnoreCase("n.d.")) {
                    try {
                        // Calculating date for sql
                        Date myDate = DateCreator.elaborateDate(releaseDate);
                        ComicDatabaseManager.insert(mContext, comicName, editor, description, releaseDate,
                                myDate, "", feature, price, "no", "no", linkMoreInfo);
                    } catch (Exception e) {
                        // Error while comic fetching
                        CCLogger.w(TAG, "parseUrlPanini - " + title + " " + e.toString());
                    }
                }
            }
        } catch (Exception e) {
            CCLogger.e(TAG, "parseUrlPanini - Error while comic search " + url + " " + e.toString());
            comicErrorPanini = true;
        }
    }

    private String findFeature(Elements features) {
        String feature = "";
        for (Element element : features) {
            String ID = element.id();
            switch (ID) {
                case "authors":
                case "pages":
                case "format":
                case "includes":
                    // For other info, take all text
                    String otherInfo = element.text();
                    feature = completeFeature(feature, otherInfo);
                    break;
            }
        }

        if (feature.equals("")) {
            feature = "N.D.";
        }

        return feature;
    }

    @NonNull
    private String completeFeature(String feature, String info) {
        // Init correctly feature list
        if (feature.length() == 0) {
            return info;
        } else {
            return feature + "\n" + info;
        }
    }

    /**
     * Method used to search RW comics.
     * @return true if search was successful, false otherwise
     */
    public boolean startParseRW() {
        CCLogger.i(TAG, "startParseRW - Start searching for RW comics");
        comicErrorRw = false;

        int day = DateCreator.getCurrentDay();
        int monthInt = DateCreator.getCurrentMonth() + 1;
        String month = DateCreator.getCurrentReadableMonth();
        int year = DateCreator.getCurrentYear();
        CCLogger.d(TAG, "startParseRW - limit date is " + DateCreator.getTodayString());
        String url;
        String releaseDate;
        for (int i = 1; i <= day + 3; i++) {
            // Example : http://www.rwedizioni.it/news/uscite-del-7-gennaio-2017/
            url = Constants.FIRSTRW + Constants.MIDDLERW + i + Constants.MIDDLERW + month + Constants.MIDDLERW + year + Constants.ENDRW;
            releaseDate = (i < 10 ? "0" + i : i) + "/" + (monthInt < 10 ? "0" + monthInt : monthInt) + "/" + year;
            parseUrlRW(url, releaseDate);
        }

        return comicErrorRw;
    }

    /**
     * Method used to search data from RW URL.
     * @param siteUrl the URL to parse
     * @param releaseDate the date of comic release
     */
    private void parseUrlRW(String siteUrl, String releaseDate) {
        CCLogger.d(TAG, "parseUrlRW - Parsing " + siteUrl);
        try {
            // Take data and save it on document
            Document doc = Jsoup.connect(siteUrl)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .maxBodySize(0)
                    .timeout(30 * 1000) // timeout to 30 seconds
                    .get();
            Element content = doc.getElementById("content");

            // Take every potential comic
            Elements pElements = content.select("p");
            String description = "N.D.", coverUrl, title;
            // Compute release date
            Date myDate = DateCreator.elaborateDate(releaseDate);

            ArrayList<String> coverList = new ArrayList<>();
            ArrayList<String> titleList = new ArrayList<>();
            ArrayList<String> featureList = new ArrayList<>();
            ArrayList<String> priceList = new ArrayList<>();

            try {
                // Insert comic on database
                for (Element element : pElements) {
                    Elements checkForImg = element.getElementsByTag("img");
                    if (!checkForImg.isEmpty()) {
                        Element src = checkForImg.get(0);
                        coverUrl = src.attr("src");
                        if (coverUrl.startsWith(Constants.MEDIARW)) {
                            // N.B.: changed due to violation of copyright
                            String rawCover = "";
                            coverList.add(rawCover);
                            /* Saving cover
                            CCLogger.v(TAG, "parseUrlRW - Cover " + coverUrl);
                            coverList.add(coverUrl);*/
                            title = coverUrl.replace(Constants.MEDIARW, "").replace("_", " ").replace(".jpg", "").toUpperCase();
                            // Title elaborated
                            CCLogger.v(TAG, "parseUrlRW - Title " + title);
                            titleList.add(title);
                        }
                    }

                    String elementText = element.text();
                    if (!elementText.equalsIgnoreCase("dc") && !elementText.equalsIgnoreCase("rw-lion")
                            && !elementText.equalsIgnoreCase("lineachiara") && !elementText.equalsIgnoreCase("goen")
                            && !elementText.equalsIgnoreCase(" ") && !elementText.equalsIgnoreCase("vertigo presenta")
                            && !elementText.equalsIgnoreCase("dc deluxe") && !elementText.equalsIgnoreCase("vertigo")
                            && !elementText.equalsIgnoreCase("vertigo deluxe") && !elementText.equalsIgnoreCase("rw-goen")
                            && !elementText.equalsIgnoreCase("DC Universe Presenta") && !elementText.equalsIgnoreCase("DC Presenta")
                            && !elementText.equalsIgnoreCase("dc all star presenta") && elementText.length() > 1) {

                        if (elementText.contains("col.") || elementText.contains("b/n")) {
                            // Found a <p> with more info
                            CCLogger.v(TAG, "parseUrlRW - Feature " + elementText);
                            featureList.add(elementText);
                        }

                        if (elementText.contains("€")) {
                            // Found price text on <p>
                            CCLogger.v(TAG, "parseUrlRW - Price " + elementText);
                            if (priceList.size() < featureList.size()) {
                                priceList.add(elementText);
                            }
                        }
                    }
                }

                if (coverList.size() == titleList.size() && featureList.size() == priceList.size()) {
                    CCLogger.v(TAG, "parseUrlRW - List are all equals, saving entries");
                    for (int i = 0; i < coverList.size(); i++) {
                        ComicDatabaseManager.insert(mContext, titleList.get(i), Constants.Sections.getName(Constants.Sections.RW), description, releaseDate, myDate, coverList.get(i), featureList.get(i), priceList.get(i), "no", "no", siteUrl);
                    }
                } else {
                    CCLogger.w(TAG, "parseUrlRW - List don't have same size:\ncoverList " + coverList.size() +
                    "\ntitleList " + titleList.size() + "\nfeatureList " + featureList.size() +
                            "\npriceList " + priceList.size());
                }
            } catch (Exception e) {
                CCLogger.w(TAG, "parseUrlRW - Error while comic fetching " + e.toString());
                comicErrorRw = true;
            }
        } catch (Exception e) {
            CCLogger.e(TAG, "parseUrlRW - This url does not exists " + siteUrl + " " + e.toString());
        }
    }

    /**
     * Method used to start search for SC comics.
     * @return true if search was successful, false otherwise
     */
    public boolean startParseStarC() {
        CCLogger.i(TAG, "startParseStarC - Start searching Star Comics comics");
        comicErrorStar = false;

        int from = 0, to = 0;

        // Parse rootPath and find the first comic of the month
        try {
            Document doc = Jsoup.parse(new URL(Constants.ROOT).openStream(), "UTF-8", Constants.ROOT);
            Element content = doc.select("div.content.clearfix").first();
            Element photo = content.select("a[href]").first();
            String myLink = photo.attr("href").replace("fumetto.aspx?Fumetto=", "");
            from = Integer.parseInt(myLink);
            to = Integer.parseInt(myLink) + 40;
        } catch (Exception e) {
            // Unable to find data for Star Comics
            CCLogger.e(TAG, "startParseStarC - Error while searching data from Star Comics site " + e.toString());
        }

        if (from != 0 && to != 0 && from < to)
            parseUrlStarC(from, to);

        return comicErrorStar;
    }

    /**
     * Method used to search comic from Star Comics URL.
     * @param from the beginning
     * @param to to the end
     */
    private void parseUrlStarC(int from, int to) {
        CCLogger.d(TAG, "parseUrlStarC - Parsing from " + from + " to " + to);
        // Start parsing every potential URL
        Document doc;
        String name, releaseDate = "", description, price, feature = "N.D.", coverUrl, testata = "", number = "";
        for (int i = from; i <= to; i++) {
            String URL = Constants.COMIC_ROOT + i;
            CCLogger.v(TAG, "parseUrlStarC - Search for comic number " + i + "\n" + URL);
            // Try to open computed URL
            try {
                // Create doc (ISO-8859-1 CP1252 UTF-8)
                doc = Jsoup.parse(new URL(URL).openStream(), "UTF-8", Constants.COMIC_ROOT + i);
                Element content = doc.getElementsByTag("article").first();
                // Take various info (name, release date and number)
                Elements li = content.select("li"); // select all li
                for (Element aLi : li) {
                    if (aLi.text().startsWith("Testata")) {
                        testata = aLi.text().substring("Testata".length()).trim();
                    }
                    if (aLi.text().startsWith("Data di uscita")) {
                        releaseDate = aLi.text().substring("Data di uscita".length()).trim();
                    }
                    if (aLi.text().startsWith("Numero")) {
                        number = aLi.text().substring("Numero".length()).trim();
                    }
                }
                name = testata + " " + number;
                CCLogger.v(TAG, "parseUrlStarC - Data found " + testata + " " + releaseDate + " " + name + " ");
                // Take cover URL
                //Element linkPathImg = content.select("img").first();
                //coverUrl = Constants.IMG_URL + linkPathImg.attr("src");
                //CCLogger.v(TAG, "parseUrlStarC - Cover " + coverUrl);
                // N.B.: changed due to violation of copyright
                coverUrl = "";
                // Take description and price
                Elements pList = content.select("p");
                description = pList.get(1).text();
                try {
                    price = pList.get(2).select("span").get(1).text();
                } catch (IndexOutOfBoundsException e) {
                    price = "N.D.";
                }
                CCLogger.v(TAG, "parseUrlStarC - Price " + price + " description " + description);

                // Calculating date for sql
                Date myDate = DateCreator.elaborateDate(releaseDate);
                // Insert comic on database
                ComicDatabaseManager.insert(mContext, name, Constants.Sections.getName(Constants.Sections.STAR), description, releaseDate, myDate, coverUrl, feature, price, "no", "no", URL);
            } catch (Exception e) {
                CCLogger.w(TAG, "parseUrlStarC - Error while searching data for comic id " + i + " " + e.toString() + "\n" + URL);
                comicErrorStar = true;
            }
        }
    }

    /**
     * Method used to start search for Bonneli comics.
     * @return true if search was successful, false otherwise
     */
    public boolean startParseBonelli() {
        CCLogger.i(TAG, "startParseBonelli - Start searching Bonelli comics");
        comicErrorBonelli = false;

        parseUrlBonelli(Constants.EDICOLA_INEDITI);
        parseUrlBonelli(Constants.EDICOLA_RISTAMPE);
        parseUrlBonelli(Constants.EDICOLA_RACCOLTE);
        parseUrlBonelli(Constants.PROSSIMAMENTE_INEDITI);
        parseUrlBonelli(Constants.PROSSIMAMENTE_RISTAMPE);
        parseUrlBonelli(Constants.PROSSIMAMENTE_RACCOLTE);

        return comicErrorBonelli;
    }

    /**
     * Method used to parse data from Bonelli URL.
     * @param url where to find comics
     */
    private void parseUrlBonelli(String url) {
        CCLogger.d(TAG, "parseUrlBonelli - URL " + url + " - start");
        try {
            // Creating doc file from URL
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .maxBodySize(0)
                    .timeout(30 * 1000) // timeout to 30 seconds
                    .get();

            // Finding release date
            String releaseTag = doc.select("p.tag_2").html();
            Document releasePart = Jsoup.parse(releaseTag);
            Elements spanRelease = releasePart.select("span.valore");

            // Finding link for other info and title if needed
            Elements spanOther = doc.select("div.cont_foto");

            //CCLogger.v(TAG, "parseUrlBonelli - Total release " + spanRelease.size() + "\nTotal entries " + spanOther.size());

            // Iterate all elements founded
            String name, releaseDate, description = "N.D.", price = "N.D.", feature = "N.D.", coverUrl,
                    moreInfoUrl, periodicityTag;
            for (int i = 0; i < spanRelease.size(); i++) {
                // On spanOther element is possible to find title and URL to other info
                moreInfoUrl = Constants.MAIN_URL + spanOther.get(i).select("a").first().attr("href");
                CCLogger.v(TAG, "parseUrlBonelli - More info URL " + moreInfoUrl);
                try {
                    // Creating doc file from URL
                    Document docMoreInfo = Jsoup.connect(moreInfoUrl)
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .maxBodySize(0)
                            .timeout(30 * 1000) // timeout to 30 seconds
                            .get();

                    // Finding periodicity
                    periodicityTag = docMoreInfo.select("p.tag_3").html();
                    Document periodicityPart = Jsoup.parse(periodicityTag);
                    Elements spanPeriod = periodicityPart.select("span.valore");
                    if (!spanPeriod.isEmpty()) {
                        feature = spanPeriod.get(0).text();
                        CCLogger.v(TAG, "parseUrlBonelli - Comic feature: " + feature);
                    }

                    // Finding comic description
                    Elements desc = docMoreInfo.select("div[class=testo_articolo testo testoResize]");
                    if (!desc.isEmpty()) {
                        description = desc.get(0).text();
                        CCLogger.v(TAG, "parseUrlBonelli - Comic description: " + description);
                    }

                    // Finding cover image
                    //Elements linkPathImg = docMoreInfo.select("div.bk-cover img[src]");
                    //coverUrl = Constants.MAIN_URL + linkPathImg.attr("src");
                    //CCLogger.v(TAG, "parseUrlBonelli - Comic cover: " + coverUrl);
                    // N.B.: changed due to violation of copyright
                    coverUrl = "";

                    // Finding comic title
                    name = docMoreInfo.select("var.atc_title").text();
                    CCLogger.v(TAG, "parseUrlBonelli - Comic name BEFORE: " + name);
                    // Defining comic name
                    if (name.startsWith("N°.")) { // ex.: N°.244 - Raccolta Zagor n°244
                        name = name.substring(name.indexOf("-") + 1).trim();
                    }

                    if (name.contains("N°.")) { // ex.: Almanacco Del West N°.2 - Tex Magazine 2017
                        name = name.replace("N°.", "");
                    }

                    if (name.contains("n°")) { // ex.: Maxi Zagor N°.29 - Maxi Zagor n°29
                        name = name.replace("n°", "");
                    }
                    CCLogger.v(TAG, "parseUrlBonelli - Comic name AFTER: " + name);

                    // Calculating date for sql
                    releaseDate = spanRelease.get(i).text();
                    Date myDate = DateCreator.elaborateDate(releaseDate);

                    // Insert comic on database
                    String editor = Constants.Sections.getName(Constants.Sections.BONELLI);
                    ComicDatabaseManager.insert(mContext, name.toUpperCase(), editor, description, releaseDate, myDate, coverUrl, feature, price, "no", "no", url);
                } catch (Exception e) {
                    CCLogger.w(TAG, "parseUrlBonelli - Can't take more info from " + moreInfoUrl + " " + e.toString() + " comic not fetched", e);
                }
            }
        } catch (Exception e) {
            CCLogger.e(TAG, "parseUrlBonelli - Error while comic fetching " + url + " " + e.toString());
            comicErrorBonelli = true;
        }

        CCLogger.v(TAG, "parseUrlBonelli - end");
    }
}
