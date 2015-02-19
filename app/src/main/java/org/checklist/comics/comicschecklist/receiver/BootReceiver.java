package org.checklist.comics.comicschecklist.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by Francesco Bevilacqua on 18/02/2015.
 * This code is part of Comics Checklist project.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent mIntentReceiver = new Intent(context, AlarmReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, mIntentReceiver, 0);

            // Set the alarm to start at 10:00 a.m.
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 10);

            // Specify a non-precise custom interval, in this case every days.
            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }

    // Enabling receiver
    /**ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
     PackageManager pm = context.getPackageManager();

     pm.setComponentEnabledSetting(receiver,
     PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
     PackageManager.DONT_KILL_APP);*/

    // Disabling receiver
    /**ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
     PackageManager pm = context.getPackageManager();

     pm.setComponentEnabledSetting(receiver,
     PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
     PackageManager.DONT_KILL_APP);*/
}
