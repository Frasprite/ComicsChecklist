package org.checklist.comics.comicschecklist.log;

public class ParserLog {

    private static final String TAG = ParserLog.class.getSimpleName();

    public static void printReport() {
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
                "BONELLI URL parsed correctly = " + parsedBonelliURL);

        resetData();
    }

    private static void resetData() {
        errorOnParsingComic = 0;
        wrongRWURL = 0;
        wrongRWElements = 0;
        parsedRWURL = 0;
        wrongStarURL = 0;
        wrongStarElements = 0;
        parsedStarURL = 0;
        wrongPaniniURL = 0;
        wrongPaniniElements = 0;
        parsedPaniniURL = 0;
        wrongBonelliURL = 0;
        wrongBonelliElements = 0;
        parsedBonelliURL = 0;
    }

    /* */

    private static int errorOnParsingComic = 0;

    public static void increaseErrorOnParsingComic() {
        errorOnParsingComic++;
    }

    /* */

    private static int wrongRWURL = 0;
    private static int wrongRWElements = 0;
    private static int parsedRWURL = 0;

    public static void increaseWrongRWURL() {
        wrongRWURL++;
    }

    public static void increaseWrongRWElements() {
        wrongRWElements++;
    }

    public static void increaseParsedRWURL() {
        parsedRWURL++;
    }

    /* */

    private static int wrongStarURL = 0;
    private static int wrongStarElements = 0;
    private static int parsedStarURL = 0;

    public static void increaseWrongStarURL() {
        wrongStarURL++;
    }

    public static void increaseWrongStarElements() {
        wrongStarElements++;
    }

    public static void increaseParsedStarURL() {
        parsedStarURL++;
    }

    /* */

    private static int wrongPaniniURL = 0;
    private static int wrongPaniniElements = 0;
    private static int parsedPaniniURL = 0;

    public static void increaseWrongPaniniURL() {
        wrongPaniniURL++;
    }

    public static void increaseWrongPaniniElements() {
        wrongPaniniElements++;
    }

    public static void increaseParsedPaniniURL() {
        parsedPaniniURL++;
    }

    /* */

    private static int wrongBonelliURL = 0;
    private static int wrongBonelliElements = 0;
    private static int parsedBonelliURL = 0;

    public static void increaseWrongBonelliURL() {
        wrongBonelliURL++;
    }

    public static void increaseWrongBonelliElements() {
        wrongBonelliElements++;
    }

    public static void increaseParsedBonelliURL() {
        parsedBonelliURL++;
    }
}
