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

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class ParserStar extends Parser {

    private static final String TAG = ParserStar.class.getSimpleName();

    private static final String BASE_URL = "https://www.starcomics.com";
    private static final String ROOT = BASE_URL + "/UsciteMensili.aspx?AspxAutoDetectCookieSupport=1";
    private static final String COMIC_ROOT = BASE_URL + "/fumetto.aspx?Fumetto=";

    private ArrayList<ComicEntity> mComicsList = new ArrayList<>();

    public ParserStar(Context context) {
        super(context);
    }

    /**
     * Method used to start search for SC comics. <br>
     * This method will launch a search for each comic URL.
     * @return true if search was successful, false otherwise
     */
    @Override
    public boolean startParsing() {
        CCLogger.i(TAG, "startParsing - Start searching Star Comics comics");
        boolean parseError;

        // Parse rootPath and find the first comic of the month
        Document doc;
        try {
            doc = Jsoup.parse(new URL(ROOT).openStream(), "UTF-8", ROOT);
        } catch (Exception e) {
            // Unable to find data for Star Comics
            CCLogger.w(TAG, "startParsing - Error while searching release from Star Comics site " + e.toString());
            ParserLog.increaseWrongStarURL();
            return false;
        }

        parseError = parseDoc(doc);

        return parseError;
    }

    @Override
    public boolean parseUrl(String url) {
        CCLogger.v(TAG, "parseUrl - Parsing " + url);

        // Take data from web and save it on document
        Document doc;
        try {
            // Create doc (ISO-8859-1 CP1252 UTF-8)
            doc = Jsoup.parse(new URL(url)
                    .openStream(), "UTF-8", url);
        } catch (Exception e) {
            CCLogger.w(TAG, "parseUrl - This url does not exists " + url + " " + e.toString());
            ParserLog.increaseWrongStarURL();
            return true;
        }

        ParserLog.increaseParsedStarURL();

        // Take every info about comic on element
        Element content;
        try {
            content = doc.getElementsByTag("article").first();
        } catch (Exception e) {
            CCLogger.w(TAG, "parseUrl - Can't take a list of elements " + url + " " + e.toString());
            ParserLog.increaseWrongStarElements();
            return true;
        }

        // Init mandatory data
        String title, releaseDate;
        Date myDate;
        // Init optional data
        String description, price, feature, coverUrl;

        try {
            // Take various info (name, release date and number)
            CCLogger.v(TAG, "parseUrl - Start inspecting element:\n" + content.toString());

            title = searchTitle(content);
            if (title == null) {
                CCLogger.w(TAG, "parseUrl - Title not found!");
                ParserLog.increaseErrorOnParsingComic();
                return true;
            }

            releaseDate = searchReleaseDate(content);
            if (releaseDate == null) {
                CCLogger.w(TAG, "parseUrl - Release date not found!");
                ParserLog.increaseErrorOnParsingComic();
                return true;
            } else {
                // Calculating date for SQL
                myDate = DateCreator.elaborateDate(releaseDate);
            }

            CCLogger.d(TAG, "parseUrl - Results:\nComic title : " + title + "\nRelease date : " + releaseDate);

            coverUrl = searchCover(content);
            description = searchDescription(content);
            feature = searchFeature(content);
            price = searchPrice(content);

            CCLogger.d(TAG, "parseUrl - Results:\nCover url : " + coverUrl + "\nFeature : " + feature + "\nDescription : " + description + "\nPrice : " + price);

            // Insert found comic on list
            ComicEntity comic = new ComicEntity(title.toUpperCase(), myDate, description,
                    price, feature, coverUrl, Constants.Sections.STAR.getName(), false, false, url);

            mComicsList.add(comic);
        } catch (Exception e) {
            CCLogger.w(TAG, "parseUrlStarC - Error while searching data for comic id " + e.toString() + "\n" + url);
            ParserLog.increaseErrorOnParsingComic();
            return true;
        }

        return false;
    }

    @Override
    public String searchTitle(Object object) {
        Element content = (Element) object;
        String header = null, number = null, title = null;

        // Take various info (header and number)
        Elements li = content.select("li"); // select all li
        for (Element aLi : li) {
            if (aLi.text().startsWith("Testata")) {
                header = aLi.text().substring("Testata".length()).trim();
            }
            if (aLi.text().startsWith("Numero")) {
                number = aLi.text().substring("Numero".length()).trim();
            }
        }

        if (header != null && number != null) {
            title = header + " " + number;
        }

        CCLogger.v(TAG, "parseUrlStarC - Title found " + title);

        return title;
    }

    @Override
    public String searchReleaseDate(Object object) {
        Element content = (Element) object;
        String releaseDate = null;

        // Take various info (header and number)
        Elements li = content.select("li"); // select all li
        for (Element aLi : li) {
            if (aLi.text().startsWith("Data di uscita")) {
                releaseDate = aLi.text().substring("Data di uscita".length()).trim();
            }
        }

        return releaseDate;
    }

    @Override
    public String searchCover(Object object) {
        // Take cover URL N.B.: changed due to violation of copyright
        Element content = (Element) object;
        Element linkPathImg = content.select("img").first();
        String rawCover = linkPathImg.attr("src");
        String coverUrl;
        if (!rawCover.equals("")) {
            coverUrl = BASE_URL + rawCover;
            CCLogger.v(TAG, "searchCover - Cover " + coverUrl);
        } else {
            CCLogger.w(TAG, "searchCover - Cover not found, setting default value!");
            coverUrl = "";
        }

        return coverUrl;
    }

    @Override
    public String searchDescription(Object object) {
        Element content = (Element) object;
        Elements pList = content.select("p");
        String rawDescription = pList.get(1).text();
        String description;
        if (!rawDescription.equals("")) {
            description = rawDescription;
            CCLogger.v(TAG, "searchDescription - Description " + description);
        } else {
            CCLogger.w(TAG, "searchDescription - Description not found, setting default value!");
            description = "N.D.";
        }

        return description;
    }

    @Override
    public String searchFeature(Object object) {
        return "N.D.";
    }

    @Override
    public String searchPrice(Object object) {
        Element content = (Element) object;
        Elements pList = content.select("p");
        String price;
        try {
            price = pList.get(2).select("span").get(1).text();
            CCLogger.v(TAG, "searchPrice - Price " + price);
        } catch (Exception e) {
            CCLogger.w(TAG, "searchPrice - Price not found, setting default value!");
            price = "N.D.";
        }

        return price;
    }

    private boolean parseDoc(Document doc) {
        int from;
        int to;
        boolean parseError = false;

        Element content = doc.select("div.content.clearfix").first();
        if (content == null) {
            CCLogger.w(TAG, "parseDoc - Element 'content' is NULL!");
            return false;
        }

        Element photo = content.select("a[href]").first();
        if (photo == null) {
            CCLogger.w(TAG, "parseDoc - Element 'photo' is NULL!");
            return false;
        }

        String myLink = photo.attr("href").replace("fumetto.aspx?Fumetto=", "");
        try {
            from = Integer.parseInt(myLink);
        } catch (Exception e) {
            CCLogger.w(TAG, "parseDoc - Element 'href' is NULL! " + e.toString());
            return false;
        }

        to = from + 40;
        CCLogger.v(TAG, "parseDoc - Parsing from " + from + " to " + to);

        if (from != 0 && to != 0 && from < to) {
            for (int i = from; i <= to; i++) {
                String URL = COMIC_ROOT + i;
                parseError = parseUrl(URL);
            }
        }

        // Get reference to repository and insert data
        CCLogger.v(TAG, "parseUrl - Inserting " + mComicsList.size() + " comics on DB");
        ((CCApp) mContext.getApplicationContext()).getRepository().insertComics(mComicsList);
        mComicsList.clear();

        return parseError;
    }
}
