package org.checklist.comics.comicschecklist.util

/**
 * Class which contains constants.
 */
object Constants {

    /* The fragment argument representing the section number for this fragment. */
    const val ARG_EDITOR = "editor"
    const val ARG_COMIC_ID = "comic_id"

    /* Data sync preferences */
    const val PREF_SYNC_FREQUENCY = "sync_frequency"
    const val PREF_DELETE_FREQUENCY = "delete_frequency"
    const val PREF_AVAILABLE_EDITORS = "available_editors"
    const val PREF_DELETE_CONTENT = "delete_content"
    const val PREF_LAST_SYNC = "last_sync"

    /* Preference last scan */
    const val PREF_PANINI_LAST_SCAN = "panini_lastscan"
    const val PREF_STAR_LAST_SCAN = "star_lastscan"
    const val PREF_BONELLI_LAST_SCAN = "bonelli_lastscan"
    const val PREF_RW_LAST_SCAN = "rw_lastscan"

    /* Service & Notification */
    const val MANUAL_SEARCH = "manual_search"
    const val CREATE_DATABASE = "create_database"

    const val RESULT_START = 0
    const val RESULT_CANCELED = 1
    const val RESULT_FINISHED = 2
    const val RESULT_DESTROYED = 3
    const val RESULT_NOT_CONNECTED = 4
    const val RESULT_EDITOR_FINISHED = 5

    const val NOTIFICATION_RESULT = "result"
    const val NOTIFICATION_EDITOR = "editor"
    const val NOTIFICATION = "org.checklist.comics.comicschecklist.service.receiver"
    const val PREF_SEARCH_NOTIFICATION = "notifications_new_message"
    const val PREF_FAVORITE_NOTIFICATION = "notifications_favorite_available"

    /* Widget & shortcut */
    const val WIDGET_EDITOR = "WIDGET_EDITOR"
    const val WIDGET_TITLE = "WIDGET_TITLE"
    const val COMIC_ID_FROM_WIDGET = "COMIC_ID_FROM_WIDGET"
    const val ACTION_COMIC_WIDGET = "comic_widget"
    const val ACTION_WIDGET_ADD = "action_add_comic"
    const val ACTION_WIDGET_OPEN_APP = "action_open_app"
    const val ACTION_ADD_COMIC = "org.checklist.comics.comicschecklist.ADD_COMIC"
    const val ACTION_SEARCH_STORE = "org.checklist.comics.comicschecklist.SEARCH_STORE"

    /* Enum of sections available on menu */
    enum class Sections constructor(val code: Int, val sectionName: String, val title: String) {
        FAVORITE(0, "preferiti", "Lista preferiti"),
        CART(1, "da comprare", "Da comprare"),
        PANINI(2, "paninicomics", "Panini Comics"),
        STAR(3, "star", "Star Comics"),
        BONELLI(4, "bonelli", "Sergio Bonelli"),
        RW(5, "rw", "RW Edizioni"),
        SETTINGS(6, "settings", "Impostazioni"),
        GOOGLE(7, "google plus", "Segui su Google+"),
        GUIDA(8, "guida", "Guida"),
        INFO(9, "info", "Info");

        companion object {

            fun fromCode(findValue: Int): Sections = Sections.values().first { it.code == findValue }
            fun fromName(findValue: String): Sections = Sections.values().first { it.sectionName == findValue }
            fun fromTitle(findValue: String): Sections = Sections.values().first { it.title == findValue }

        }
    }
}
