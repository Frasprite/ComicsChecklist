package org.checklist.comics.comicschecklist.util;

import java.util.HashMap;

/**
 * Created by Francesco Bevilacqua on 25/10/2014.
 * This code is part of ComicsChecklist project.
 */
public class Constants {

    /* The fragment argument representing the section number for this fragment. */
    public static final String ARG_SECTION_NUMBER = "section_number";
    /**
     * Flag used to show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    public static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    public static final String PREF_USER_LEARNED_SLIDING_UP = "sliding_up_learned";

    /* AppRater */
    public static final String PREF_USER_DONT_RATE = "dont_show_again";
    public static final String PREF_APP_RATER = "app_rater";
    public static final String PREF_LAUNCH_COUNT = "launch_count";
    public static final String PREF_DATE_FIRST_LAUNCH = "date_first_launch";
    public final static int DAYS_UNTIL_PROMPT = 7;
    public final static int LAUNCHES_UNTIL_PROMPT = 7;

    /* Enum of editors and list created by user */
    public enum Editors {
        FAVORITE(0, "preferiti", "Lista preferiti"),
        CART    (1, "da comprare", "Da comprare"),
        MARVEL  (2, "marvelitalia", "Marvel"),
        PANINI  (3, "paninicomics", "Panini Comics"),
        PLANET  (4, "planetmanga", "Planet Manga"),
        STAR    (5, "star", "Star Comics"),
        BONELLI (6, "bonelli", "Sergio Bonelli"),
        RW      (7, "rw", "RW Edizioni");

        private static final HashMap<Integer, Editors> map;
        private static final HashMap<String, Editors> titleMap;

        static {
            map = new HashMap<>();
            titleMap = new HashMap<>();
            for (Editors editor : Editors.values()) {
                map.put(editor.code, editor);
                titleMap.put(editor.title, editor);
            }
        }

        private final int code;
        private final String name;
        private final String title;

        Editors(int code, String name, String title) {
            this.code = code;
            this.name = name;
            this.title = title;
        }

        public static Editors getEditor(int position) {
            return map.get(position);
        }

        public static String getName(int position) {
            return map.get(position).name;
        }

        public static String getName(Editors editor) {
            return editor.name;
        }

        public static String getTitle(int position) {
            return map.get(position).title;
        }

        public static String getTitle(Editors editor) {
            return editor.title;
        }

        public static String getName(String title) {
            return titleMap.get(title).name;
        }
    }

    /* Preference last scan */
    public static final String PREF_MARVEL_LAST_SCAN = "marvel_lastscan";
    public static final String PREF_PANINI_LAST_SCAN = "panini_lastscan";
    public static final String PREF_PLANET_LAST_SCAN = "planet_lastscan";
    public static final String PREF_STAR_LAST_SCAN = "star_lastscan";
    public static final String PREF_BONELLI_LAST_SCAN = "bonelli_lastscan";
    public static final String PREF_RW_LAST_SCAN = "rw_lastscan";

    /* Service & Notification */
    public static final String MANUAL_SEARCH = "manual_search";
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

    /* Widget */
    public static final String WIDGET_EDITOR = "WIDGET_EDITOR";
    public static final String WIDGET_TITLE = "WIDGET_TITLE";
    public static final String COMIC_ID_FROM_WIDGET = "COMIC_ID_FROM_WIDGET";
    public static final String ACTION_COMIC_WIDGET = "comic_widget";

    /* Search */
    public static final String PREF_SEARCH_QUERY = "search_query";

    /* Dialog */
    public static final int DIALOG_GUIDE = 0;
    public static final int DIALOG_INFO = 1;
    public static final int DIALOG_ADD_COMIC = 2;
    public static final int DIALOG_RATE = 3;
    public static final int DIALOG_RESULT_LIST = 4;

    /**
     * Constants for parser class.
     */
    // Panini, Planet Manga & Marvel
    public static final String FIRSTPANINI = "http://www.paninicomics.it/web/guest/";
    public static final String SECONDPANINI = "/checklist?year=";
    public static final String THIRDPANINI = "&weekOfYear=";
    public static final String URLPANINI = "http://www.paninicomics.it";
    // RWEdizioni ex.: http://www.rwedizioni.it/news/uscite-9-gennaio-2016/
    public static final String FIRSTRW = "http://www.rwedizioni.it/news/uscite";
    public static final String MIDDLERW = "-";
    public static final String ENDRW = "/";
    public static final String MEDIARW = "http://www.rwedizioni.it/media/";
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
    public static final String MAIN_URL = "http://www.sergiobonelli.it/";
}
