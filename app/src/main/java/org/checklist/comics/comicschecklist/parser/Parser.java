package org.checklist.comics.comicschecklist.parser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.DateCreator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
        Log.d(TAG, "startParseFreeComics - start");

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

        Log.v(TAG, "startParseFreeComics - stop");
    }

    /**
     * Method used to search Panini comics.
     * @return true if search was successful, false otherwise
     */
    public boolean startParsePanini() {
        Log.i(TAG, "Start searching for Panini comics");
        comicErrorPanini = false;

        parsePaniniUrl("http://comics.panini.it/calendario/uscite-scorsa-settimana/");
        parsePaniniUrl("http://comics.panini.it/calendario/uscite-questa-settimana/");
        parsePaniniUrl("http://comics.panini.it/calendario/uscite-prossime-settimane/");

        return comicErrorPanini;
    }

    /**
     * Private method used to parse a Panini URL
     * @param url the URL to parse
     */
    private void parsePaniniUrl(String url) {
        Log.d(TAG, "Parsing Panini Comics " + url);

        try {
            // Take data from URL and save it on document
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .maxBodySize(0)
                    .timeout(10 * 1000) // timeout to 10 seconds
                    .get();

            // Select only a part of document
            Element content = doc.getElementById("products-list");
            Elements links = content.getElementsByClass("col-sm-12");
            // Prompt number of comics found on document
            Log.d(TAG, "Total links : " + links.size());
            for (Element link : links) {
                // Getting link to more info, comic name and release date
                String linkMoreInfo = link.getElementsByTag("a").attr("href");
                String comicName = link.getElementsByTag("h3").text();
                String rawReleaseDate = link.getElementsByTag("p").text();
                String releaseDate = rawReleaseDate.replace("Data d'uscita:", "").trim();
                Log.d(TAG, "Comic name : " + comicName+ "\nRelease date : " + releaseDate + "\nLink more info : " + linkMoreInfo);

                // Connecting to URL for more info
                Document docMoreInfo = Jsoup.connect(linkMoreInfo)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .maxBodySize(0)
                        .timeout(10 * 1000) // timeout to 10 seconds
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
                Log.d(TAG, "Description : " + description + "\nPrice " + price);

                // Getting feature and editor
                Element featureElement = divEssential.select("div.box-additional-info").first();
                Elements features = featureElement.getElementsByClass("product");
                String feature = findFeature(features);
                String editor = findEditor(features);
                Log.d(TAG, "Feature found : " + feature + "\n" + "Editor " + editor);

                if (editor != null) {
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
                            Log.w(TAG, title + " " + e.toString());
                        }
                    }
                } else {
                    Log.w(TAG, "Editor not found from given link:\n" + linkMoreInfo + "\n" + features);
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Error while comic search " + url + " " + e.toString());
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

    private String findEditor(Elements features) {
        String editor = null;
        for (Element element : features) {
            String ID = element.id();
            if (ID.equals("linea-editoriale")) {
                editor = element.getElementsByTag("h3").text();
                break;
            }
        }

        if (editor == null) {
            // Editor not found
            Log.w(TAG, "Editor not found for this element\n" + features.toString());
            return null;
        }

        switch (editor) {
            case "Panini Comics":
                return Constants.Sections.PANINI.getName();
            case "Marvel":
                return Constants.Sections.MARVEL.getName();
            case "Planet Manga":
                return Constants.Sections.PLANET.getName();
            default:
                return null;
        }
    }

    /**
     * Method used to search RW comics.
     * @return true if search was successful, false otherwise
     */
    public boolean startParseRW() {
        Log.i(TAG, "Start searching for RW comics");
        comicErrorRw = false;

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int monthInt = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        Log.d(TAG, "startParseRW - date is " + day + "/" + monthInt + "/" + year);
        String month = DateFormatSymbols.getInstance(Locale.ITALIAN).getMonths()[monthInt];
        String url;
        for (int i = 1; i < day + 3; i++) {
            url = Constants.FIRSTRW + Constants.MIDDLERW + i + Constants.MIDDLERW + month + Constants.MIDDLERW + year + Constants.ENDRW;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String date = simpleDateFormat.format(calendar.getTime());
            parseUrlRW(url, date);
        }

        return comicErrorRw;
    }

    /**
     * Method used to search data from RW URL.
     * @param siteUrl the URL to parse
     * @param releaseDate the date of comic release
     */
    private void parseUrlRW(String siteUrl, String releaseDate) {
        Log.d(TAG, "Parsing " + siteUrl);
        try {
            // Take data and save it on document
            Document doc = Jsoup.connect(siteUrl)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .maxBodySize(0)
                    .timeout(10 * 1000) // timeout to 10 seconds
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
                            Log.v(TAG, "Cover " + coverUrl);
                            coverList.add(coverUrl);*/
                            title = coverUrl.replace(Constants.MEDIARW, "").replace("_", " ").replace(".jpg", "").toUpperCase();
                            // Title elaborated
                            Log.v(TAG, "Title " + title);
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
                            Log.v(TAG, "Feature " + elementText);
                            featureList.add(elementText);
                        }

                        if (elementText.contains("€")) {
                            // Found price text on <p>
                            Log.v(TAG, "Price " + elementText);
                            priceList.add(elementText);
                        }
                    }
                }

                if (coverList.size() == titleList.size() && featureList.size() == priceList.size()) {
                    for (int i = 0; i < coverList.size(); i++) {
                        ComicDatabaseManager.insert(mContext, titleList.get(i), Constants.Sections.getName(Constants.Sections.RW), description, releaseDate, myDate, coverList.get(i), featureList.get(i), priceList.get(i), "no", "no", siteUrl);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error while comic fetching " + e.toString());
                comicErrorRw = true;
            }
        } catch (Exception e) {
            Log.w(TAG, "This url does not exists " + siteUrl + " " + e.toString());
        }
    }

    /**
     * Method used to start search for SC comics.
     * @return true if search was successful, false otherwise
     */
    public boolean startParseStarC() {
        Log.i(TAG, "Start searching Star Comics comics");
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
            Log.w(TAG, "Error while searching data from Star Comics site " + e.toString());
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
        Log.d(TAG, "Parsing from " + from + " to " + to);
        // Start parsing every potential URL
        Document doc;
        String name, releaseDate = "", description, price, feature = "N.D.", coverUrl, testata = "", number = "";
        for (int i = from; i <= to; i++) {
            Log.v(TAG, "Search for comic number " + i);
            // Try to open computed URL
            try {
                // Create doc (ISO-8859-1 CP1252 UTF-8)
                String URL = Constants.COMIC_ROOT + i;
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
                Log.v(TAG, "Data found " + testata + " " + releaseDate + " " + name + " ");
                // Take cover URL
                //Element linkPathImg = content.select("img").first();
                //coverUrl = Constants.IMG_URL + linkPathImg.attr("src");
                //Log.v(TAG, "Cover " + coverUrl);
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
                Log.v(TAG, "Price " + price + " description " + description);

                // Calculating date for sql
                Date myDate = DateCreator.elaborateDate(releaseDate);
                // Insert comic on database
                ComicDatabaseManager.insert(mContext, name, Constants.Sections.getName(Constants.Sections.STAR), description, releaseDate, myDate, coverUrl, feature, price, "no", "no", URL);
            } catch (Exception e) {
                Log.w(TAG, "Error while searching data for comic id " + i + " " + e.toString());
                comicErrorStar = true;
            }
        }
    }

    /**
     * Method used to start search for Bonneli comics.
     * @return true if search was successful, false otherwise
     */
    public boolean startParseBonelli() {
        Log.i(TAG, "Start searching Bonelli comics");
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
        Log.d(TAG, "Parsing Bonelli URL " + url + " - start");
        try {
            // Creating doc file from URL
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .maxBodySize(0)
                    .timeout(10 * 1000) // timeout to 10 seconds
                    .get();

            // Finding release date
            String releaseTag = doc.select("p.tag_2").html();
            Document releasePart = Jsoup.parse(releaseTag);
            Elements spanRelease = releasePart.select("span.valore");

            // Finding comics name with number
            Elements spanTitle = doc.select("span._summary");

            // Finding link for other info and title ig needed
            Elements spanOther = doc.select("span._url");

            // Iterate all elements founded
            String name, releaseDate, description = "N.D.", price = "N.D.", feature = "N.D.", coverUrl,
                    moreInfoUrl, periodicityTag, title = "N.D.";
            for (int i = 0; i < spanRelease.size(); i++) {
                name = spanTitle.get(i).text();
                moreInfoUrl = spanOther.get(i).text().replace(Constants.MAIN_URL, "");
                moreInfoUrl = Constants.MAIN_URL + moreInfoUrl;
                Log.v(TAG, name + " " + moreInfoUrl);
                try {
                    // Creating doc file from URL
                    Document docMoreInfo = Jsoup.connect(moreInfoUrl)
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .maxBodySize(0)
                            .timeout(10 * 1000) // timeout to 10 seconds
                            .get();

                    // Finding periodicity
                    periodicityTag = docMoreInfo.select("p.tag_3").html();
                    Document periodicityPart = Jsoup.parse(periodicityTag);
                    Elements spanPeriod = periodicityPart.select("span.valore");
                    if (!spanPeriod.isEmpty()) {
                        feature = spanPeriod.get(0).text();
                        Log.v(TAG, "Comic feature: " + feature);
                    }

                    // Finding comic title
                    Elements titleIte = docMoreInfo.select("h1");
                    if (!titleIte.isEmpty()) {
                        title = titleIte.get(0).text();
                        Log.v(TAG, "Comic title: " + title);
                    }

                    // Finding comic description
                    Elements desc = docMoreInfo.select("div[class=testo_articolo testo testoResize]");
                    if (!desc.isEmpty()) {
                        description = desc.get(0).text();
                        Log.v(TAG, "Comic description: " + description);
                    }

                    // Finding cover image
                    //Elements linkPathImg = docMoreInfo.select("div.bk-cover img[src]");
                    //coverUrl = Constants.MAIN_URL + linkPathImg.attr("src");
                    //Log.v(TAG, "Comic cover: " + coverUrl);
                    // N.B.: changed due to violation of copyright
                    coverUrl = "";

                    // Defining comic name
                    if (name.startsWith("N°.")) {
                        name = title.trim().replace("n°", "");
                    } else {
                        name = name.substring(0, name.indexOf("-")).trim().replace("N°.", "");
                    }
                    Log.v(TAG, "Comic name: " + name);

                    // Calculating date for sql
                    releaseDate = spanRelease.get(i).text();
                    Date myDate = DateCreator.elaborateDate(releaseDate);

                    // Insert comic on database
                    String editor = Constants.Sections.getName(Constants.Sections.BONELLI);
                    ComicDatabaseManager.insert(mContext, name.toUpperCase(), editor, description, releaseDate, myDate, coverUrl, feature, price, "no", "no", url);
                } catch (Exception e) {
                    Log.w(TAG, "Can't take more info from " + moreInfoUrl + " " + e.toString() + " comic not fetched");
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error while comic fetching " + url + " " + e.toString());
            comicErrorBonelli = true;
        }

        Log.v(TAG, "Parsing Bonelli - end");
    }
}
