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

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class ParserStar extends Parser {

    private static final String TAG = ParserStar.class.getSimpleName();

    private static final String BASE_URL = "https://www.starcomics.com";
    private static final String ROOT = BASE_URL + "/UsciteMensili.aspx?AspxAutoDetectCookieSupport=1";
    private static final String COMIC_ROOT = BASE_URL + "/fumetto.aspx?Fumetto=";

    /**
     * Method used to start search for SC comics. <br>
     * This method will launch a search for each comic URL.
     * @return a list found comic
     */
    @Override
    public ArrayList<ComicEntity> initParser() {
        CCLogger.i(TAG, "initParser - Start searching Star Comics comics");

        return parseUrl(ROOT);
    }

    @Override
    public ArrayList<ComicEntity> parseUrl(String url) {
        CCLogger.v(TAG, "parseUrl - Parsing " + url);

        // First part: arse root page and find the first comic of the month
        Document doc;
        try {
            doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
        } catch (Exception e) {
            // Unable to find data for Star Comics
            CCLogger.w(TAG, "parseUrl - Error while searching release from Star Comics site " + e.toString());
            ParserLog.increaseWrongStarURL();
            return null;
        }

        // Second part: elaborate first index where to start parse comic one by one
        int firstIndex = elaborateFirstComicIndex(doc);
        if (firstIndex <= 0) {
            return null;
        }

        // Third part: parse every single URL to find comic data
        int lastIndex = firstIndex + 40;
        CCLogger.v(TAG, "parseUrl - Parsing from " + firstIndex + " to " + lastIndex);

        ArrayList<ComicEntity> comicEntities = new ArrayList<>();
        ComicEntity comicEntity;
        for (int i = firstIndex; i <= lastIndex; i++) {
            String URL = COMIC_ROOT + i;
            comicEntity = parseSingleComicURL(URL);
            if (comicEntity != null) {
                comicEntities.add(comicEntity);
            }
        }

        CCLogger.v(TAG, "parseUrl - Found " + comicEntities.size() + " comics!");

        return comicEntities;
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

    private ComicEntity parseSingleComicURL(String url) {
        // Take data from web and save it on document
        Document docSingleComic;
        try {
            // Create doc (ISO-8859-1 CP1252 UTF-8)
            docSingleComic = Jsoup.parse(new URL(url)
                    .openStream(), "UTF-8", url);
        } catch (Exception e) {
            CCLogger.w(TAG, "parseSingleComicURL - This url does not exists " + url + " " + e.toString());
            ParserLog.increaseWrongStarURL();
            return null;
        }

        ParserLog.increaseParsedStarURL();

        // Take every info about comic on element
        Element content;
        try {
            content = docSingleComic.getElementsByTag("article").first();
        } catch (Exception e) {
            CCLogger.w(TAG, "parseSingleComicURL - Can't take a list of elements " + url + " " + e.toString());
            ParserLog.increaseWrongStarElements();
            return null;
        }

        // Init mandatory data
        String title, releaseDate;
        Date myDate;
        // Init optional data
        String description, price, feature, coverUrl;

        try {
            // Take various info (name, release date and number)
            CCLogger.v(TAG, "parseSingleComicURL - Start inspecting element:\n" + content.toString());

            title = searchTitle(content);
            if (title == null) {
                CCLogger.w(TAG, "parseSingleComicURL - Title not found!");
                ParserLog.increaseErrorOnParsingComic();
                return null;
            }

            releaseDate = searchReleaseDate(content);
            if (releaseDate == null) {
                CCLogger.w(TAG, "parseSingleComicURL - Release date not found!");
                ParserLog.increaseErrorOnParsingComic();
                return null;
            } else {
                // Calculating date for SQL
                myDate = DateCreator.elaborateDate(releaseDate);
            }

            CCLogger.d(TAG, "parseSingleComicURL - Results:\nComic title : " + title + "\nRelease date : " + releaseDate);

            coverUrl = searchCover(content);
            description = searchDescription(content);
            feature = searchFeature(content);
            price = searchPrice(content);

            CCLogger.d(TAG, "parseSingleComicURL - Results:\nCover url : " + coverUrl + "\nFeature : " + feature + "\nDescription : " + description + "\nPrice : " + price);

            return new ComicEntity(title.toUpperCase(), myDate, description,
                    price, feature, coverUrl, Constants.Sections.STAR.getName(), false, false, url);
        } catch (Exception e) {
            CCLogger.w(TAG, "parseSingleComicURL - Error while searching data for comic id " + e.toString() + "\n" + url);
            ParserLog.increaseErrorOnParsingComic();
            return null;
        }
    }

    private int elaborateFirstComicIndex(Document doc) {
        Element content = doc.select("div.content.clearfix").first();
        if (content == null) {
            CCLogger.w(TAG, "elaborateFirstComicIndex - Element 'content' is NULL!");
            return -1;
        }

        Element photo = content.select("a[href]").first();
        if (photo == null) {
            CCLogger.w(TAG, "elaborateFirstComicIndex - Element 'photo' is NULL!");
            return -1;
        }

        String myLink = photo.attr("href").replace("fumetto.aspx?Fumetto=", "");
        try {
            return Integer.parseInt(myLink);
        } catch (Exception e) {
            CCLogger.w(TAG, "elaborateFirstComicIndex - Element 'href' is NULL! " + e.toString());
            return -1;
        }
    }
}
