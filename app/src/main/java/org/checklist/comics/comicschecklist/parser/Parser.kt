package org.checklist.comics.comicschecklist.parser

import org.checklist.comics.comicschecklist.database.entity.ComicEntity
import org.checklist.comics.comicschecklist.log.CCLogger

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

/**
 * Abstract class which give instruction how sub-class should parse data from HTML pages.
 */
abstract class Parser {

    abstract fun initParser(): ArrayList<ComicEntity>
    abstract fun parseUrl(url: String): ArrayList<ComicEntity>
    abstract fun searchTitle(`object`: Any): String?
    abstract fun searchReleaseDate(`object`: Any): String?
    abstract fun searchDescription(`object`: Any): String?
    abstract fun searchCover(`object`: Any): String?
    abstract fun searchFeature(`object`: Any): String?
    abstract fun searchPrice(`object`: Any): String?

    /**
     * Elaborate a date, in [Date] format, from given date which is in [String] format.
     * @param releaseDate the date to elaborate
     * @return the date in another format
     */
    internal fun elaborateDate(releaseDate: String): Date {
        CCLogger.v(TAG, "elaborateDate - start")
        var date: Date? = null
        try {
            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(releaseDate)
        } catch (e: ParseException) {
            CCLogger.w(TAG, "elaborateDate - Error while elaborating Date from " + releaseDate + " " + e.toString())
        } finally {
            if (date == null) {
                date = Date()
                date.time = System.currentTimeMillis()
                CCLogger.d(TAG, "elaborateDate - Setting date from current time")
            }
        }
        CCLogger.v(TAG, "elaborateDate - end - " + date.toString())
        return date
    }

    companion object {

        private val TAG = Parser::class.java.simpleName
    }

}
