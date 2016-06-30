package org.checklist.comics.comicschecklist.parser;

import android.content.Context;
import android.util.Log;

import org.checklist.comics.comicschecklist.database.ComicDatabaseManager;
import org.checklist.comics.comicschecklist.util.Constants;
import org.checklist.comics.comicschecklist.util.DateCreator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
     * Method used to search Panini comics.
     * @param editor the editor to search
     * @return true if search was successful, false otherwise
     */
    public boolean startParsePanini(String editor) {
        Log.i(TAG, "Start searching for Panini comics");
        comicErrorPanini = false;

        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int nextYear = year + 1;
        int weekOfTheYear = calendar.get(Calendar.WEEK_OF_YEAR);

        // Calculating last days of year
        Date lastDec = DateCreator.elaborateDate("28/12/" + year);

        Calendar calIt = Calendar.getInstance(Locale.ITALY);
        calIt.setTime(lastDec);

        if (weekOfTheYear <= (calIt.get(Calendar.WEEK_OF_YEAR) - 3)) {
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + (weekOfTheYear - 1), editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + weekOfTheYear, editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + (weekOfTheYear + 1), editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + (weekOfTheYear + 2), editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + (weekOfTheYear + 3), editor);
        } else if (weekOfTheYear == (calIt.get(Calendar.WEEK_OF_YEAR) - 2)) {
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + (weekOfTheYear - 1), editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + weekOfTheYear, editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + (weekOfTheYear + 1), editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + (weekOfTheYear + 2), editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + nextYear + Constants.THIRDPANINI + 1, editor);
        } else if (weekOfTheYear == (calIt.get(Calendar.WEEK_OF_YEAR) - 1)) {
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + (weekOfTheYear - 1), editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + weekOfTheYear, editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + (weekOfTheYear + 1), editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + nextYear + Constants.THIRDPANINI + 1, editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + nextYear + Constants.THIRDPANINI + 2, editor);
        } else if (weekOfTheYear == calIt.get(Calendar.WEEK_OF_YEAR)) {
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + (weekOfTheYear - 1), editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + year + Constants.THIRDPANINI + weekOfTheYear, editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + nextYear + Constants.THIRDPANINI + 1, editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + nextYear + Constants.THIRDPANINI + 2, editor);
            parsePaniniUrl(Constants.FIRSTPANINI + editor + Constants.SECONDPANINI + nextYear + Constants.THIRDPANINI + 3, editor);
        }

        return comicErrorPanini;
    }

    /**
     * Private method used to parse a Panini URL
     * @param url the URL to parse
     * @param editor the editor of URL
     */
    private void parsePaniniUrl(String url, String editor) {
        Log.d(TAG, "Parsing " + editor + " " + url);
        ArrayList<String> arrayCoverUrl = new ArrayList<>();
        ArrayList<String> arrayName = new ArrayList<>();
        ArrayList<String> arrayFeature = new ArrayList<>();
        ArrayList<String> arrayPrice = new ArrayList<>();
        ArrayList<String> arrayReleaseDate = new ArrayList<>();
        ArrayList<String> arrayDescription = new ArrayList<>();
        try {
            // Take data and save it on document
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .maxBodySize(0)
                    .get();

            // Create array of cover
            String docPath = doc.select("div.cover").html();
            Document divPath = Jsoup.parse(docPath);
            for (Element element5 : divPath.select("img")) {
                String rawCover = element5.attr("src");
                if (rawCover.contains(Constants.IMAGE_NOT_AVAILABLE) ||
                        rawCover.equalsIgnoreCase(Constants.IMAGE_NOT_AVAILABLE_URL)) {
                    arrayCoverUrl.add("");
                } else {
                    arrayCoverUrl.add(rawCover);
                }
            }
            // Create array of title and features
            String docTitle = doc.select("div.title").html();
            Document divTitle = Jsoup.parse(docTitle);
            for (Element element4 : divTitle.select("h3")) {
                arrayName.add(element4.text());
            }
            for (Element element3 : divTitle.select("p.features")) {
                String feature = element3.text();
                arrayFeature.add(feature);
            }
            // Compute comic price
            String docPrice = doc.select("div.price").html();
            Document divPrice = Jsoup.parse(docPrice);
            for (Element element2 : divPrice.select("h4")) {
                String price = element2.text();
                try {
                    price = price.substring(7).replace(".", ",");
                } catch (Exception e) {
                    price = "N.D.";
                }
                arrayPrice.add(price.trim());
            }
            // Compute date release
            String docDR = doc.select("div.logo_brand").html();
            Document divDR = Jsoup.parse(docDR);
            for (Element element1 : divDR.select("span")) arrayReleaseDate.add(element1.text());
            // Take comic description
            String docDesc = doc.select("div.desc").html();
            Document divDesc = Jsoup.parse(docDesc);
            for (Element element : divDesc.select("p")) arrayDescription.add(element.text());
        } catch (Exception e) {
            Log.w(TAG, "Error while comic fetching " + url + " " + e.toString());
        }

        // Insert data on database
        for (int i = 0; i < arrayCoverUrl.size(); i++) {
            StringTokenizer tokenizer = new StringTokenizer(arrayName.get(i));
            String title = tokenizer.nextToken();
            if (!title.equalsIgnoreCase("play") && !arrayName.get(i).equalsIgnoreCase("n.d.")) {
                try {
                    // Calculating date for sql
                    Date myDate = DateCreator.elaborateDate(arrayReleaseDate.get(i));
                    ComicDatabaseManager.insert(mContext, arrayName.get(i), editor, arrayDescription.get(i), arrayReleaseDate.get(i),
                            myDate, Constants.URLPANINI + arrayCoverUrl.get(i), arrayFeature.get(i), arrayPrice.get(i), "no", "no", url);
                } catch (Exception e) {
                    // Error while comic fetching
                    Log.w(TAG, title + " " + e.toString());
                    comicErrorPanini = true;
                }
            }
        }
    }

    /**
     * Method used to search RW comics.
     * @return true if search was successful, false otherwise
     */
    public boolean startParseRW() {
        Log.i(TAG, "Start searching for RW comics");
        comicErrorRw = false;

        Calendar calendar = new GregorianCalendar();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int monthInt = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String month = DateFormatSymbols.getInstance(Locale.ITALIAN).getMonths()[monthInt];
        String url;
        for (int i = 1; i < day + 3; i++) {
            url = Constants.FIRSTRW + Constants.MIDDLERW + i + Constants.MIDDLERW + month + Constants.MIDDLERW + year + Constants.ENDRW;
            parseUrlRW(url, i + "/" + (monthInt + 1) + "/" + year);
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
                            // Saving cover
                            Log.v(TAG, "Cover " + coverUrl);
                            coverList.add(coverUrl);
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
                        ComicDatabaseManager.insert(mContext, titleList.get(i), Constants.Editors.getName(Constants.Editors.RW), description, releaseDate, myDate, coverList.get(i), featureList.get(i), priceList.get(i), "no", "no", siteUrl);
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
                Element linkPathImg = content.select("img").first();
                coverUrl = Constants.IMG_URL + linkPathImg.attr("src");
                Log.v(TAG, "Cover " + coverUrl);
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
                ComicDatabaseManager.insert(mContext, name, Constants.Editors.getName(Constants.Editors.STAR), description, releaseDate, myDate, coverUrl, feature, price, "no", "no", URL);
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
        Log.d(TAG, "Parsing Bonelli URL " + url);
        try {
            // Creating doc file from URL
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .maxBodySize(0)
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
                    Elements linkPathImg = docMoreInfo.select("div.bk-cover img[src]");
                    coverUrl = Constants.MAIN_URL + linkPathImg.attr("src");
                    Log.v(TAG, "Comic cover: " + coverUrl);

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
                    String editor = Constants.Editors.getName(Constants.Editors.BONELLI);
                    ComicDatabaseManager.insert(mContext, name.toUpperCase(), editor, description, releaseDate, myDate, coverUrl, feature, price, "no", "no", url);
                } catch (Exception e) {
                    Log.w(TAG, "Can't take more info from " + moreInfoUrl + " " + e.toString() + " comic not fetched");
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error while comic fetching " + url + " " + e.toString());
            comicErrorBonelli = true;
        }
    }
}
