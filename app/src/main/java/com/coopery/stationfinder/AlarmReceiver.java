package com.coopery.stationfinder;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.List;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    public static final String TAG = "AlarmReceiver";

    // Runs right before onReceive()
    public AlarmReceiver() {}

    // Alarm triggered, time to start the service
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm triggered");

        Intent startService = new Intent(context, StationDataService.class);
        startWakefulService(context, startService);
    }
}
