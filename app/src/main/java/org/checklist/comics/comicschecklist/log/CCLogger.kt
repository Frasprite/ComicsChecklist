package org.checklist.comics.comicschecklist.log

import android.util.Log

/**
 * Support class used to log info.
 * TODO migrate to LoggerDelegate
 */
object CCLogger {

    private val DEBUG = false

    fun v(tag: String, msg: String) {
        if (DEBUG) {
            Log.v(tag, msg)
        }
    }

    fun d(tag: String, msg: String) {
        if (DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (DEBUG) {
            Log.i(tag, msg)
        }
    }

    fun w(tag: String, msg: String) {
        if (DEBUG) {
            Log.w(tag, msg)
        }
    }

    fun w(tag: String, msg: String, exception: Exception) {
        if (DEBUG) {
            Log.w(tag, msg, exception)
        }
    }

    fun e(tag: String, msg: String) {
        if (DEBUG) {
            Log.e(tag, msg)
        }
    }
}

