package org.checklist.comics.comicschecklist.parser;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.checklist.comics.comicschecklist.provider.ComicContentProvider;
import org.checklist.comics.comicschecklist.database.ComicDatabase;
import org.checklist.comics.comicschecklist.util.Constants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by Francesco Bevilacqua on 20/10/2014.
 * This code is part of ParserTest project.
 */
public class Parser {

    private static final String TAG = Parser.class.getSimpleName();

    private Context mContext;
    private boolean comicErrorBonelli;
    private boolean comicErrorPanini;
    private boolean comicErrorRw;
    private boolean comicErrorStar;

    /**
     * Costruttore della classe.
     */
    public Parser(Context context) {
        mContext = context;
    }

    public boolean startParsePanini(String editor) {
        Log.d(TAG, "Inizio scansione Panini");
        comicErrorPanini = false;

        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int nextYear = year + 1;
        int weekOfTheYear = calendar.get(Calendar.WEEK_OF_YEAR);

        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date lastDec;
        try {
            lastDec = formatter.parse("28/12/" + year);
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
        } catch (ParseException e) {
            Log.w(TAG, "Can't compute date: search Panini interrupted");
            e.printStackTrace();
        }

        return comicErrorPanini;
    }

    // Metodo che raccoglie i dati dall'url fornito.
    private void parsePaniniUrl(String url, String editor) {
        Log.v(TAG, "Inizio scansione URL " + editor + " " + url);
        ArrayList<String> arrayCoverUrl = new ArrayList<>();
        ArrayList<String> arrayName = new ArrayList<>();
        ArrayList<String> arrayFeature = new ArrayList<>();
        ArrayList<String> arrayPrice = new ArrayList<>();
        ArrayList<String> arrayReleaseDate = new ArrayList<>();
        ArrayList<String> arrayDescription = new ArrayList<>();
        try {
            // Prendo solo la parte di codice che mi interessa
            Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);

            // Aggiungo all'array le url delle cover
            String docPath = doc.select("div.cover").html();
            Document divPath = Jsoup.parse(docPath);
            for (Element element5 : divPath.select("img")) arrayCoverUrl.add(element5.attr("src"));
            // Aggiungo il titolo del fumetto e le feature
            String docTitle = doc.select("div.title").html();
            Document divTitle = Jsoup.parse(docTitle);
            for (Element element4 : divTitle.select("h3")) arrayName.add(element4.text());
            for (Element element3 : divTitle.select("p.features")) {
                String feature = element3.text();
                arrayFeature.add(feature);
            }
            // Calcolo il prezzo del fumetto
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
            // Parte di codice che prende la data d'uscita del fumetto
            String docDR = doc.select("div.logo_brand").html();
            Document divDR = Jsoup.parse(docDR);
            for (Element element1 : divDR.select("span")) arrayReleaseDate.add(element1.text());
            // Parte di codice che prende la descrizione del fumetto
            String docDesc = doc.select("div.desc").html();
            Document divDesc = Jsoup.parse(docDesc);
            for (Element element : divDesc.select("p")) arrayDescription.add(element.text());
        } catch (Exception e) {
            Log.d(TAG, "Something is wrong with parsePaniniUrl " + url + " " + e.toString());
        }

