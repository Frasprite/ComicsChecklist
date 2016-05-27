package org.checklist.comics.comicschecklist.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateCreator {

    private static final String TAG = DateCreator.class.getSimpleName();

    public static Date elaborateDate(String releaseDate) {
        Log.d(TAG, "elaborateDate - start");
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(releaseDate);
        } catch (ParseException e) {
            Log.w(TAG, "Error while elaborating Date from " + releaseDate + " " + e.toString());
        } finally {
            if (date == null) {
                date = new Date();
                date.setTime(System.currentTimeMillis());
                Log.d(TAG, "Setting date from current time");
            }
        }
        Log.v(TAG, "elaborateDate - end - " + date.toString());
        return date;
    }

}
