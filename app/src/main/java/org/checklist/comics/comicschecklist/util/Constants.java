package org.checklist.comics.comicschecklist.util;

import android.util.SparseArray;

import java.util.HashMap;

/**
 * Class which contains constants.
 */
public class Constants {

    /* The fragment argument representing the section number for this fragment. */
    public static final String ARG_EDITOR = "editor";
    public static final String ARG_COMIC_ID = "comic_id";
    public static final String ARG_SAVED_COMIC_ID = "comic_id";
    /**
     * Flag used to show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    public static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /* AppRater */
    static final String PREF_USER_DONT_RATE = "dont_show_again";
    static final String PREF_APP_RATER = "app_rater";
    static final String PREF_LAUNCH_COUNT = "launch_count";
    static final String PREF_DATE_FIRST_LAUNCH = "date_first_launch";
    final static int DAYS_UNTIL_PROMPT = 7;
    final static int LAUNCHES_UNTIL_PROMPT = 7;

    /* Enum of sections available on menu */
    public enum Sections {
        FAVORITE(0, "preferiti", "Lista preferiti"),
        CART    (1, "da comprare", "Da comprare"),
        PANINI  (2, "paninicomics", "Panini Comics"),
        STAR    (3, "star", "Star Comics"),
        BONELLI (4, "bonelli", "Sergio Bonelli"),
        RW      (5, "rw", "RW Edizioni"),
        SETTINGS(6, "settings", "Impostazioni"),
        GOOGLE  (7, "google plus", "Segui su Google+"),
        GUIDA   (8, "guida", "Guida"),
        INFO    (9, "info", "Info");

        private static final SparseArray<Sections> map;
        private static final HashMap<String, Sections> nameMap;
        private static final HashMap<String, Sections> titleMap;

        static {
            map = new SparseArray<>();
            nameMap = new HashMap<>();
            titleMap = new HashMap<>();
            for (Sections editor : Sections.values()) {
                map.put(editor.code, editor);
                nameMap.put(editor.name, editor);
                titleMap.put(editor.title, editor);
            }
        }

        private final int code;
        private final String name;
        private final String title;

        Sections(int code, String name, String title) {
            this.code = code;
            this.name = name;
            this.title = title;
        }

        public static Sections getEditor(int position) {
            return map.get(position);
        }

        public static Sections getEditorFromName(String name) {
            return nameMap.get(name);
        }

        public static Sections getEditorFromTitle(String title) {
            return titleMap.get(title);
        }

        public static String getName(Sections editor) {
            return editor.name;
        }

        public static String getTitle(Sections editor) {
            return editor.title;
        }

        public static String getName(String title) {
            return titleMap.get(title).name;
        }

        public int getCode() {
            return code;
        }

        public String getTitle() {
            return title;
        }

        public String getName() {
            return name;
        }
    }

    /* Data sync preferences */
    public static final String PREF_SYNC_FREQUENCY = "sync_frequency";
    public static final String PREF_DELETE_FREQUENCY = "delete_frequency";
    public static final String PREF_AVAILABLE_EDITORS = "available_editors";
    public static final String PREF_DELETE_CONTENT = "delete_content";
    public static final String PREF_LAST_SYNC = "last_sync";

    /* Preference last scan */
    public static final String PREF_PANINI_LAST_SCAN = "panini_lastscan";
    public static final String PREF_STAR_LAST_SCAN = "star_lastscan";
    public static final String PREF_BONELLI_LAST_SCAN = "bonelli_lastscan";
    public static final String PREF_RW_LAST_SCAN = "rw_lastscan";

    /* Service & Notification */
    public static final String MANUAL_SEARCH = "manual_search";
    public static final String CREATE_DATABASE = "create_database";

    public enum SearchResults {
        RESULT_START,
        RESULT_CANCELED,
        RESULT_FINISHED,
        RESULT_DESTROYED,
        RESULT_NOT_CONNECTED,
        RESULT_EDITOR_FINISHED
    }

    public static final String NOTIFICATION_RESULT = "result";
    public static final String NOTIFICATION_EDITOR = "editor";
    public static final String NOTIFICATION = "org.checklist.comics.comicschecklist.service.receiver";
    public static final int NOTIFICATION_ID = 299;
    public static final String PREF_SEARCH_NOTIFICATION = "notifications_new_message";
    public static final String PREF_FAVORITE_NOTIFICATION = "notifications_favorite_available";

    /* Widget & shortcut */
    public static final String WIDGET_EDITOR = "WIDGET_EDITOR";
    public static final String WIDGET_TITLE = "WIDGET_TITLE";
    public static final String COMIC_ID_FROM_WIDGET = "COMIC_ID_FROM_WIDGET";
    public static final String ACTION_COMIC_WIDGET = "comic_widget";
    public static final String ACTION_WIDGET_ADD = "action_add_comic";
    public static final String ACTION_WIDGET_OPEN_APP = "action_open_app";
    public static final String ACTION_ADD_COMIC = "org.checklist.comics.comicschecklist.ADD_COMIC";
    public static final String ACTION_SEARCH_STORE = "org.checklist.comics.comicschecklist.SEARCH_STORE";

    /* Constants for old database migration */
    public static final String URL_PANINI = "http://www.paninicomics.it";
    public static final String URL_RW = "http://www.rwedizioni.it";
    public static final String URL_STAR = "https://www.starcomics.com";
    public static final String URL_BONELLI = "http://www.sergiobonelli.it/";
}
