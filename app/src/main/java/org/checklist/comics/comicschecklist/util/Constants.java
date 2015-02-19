package org.checklist.comics.comicschecklist.util;

/**
 * Created by Francesco Bevilacqua on 25/10/2014.
 * This code is part of ComicsChecklist project.
 */
public class Constants {
    // Log
    public static final String LOG_TAG = "ComicsChecklist";
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static final String ARG_SECTION_NUMBER = "section_number";
    // Remember the position of the selected item.
    public static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    public static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    public static final String PREF_USER_LEARNED_SLIDING_UP = "sliding_up_learned";
    // AppRater
    public static final String PREF_USER_DONT_RATE = "dont_show_again";
    public static final String PREF_APP_RATER = "app_rater";
    public static final String PREF_LAUNCH_COUNT = "launch_count";
    public static final String PREF_DATE_FIRST_LAUNCH = "date_first_launch";
    public final static int DAYS_UNTIL_PROMPT = 7;
    public final static int LAUNCHES_UNTIL_PROMPT = 7;
    // Parser
    public static final String MARVEL = "marvelitalia";
    public static final String PANINI = "paninicomics";
    public static final String PLANET = "planetmanga";
    public static final String BONELLI = "bonelli";
    public static final String STAR = "star";
    public static final String RW = "rw";
    // List created/searched by user
    public static final String FAVORITE = "preferiti";
    public static final String CART = "da comprare";
    // Preference last scan
    public static final String PREF_MARVEL_LAST_SCAN = "marvel_lastscan";
    public static final String PREF_PANINI_LAST_SCAN = "panini_lastscan";
    public static final String PREF_PLANET_LAST_SCAN = "planet_lastscan";
    public static final String PREF_STAR_LAST_SCAN = "star_lastscan";
    public static final String PREF_BONELLI_LAST_SCAN = "bonelli_lastscan";
    public static final String PREF_RW_LAST_SCAN = "rw_lastscan";
    // Service & Notification
    public static final int RESULT_START = 304;
    public static final int RESULT_CANCELED = 303;
    public static final int RESULT_FINISHED = 302;
    public static final int RESULT_DESTROYED = 301;
    public static final int RESULT_NOT_CONNECTED = 300;
    public static final int RESULT_EDITOR_FINISHED = 299;
    public static final String NOTIFICATION_RESULT = "result";
    public static final String NOTIFICATION_EDITOR = "editor";
    public static final String NOTIFICATION = "org.checklist.comics.comicschecklist.service.receiver";
    public static final int NOTIFICATION_ID = 299;
    public static final String PREF_SEARCH_NOTIFICATION = "notifications_new_message";
    public static final String PREF_FAVORITE_NOTIFICATION = "notifications_favorite_available";
    /**
     * Constants for parser class.
     */
    // Panini, Planet Manga & Marvel
    public static final String FIRSTPANINI = "http://www.paninicomics.it/web/guest/";
    public static final String SECONDPANINI = "/checklist?year=";
    public static final String THIRDPANINI = "&weekOfYear=";
    public static final String URLPANINI = "http://www.paninicomics.it";
    // RWEdizioni ex.: http://www.rwedizioni.it/news/rw-edizioni-uscite-del-10-maggio/
    public static final String FIRSTRW = "http://www.rwedizioni.it/news/rw-edizioni-uscite-del-";
    public static final String MIDDLERW = "-";
    public static final String ENDRW = "/";
    public static final String MEDIARW = "http://www.rwedizioni.it/media";
    // Star Comics
    public static final String ROOT = "http://www.starcomics.com/UsciteMensili.aspx";
    public static final String COMIC_ROOT = "http://www.starcomics.com/fumetto.aspx?Fumetto=";
    public static final String IMG_URL = "http://www.starcomics.com";
    // Bonelli
    public static final String EDICOLA_INEDITI = "http://www.sergiobonelli.it/sezioni/1025/inediti";
    public static final String EDICOLA_RISTAMPE = "http://www.sergiobonelli.it/sezioni/1016/ristampe";
    public static final String EDICOLA_RACCOLTE = "http://www.sergiobonelli.it/sezioni/1017/raccolte";
    public static final String PROSSIMAMENTE_INEDITI = "http://www.sergiobonelli.it/sezioni/1026/inediti";
    public static final String PROSSIMAMENTE_RISTAMPE = "http://www.sergiobonelli.it/sezioni/1018/ristampe";
    public static final String PROSSIMAMENTE_RACCOLTE = "http://www.sergiobonelli.it/sezioni/1019/raccolte";
    public static final String COVER_PART = "http://www.sergiobonelli.it/";
    // Widget
    public static final String WIDGET_EDITOR = "WIDGET_EDITOR";
    public static final String WIDGET_TITLE = "WIDGET_TITLE";
    public static final String COMIC_ID_FROM_WIDGET = "COMIC_ID_FROM_WIDGET";
    public static final String URI_SCHEME = "CC_WIDGET";
    // Search
    public static final String PREF_SEARCH_QUERY = "search_query";

}
