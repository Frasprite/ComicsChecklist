package org.checklist.comics.comicschecklist.log

object ParserLog {

    private val TAG = ParserLog::class.java.simpleName

    private var errorOnParsingComic = 0
    private var wrongRWURL = 0
    private var wrongRWElements = 0
    private var parsedRWURL = 0
    private var wrongStarURL = 0
    private var wrongStarElements = 0
    private var parsedStarURL = 0
    private var wrongPaniniURL = 0
    private var wrongPaniniElements = 0
    private var parsedPaniniURL = 0
    private var wrongBonelliURL = 0
    private var wrongBonelliElements = 0
    private var parsedBonelliURL = 0

    fun printReport() {
        CCLogger.v(TAG, "Report:\n" +
                "Error while parsing comic = " + errorOnParsingComic + "\n" +
                "Wrong RW URL = " + wrongRWURL + "\n" +
                "Wrong RW list = " + wrongRWElements + "\n" +
                "RW URL parsed correctly = " + parsedRWURL + "\n" +
                "Wrong STAR COMICS URL = " + wrongStarURL + "\n" +
                "Wrong STAR COMICS list = " + wrongStarElements + "\n" +
                "STAR COMICS URL parsed correctly = " + parsedStarURL + "\n" +
                "Wrong PANINI URL = " + wrongPaniniURL + "\n" +
                "Wrong PANINI list = " + wrongPaniniElements + "\n" +
                "PANINI URL parsed correctly = " + parsedPaniniURL + "\n" +
                "Wrong BONELLI URL = " + wrongBonelliURL + "\n" +
                "Wrong BONELLI list = " + wrongBonelliElements + "\n" +
                "BONELLI URL parsed correctly = " + parsedBonelliURL)

        resetData()
    }

    private fun resetData() {
        errorOnParsingComic = 0
        wrongRWURL = 0
        wrongRWElements = 0
        parsedRWURL = 0
        wrongStarURL = 0
        wrongStarElements = 0
        parsedStarURL = 0
        wrongPaniniURL = 0
        wrongPaniniElements = 0
        parsedPaniniURL = 0
        wrongBonelliURL = 0
        wrongBonelliElements = 0
        parsedBonelliURL = 0
    }

    fun increaseErrorOnParsingComic() {
        errorOnParsingComic++
    }

    fun increaseWrongRWURL() {
        wrongRWURL++
    }

    fun increaseWrongRWElements() {
        wrongRWElements++
    }

    fun increaseParsedRWURL() {
        parsedRWURL++
    }

    fun increaseWrongStarURL() {
        wrongStarURL++
    }

    fun increaseWrongStarElements() {
        wrongStarElements++
    }

    fun increaseParsedStarURL() {
        parsedStarURL++
    }

    fun increaseWrongPaniniURL() {
        wrongPaniniURL++
    }

    fun increaseWrongPaniniElements() {
        wrongPaniniElements++
    }

    fun increaseParsedPaniniURL() {
        parsedPaniniURL++
    }

    fun increaseWrongBonelliURL() {
        wrongBonelliURL++
    }

    fun increaseWrongBonelliElements() {
        wrongBonelliElements++
    }

    fun increaseParsedBonelliURL() {
        parsedBonelliURL++
    }
}
