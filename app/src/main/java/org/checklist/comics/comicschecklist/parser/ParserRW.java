package org.checklist.comics.comicschecklist.parser;

import android.content.Context;

import org.checklist.comics.comicschecklist.database.AppDatabase;
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
import java.util.Calendar;
import java.util.Date;

public class ParserRW extends Parser {

    private static final String TAG = ParserRW.class.getSimpleName();

    private static final String BASE_URL = "http://www.rwedizioni.it";
    private static final String FIRST_RW = BASE_URL + "/news/uscite";
    private static final String MIDDLE_RW = "-";
    private static final String END_RW = "/";
    private static final String MEDIA_RW = BASE_URL + "/media/";

    private String mCurrentReleaseDate;

    public ParserRW(Context context) {
        super(context);
    }

    /**
     * Method used to search RW comics. <br>
     * This method will search URL by URL, which is supposed to have a list of comics.
     * @return true if search was successful, false otherwise
     */
    @Override
    public boolean startParsing() {
        CCLogger.i(TAG, "startParsing - Start searching for RW comics");
        boolean parseError = false;

        int dayToSearch = 5;
        for (int i = -3; i <= dayToSearch; i++) {
            CCLogger.d(TAG, "startParsing - Today is " + DateCreator.getTodayString() + " adding " + i + " day(s)");
            mCurrentReleaseDate = searchReleaseDate(i);
            int day = DateCreator.getTargetDay(i).get(Calendar.DAY_OF_MONTH);

            // Example : http://www.rwedizioni.it/news/uscite-2-dicembre/
            String url = FIRST_RW + MIDDLE_RW + day + MIDDLE_RW +
                    DateCreator.getTargetReadableMonth(i) + END_RW;
            parseError = parseUrl(url);
        }

        return parseError;
    }

    /**
     * Method used to search data from RW URL.
     * @param url the URL to parse
     */
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
            CCLogger.w(TAG, "parseUrl - Error while parsing URL " + url + " " + e.toString());
            ParserLog.increaseWrongRWURL();
            return true;
        }

        ParserLog.increaseParsedRWURL();

        // Take every potential comic as elements
        Element content = doc.getElementById("content");
        if (content == null) {
            CCLogger.w(TAG, "parseUrl - Can't take a list of elements " + url + " because content is NULL!");
            ParserLog.increaseWrongRWElements();
            return true;
        }

        // Init mandatory data (we already have computed the release date)
        String title;
        Date myDate = DateCreator.elaborateDate(mCurrentReleaseDate);
        // Init optional data
        String description, coverUrl, feature, price;

        ArrayList<ComicEntity> comicsList = new ArrayList<>();

        // Parse comic data
        Elements pElements = content.select("p");
        for (Element element : pElements) {
            Elements checkForImg = element.getElementsByTag("img");
            CCLogger.v(TAG, "parseUrl - Start inspecting element:\n" + element.toString());

            title = searchTitle(checkForImg);
            if (title == null) {
                CCLogger.w(TAG, "parseUrl - Title not found!");
                ParserLog.increaseErrorOnParsingComic();
                continue;
            }

            CCLogger.d(TAG, "parseUrl - Results:\nComic title : " + title + "\nRelease date : " + mCurrentReleaseDate);

            coverUrl = searchCover(checkForImg);
            description = searchDescription(checkForImg);
            feature = searchFeature(element);
            price = searchPrice(element);

            CCLogger.d(TAG, "parseUrl - Results:\nCover url : " + coverUrl + "\nFeature : " + feature + "\nDescription : " + description + "\nPrice : " + price);

            // Insert found comic on list
            ComicEntity comic = new ComicEntity(title.toUpperCase(), myDate, description,
                    price, feature, coverUrl, Constants.Sections.RW.getName(), false, false, url);

            comicsList.add(comic);
        }

        // Get reference to database and insert data
        AppDatabase database = AppDatabase.getInstance(mContext.getApplicationContext());
        AppDatabase.insertData(database, comicsList);

        return false;
    }

    @Override
    public String searchTitle(Object object) {
        Elements elements = (Elements) object;
        String title = null;

        if (!elements.isEmpty()) {
            Element src = elements.get(0);
            String coverUrl = src.attr("src");
            if (coverUrl.startsWith(MEDIA_RW)) {
                title = coverUrl.replace(MEDIA_RW, "").replace("_", " ").replace(".jpg", "");
                if (title.contains("-")) {
                    title = title.substring(0, title.indexOf("-"));
                }

                // Title elaborated
                CCLogger.v(TAG, "searchTitle - Title " + title);
            }
        }

        return title;
    }

    @Override
    public String searchReleaseDate(Object object) {
        int daysToAdd = (Integer) object;
        String releaseDate = DateCreator.getStringTargetDay(daysToAdd);
        CCLogger.v(TAG, "searchReleaseDate - Computed release date " + releaseDate);
        return releaseDate;
    }

    @Override
    public String searchCover(Object object) {
        Elements elements = (Elements) object;
        String coverUrl = null;

        if (!elements.isEmpty()) {
            Element src = elements.get(0);
            String rawCoverUrl = src.attr("src");
            if (rawCoverUrl.startsWith(MEDIA_RW)) {
                // Saving cover. N.B.: changed due to violation of copyright
                coverUrl = rawCoverUrl;
                CCLogger.v(TAG, "searchCover - Cover URL " + coverUrl);
            }
        }

        if (coverUrl == null) {
            CCLogger.w(TAG, "searchCover - Cover not found, setting default value!");
            coverUrl = "";
        }

        return coverUrl;
    }

    @Override
    public String searchDescription(Object object) {
        return "N.D.";
    }

    @Override
    public String searchFeature(Object object) {
        String text = inspectElement((Element) object);
        String feature;

        if (text != null && text.contains("col.") || text != null && text.contains("b/n")) {
            // Found a <p> with more info
            feature = text;
            CCLogger.v(TAG, "searchFeature - Feature " + feature);
        } else {
            CCLogger.w(TAG, "searchFeature - Feature not found, setting default value!");
            feature = "N.D.";
        }

        return feature;
    }

    @Override
    public String searchPrice(Object object) {
        String text = inspectElement((Element) object);
        String price;

        if (text != null && text.contains("€")) {
            // Found price text on <p>
            price = text;
            CCLogger.v(TAG, "searchPrice - Price " + price);
        } else {
            CCLogger.w(TAG, "searchPrice - Price not found, setting default value!");
            price = "N.D.";
        }

        return price;
    }

    private String inspectElement(Element element) {
        String elementText = element.text();
        if (!elementText.equalsIgnoreCase("dc") && !elementText.equalsIgnoreCase("rw-lion")
                && !elementText.equalsIgnoreCase("lineachiara") && !elementText.equalsIgnoreCase("goen")
                && !elementText.equalsIgnoreCase(" ") && !elementText.equalsIgnoreCase("vertigo presenta")
                && !elementText.equalsIgnoreCase("dc deluxe") && !elementText.equalsIgnoreCase("vertigo")
                && !elementText.equalsIgnoreCase("vertigo deluxe") && !elementText.equalsIgnoreCase("rw-goen")
                && !elementText.equalsIgnoreCase("DC Universe Presenta") && !elementText.equalsIgnoreCase("DC Presenta")
                && !elementText.equalsIgnoreCase("dc all star presenta") && elementText.length() > 1) {
            return elementText;
        } else {
            return null;
        }
    }
}
