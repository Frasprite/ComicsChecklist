package org.checklist.comics.comicschecklist.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.checklist.comics.comicschecklist.service.AlarmService;
import org.checklist.comics.comicschecklist.util.CCLogger;

/**
 * Class used to fire an alarm when a comic is out.
 * TODO  replace deprecated class
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // Start the service, keeping the device awake while the service is
        // launching. This is the Intent to deliver to the service.
        CCLogger.d(TAG, "onReceive");
        Intent service = new Intent(context, AlarmService.class);
        startWakefulService(context, service);
    }
}
