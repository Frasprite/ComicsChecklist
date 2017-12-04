package org.checklist.comics.comicschecklist.util;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Class used to centralizes date method.
 */
public class DateCreator {

    private static final String TAG = DateCreator.class.getSimpleName();

    /**
     * Elaborate a date, in {@link Date} format, from given date which is in {@link String} format.
     * @param releaseDate the date to elaborate
     * @return the date in another format
     */
    public static Date elaborateDate(String releaseDate) {
        CCLogger.v(TAG, "elaborateDate - start");
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(releaseDate);
        } catch (ParseException e) {
            CCLogger.w(TAG, "elaborateDate - Error while elaborating Date from " + releaseDate + " " + e.toString());
        } finally {
            if (date == null) {
                date = new Date();
                date.setTime(System.currentTimeMillis());
                CCLogger.d(TAG, "elaborateDate - Setting date from current time");
            }
        }
        CCLogger.v(TAG, "elaborateDate - end - " + date.toString());
        return date;
    }

    /**
     * Elaborate a date from given day, month (which is from 0 to 11) and year.
     * @param year the year
     * @param month the month (which is from 0 to 11)
     * @param day the day of month
     * @return a date in following format "dd/MM/yyyy"
     */
    public static String elaborateDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * Method used to create an human readable date ("dd/MM/yyyy").
     * @return the date in a good human format
     */
    public static String elaborateHumanDate(String releaseDate) {
        CCLogger.v(TAG, "elaborateHumanDate - start with " + releaseDate);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        calendar.setTime(DateCreator.elaborateDate(releaseDate));
        String resultDate = simpleDateFormat.format(calendar.getTime());
        CCLogger.v(TAG, "elaborateHumanDate - end with " + resultDate);
        return resultDate;
    }

    /**
     * This method return the current date in "dd/MM/yyyy" format.
     * @return current date
     */
    public static String getTodayString() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * Get today calendar with hour at 10:00 a.m.
     * @return current date with 10:00 a.m.
     */
    public static Calendar getAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        return calendar;
    }

    /**
     * Return the day of month in {@link Integer} format.
     * @return day of month
     */
    public static int getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        CCLogger.d(TAG, "getCurrentDay - day is " + day);
        return day;
    }

    /**
     * Return the current month in {@link Integer} format.
     * @return current month (from 0 to 11)
     */
    public static int getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        CCLogger.d(TAG, "getCurrentMonth - day is " + month);
        return month;
    }

    /**
     * Return the current month in a human readable way.
     * @return the current month
     */
    public static String getCurrentReadableMonth() {
        int month = DateCreator.getCurrentMonth();
        String monthString = DateFormatSymbols.getInstance(Locale.ITALIAN).getMonths()[month];
        CCLogger.d(TAG, "getCurrentReadableMonth - day is " + monthString);
        return monthString;
    }

    /**
     * Return the current year in {@link Integer} format.
     * @return the current year
     */
    public static int getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        CCLogger.d(TAG, "getCurrentYear - day is " + year);
        return year;
    }

    /**
     * Get given time in milliseconds.
     * @param date the time to elaborate in milliseconds in {@link String} format
     * @return the time in millis
     */
    public static long getTimeInMillis(String date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateCreator.elaborateDate(date));
        long millis = calendar.getTimeInMillis();
        CCLogger.d(TAG, "getTimeInMillis - result is " + millis);
        return millis;
    }

    /**
     * Elaborate passed day by given frequency.
     * @param frequency the day to deduct from now
     * @return the date passed by
     */
    public static Date getPastDay(int frequency) {
        Calendar calendar = Calendar.getInstance();
        int x = frequency * -1;
        calendar.add(Calendar.DAY_OF_YEAR, x);
        Date pastDay = calendar.getTime();
        CCLogger.d(TAG, "getPastDay - result is " + pastDay.toString());
        return pastDay;
    }

    /**
     * Return the difference in millis between 2 dates.
     * @param today the current day
     * @param dateStart the passed day
     * @return the difference in milliseconds
     */
    public static long getDifferenceInMillis(String today, String dateStart) {
        Date d1 = DateCreator.elaborateDate(dateStart);
        Date d2 = DateCreator.elaborateDate(today);
        long difference = d2.getTime() - d1.getTime();
        CCLogger.v(TAG, "getDifferenceInMillis - result is " + difference);
        return difference;
    }
}