        // Unisco tutti i dati e li inserisco nel com.example.fra.parsertest.database
        for (int i = 0; i < arrayCoverUrl.size(); i++) {
            StringTokenizer tokenizer = new StringTokenizer(arrayName.get(i));
            String title = tokenizer.nextToken();
            if (!title.equalsIgnoreCase("play") && !arrayName.get(i).equalsIgnoreCase("n.d.")) {
                try {
                    // Calculating date for sql
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date myDate = formatter.parse(arrayReleaseDate.get(i));
                    insertComic(arrayName.get(i), editor, arrayDescription.get(i), arrayReleaseDate.get(i),
                            myDate, Constants.URLPANINI + arrayCoverUrl.get(i), arrayFeature.get(i), arrayPrice.get(i));
                } catch (Exception e) {
                    // Error during fetching of a comic
                    Log.w(TAG, title + " " + e.toString());
                    comicErrorPanini = true;
                }
            }
        }
    }

    public boolean startParseRW() {
        Log.d(TAG, "Inizio scansione RW");
        comicErrorRw = false;

        Calendar calendar = new GregorianCalendar();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int monthInt = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String month = DateFormatSymbols.getInstance(Locale.ITALIAN).getMonths()[monthInt];
        String url;
        for (int i = 1; i < day + 3; i++) {
            url = Constants.FIRSTRW + i + Constants.MIDDLERW + month + Constants.MIDDLERW + year + Constants.ENDRW;
            parseUrlRW(url, i + "/" + (monthInt + 1) + "/" + year);
        }

        return comicErrorRw;
    }

    /** Metodo che raccoglie i dati dall'url fornito. */
    private void parseUrlRW(String url, String releaseDate) {
        Log.v(TAG, "Inizio scansione URL RW " + url);
        try {
            // Prendo solo la parte di codice che mi interessa
            Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
            Element content = doc.getElementById("content");

            // Ogni tag p contiene un fumetto
            Elements pElements = content.select("p");
            String name, description = "N.D.", price = "N.D.", feature = "N.D.", coverUrl = "N.D.";

            try {
                // Calculating date for sql
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date myDate = formatter.parse(releaseDate);

                // Detecting all p tag
                for (Element pElement : pElements) {
                    Elements checkForA = pElement.getElementsByTag("a");
                    Elements checkForImg = pElement.getElementsByTag("img");
                    if (!checkForA.isEmpty() || !checkForImg.isEmpty()) {

                        // Check if we have more then one comic on <p> tag
                        if (checkForA.size() > 1) {
                            // More comic founded, insert only name, image, release and editor
                            insertComic(pElement.select("span[style^=color: #ff0000]").first().text(), Constants.Editors.RW.name(),
                                    description, releaseDate, myDate, checkForA.first().attr("href"), feature, price);

                            insertComic(pElement.select("span[style^=color: #ff0000]").last().text(), Constants.Editors.RW.name(),
                                    description, releaseDate, myDate, checkForA.last().attr("href"), feature, price);
                        } else {
                            // Only one comic founded: getting all junk data
                            Elements innerP = pElement.getElementsByTag("strong");
                            name = pElement.select("span[style^=color: #ff0000]").last().text();
                            if (innerP.size() > 1) {
                                price = innerP.last().text();
                                feature = innerP.get(innerP.size() - 2).text();
                            } else {
                                price = "N.D.";
                                feature = "N.D.";
                            }
                            //Log.i(Constants.LOG_TAG, "Comic founded " + name + " " + price + " " + feature);

                            // Getting image URL
                            if (!checkForA.isEmpty()) {
                                for (Element link : checkForA) {
                                    if (link.attr("href").startsWith(Constants.MEDIARW)) {
                                        coverUrl = link.attr("href");
                                        //Log.i(Constants.LOG_TAG, "Comic img path: " + coverUrl);
                                    }
                                }
                            } else if (!checkForImg.isEmpty()) {
                                for (Element link : checkForImg) {
                                    if (link.attr("src").startsWith(Constants.MEDIARW)) {
                                        coverUrl = link.attr("src");
                                    }
                                }
                            } else
                                coverUrl = "unknown";

                            // Finding description
                            Elements innerStrong = pElement.select("strong");
                            Elements innerSpan = pElement.select("span");
                            for (Element inS2 : innerStrong) {
                                inS2.remove();
                            }
                            for (Element inSpan : innerSpan) {
                                inSpan.remove();
                            }
                            Elements innerA = pElement.getElementsByTag("a");
                            for (Element inA2 : innerA) {
                                inA2.remove();
                            }

                            // Clean description from html code
                            description = pElement.toString();
                            description = description.replace("<b>", "").replace("</b>", "").replace("<br>", "").replace("<p>", "").replace("<br />", "").replace("</p>", "").trim();
                            if (description.length() == 0)
                                description = "N.D.";

                            // Insert comic on database
                            insertComic(name, Constants.Editors.RW.name(), description, releaseDate, myDate, coverUrl, feature, price);
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error during comic fetching " + e.toString());
                comicErrorRw = true;
            }
        } catch (Exception e) {
            Log.w(TAG, "This url does not exists " + url + " " + e.toString());
        }
    }

    public boolean startParseStarC() {
        Log.d(TAG, "Inizio scansione Star Comics");
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
            Log.w(TAG, "Error on startParseStarC " + e.toString());
        }

        if (from != 0 && to != 0 && from < to)
            parseUrlStarC(from, to);

        return comicErrorStar;
    }

    private void parseUrlStarC(int from, int to) {
        Log.v(TAG, "Inizio scansione URL Star comics da " + from + " to " + to);
        // Parte di codice che cerca tutti i fumetti
        Document doc;
        String name, releaseDate = "", description, price, feature = "N.D.", coverUrl, testata = "", number = "";
        for (int i = from; i <= to; i++) {
            //Log.i(Constants.LOG_TAG, "Comic number = " + i);
            // Blocco di codice che cercherà di aprire l'url e ricercare i dati richiesti
            try {
                // Recupero i dati del fumetto (ISO-8859-1 CP1252 UTF-8)
                doc = Jsoup.parse(new URL(Constants.COMIC_ROOT + i).openStream(), "UTF-8", Constants.COMIC_ROOT + i);
                Element content = doc.getElementsByTag("article").first();
                // Info varie
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
                //Log.i(Constants.LOG_TAG, testata + " " + releaseDate + " " + name + " ");
                // Parte di codice recupera l'indirizzo della copertina
                Element linkPathImg = content.select("img").first();
                coverUrl = Constants.IMG_URL + linkPathImg.attr("src");
                //Log.i(Constants.LOG_TAG, coverUrl);
                // Desc and price
                Elements pList = content.select("p");
                description = pList.get(1).text();
                try {
                    price = pList.get(2).select("span").get(1).text();
                } catch (IndexOutOfBoundsException e) {
                    price = "N.D.";
                }
                //Log.i(Constants.LOG_TAG, price + " " + description);

                // Calculating date for sql
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date myDate = formatter.parse(releaseDate);
                // Insert comic on database
                insertComic(name, Constants.Editors.STAR.name(), description, releaseDate, myDate, coverUrl, feature, price);
            } catch (Exception e) {
                Log.w(TAG, "Error on parseUrlStarC comic id " + i + " " + e.toString());
                comicErrorStar = true;
            }
        }
    }

    public boolean startParseBonelli() {
        // TODO fix parser
        Log.d(TAG, "Inizio scansione Bonelli");
        comicErrorBonelli = false;

        parseUrlBonelli(Constants.EDICOLA_INEDITI);
        parseUrlBonelli(Constants.EDICOLA_RISTAMPE);
        parseUrlBonelli(Constants.EDICOLA_RACCOLTE);
        parseUrlBonelli(Constants.PROSSIMAMENTE_INEDITI);
        parseUrlBonelli(Constants.PROSSIMAMENTE_RISTAMPE);
        parseUrlBonelli(Constants.PROSSIMAMENTE_RACCOLTE);

        return comicErrorBonelli;
    }

    /** Metodo che raccoglie i dati dall'url fornito. */
    private void parseUrlBonelli(String url) {
        Log.v(TAG, "Inizio scansione URL Bonelli " + url);
        try {
            // Creating doc file from URL
            Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);

            // Finding release date
            String releaseTag = doc.select("p.tag_2").html();
            Document releasePart = Jsoup.parse(releaseTag);
            Elements spanRelease = releasePart.select("span.valore");

            // Finding comics name with number
            Elements spanTitle = doc.select("span._summary");

            // Finding link for other info and title ig needed
            Elements spanOther = doc.select("span._url");

            //Log.i(Constants.LOG_TAG, "Title, info and release array length : " + spanTitle.size() + " " + spanOther.size() + " " + spanRelease.size());

            // Iterate all elements founded
            String name, releaseDate, description = "N.D.", price = "N.D.", feature = "N.D.", coverUrl = "N.D.",
                    moreInfoUrl, periodicityTag, title = "N.D.";
            for (int i = 0; i < spanRelease.size(); i++) {
                name = spanTitle.get(i).text();
                moreInfoUrl = spanOther.get(i).text();
                //Log.i("Parser", name + " " + moreInfoUrl);
                try {
                    // Creating doc file from URL
                    Document docMoreInfo = Jsoup.parse(new URL(moreInfoUrl).openStream(), "UTF-8", moreInfoUrl);

                    // Finding periodicity
                    periodicityTag = docMoreInfo.select("p.tag_3").html();
                    Document periodicityPart = Jsoup.parse(periodicityTag);
                    Elements spanPeriod = periodicityPart.select("span.valore");
                    if (!spanPeriod.isEmpty())
                        feature = spanPeriod.get(0).text();

                    // Finding comic title
                    Elements titleIte = docMoreInfo.select("h1");
                    if (!titleIte.isEmpty())
                        title = titleIte.get(0).text();
                    //Log.i(Constants.LOG_TAG, "Comic title: " + titleIte.get(0).text());

                    // Finding comic description
                    Elements desc = docMoreInfo.select("div.testo_articolo");
                    if (!desc.isEmpty())
                        description = desc.get(0).text();

                    // Finding cover image
                    Elements linkPathImg = docMoreInfo.select("div.bk-cover img[src]");
                    coverUrl = Constants.COVER_PART + linkPathImg.attr("src");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Defining comic name
                //Log.i(Constants.LOG_TAG, "name = " + name + " title = " + title);
                if (name.startsWith("N°.")) {
                    name = title.trim().replace("n°", "");
                } else
                    name = name.substring(0, name.indexOf("-")).trim().replace("N°.", "");

                // Calculating date for sql
                releaseDate = spanRelease.get(i).text();
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date myDate = formatter.parse(releaseDate);

                // Insert comic on database
                insertComic(name.toUpperCase(), Constants.Editors.BONELLI.name(), description, releaseDate, myDate, coverUrl, feature, price);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error during comic fetching " + url + " " + e.toString());
            comicErrorBonelli = true;
        }
    }

    private void insertComic(String name, String editor, String description, String releaseDate,
                             Date date, String coverUrl, String feature, String price) {
        Log.v(TAG, "Insert comic " + name + " " + editor + " " + releaseDate);
        ContentValues values = new ContentValues();
        values.put(ComicDatabase.COMICS_NAME_KEY, name);
        values.put(ComicDatabase.COMICS_EDITOR_KEY, editor);
        values.put(ComicDatabase.COMICS_DESCRIPTION_KEY, description);
        values.put(ComicDatabase.COMICS_RELEASE_KEY, releaseDate);
        values.put(ComicDatabase.COMICS_DATE_KEY, date.getTime());
        values.put(ComicDatabase.COMICS_COVER_KEY, coverUrl);
        values.put(ComicDatabase.COMICS_FEATURE_KEY, feature);
        values.put(ComicDatabase.COMICS_PRICE_KEY, price);
        values.put(ComicDatabase.COMICS_CART_KEY, "no");
        values.put(ComicDatabase.COMICS_FAVORITE_KEY, "no");

        mContext.getContentResolver().insert(ComicContentProvider.CONTENT_URI, values);
    }
}
