package com.coopery.stationfinder;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Does work in the background
 */
public class StationDataService extends IntentService implements LocationListener {

    public static final String TAG = "StationDataService";

    // Time defined to be a "significant difference" between location fixes
    private static final int SIGNIFICANT_LOC_TIME = 2 * 60 * 1000;

    // Timeout length for location listening
    private static final int LOCATION_TIMEOUT = 30 * 1000;

    public static PowerManager.WakeLock wakeLock;

    private LocationManager locManager;
    private Location location;

    private Intent incomingIntent;

    // Runs right before onHandleIntent()
    public StationDataService() {
        super("StationDataService");
    }

    // Service has been started by an alarm, time to do things
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service started");
        incomingIntent = intent;

        startLocationListening();
    }

    private void startLocationListening() {
        // Start listening for location updates
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String bestProvider = locManager.getBestProvider(new Criteria(), true);
        locManager.requestLocationUpdates(bestProvider, 0, 0, this);

        // Check if the device's last known location is better than our app's last saved location
        // (it really should be, cause that was like 15 minutes ago)
        List<String> providers = locManager.getProviders(true);
        for (String provider : providers) {
            Location newLocation = locManager.getLastKnownLocation(provider);

            if (isBetterLocation(newLocation))
                location = newLocation;
        }

        // If the last known location is recent enough, then we can just use that (5 min)
        if (Build.VERSION.SDK_INT >= 17) {
            // TODO fix this madness
            if (BigInteger.valueOf(SystemClock.elapsedRealtimeNanos()).subtract(BigInteger.valueOf(location.getElapsedRealtimeNanos())).compareTo(BigInteger.valueOf(10).pow(9).multiply(BigInteger.valueOf(5 * 60))) < 0) {
                Log.d(TAG, "Last known location good enough: proceeding with last location.");
                gotGoodLocation();
                return;
            }
        }

        // Set timeout for location listening
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Location listening timeout: proceeding with bad location.");
                gotGoodLocation();
            }
        }, LOCATION_TIMEOUT);
    }

    private void stopLocationListening() {
        locManager.removeUpdates(this);
    }

    private void gotGoodLocation() {
        // We got a good enough location, so stop listening
        stopLocationListening();

        ArrayList<Station> stations = ApiHandler.getStations(this, location, 75);
        try {
            ApiHandler.findFormats(this, stations);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        DataModel.setStations(this, stations);

        for (int i = 0; i < stations.size(); ++i) {
            if (stations.get(i).getFormat().equals("")) {
                stations.remove(i--);
            }
        }

        Intent intent = new Intent("com.coopery.stationfinder.newdata");
        sendBroadcast(intent);

        // This service is completely done now, we can release our wakelock
        Log.d(TAG, "Service completed.");
        AlarmReceiver.completeWakefulIntent(incomingIntent);
    }



    // Determines whether one Location reading is better than the current Location fix
    protected boolean isBetterLocation(Location newLocation) {
        if (location == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = newLocation.getTime() - location.getTime();
        boolean isSignificantlyNewer = timeDelta > SIGNIFICANT_LOC_TIME;
        boolean isSignificantlyOlder = timeDelta < -SIGNIFICANT_LOC_TIME;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - location.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), location.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    // Checks whether two providers are the same
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }



    // Required method overrides to be a LocationListener
    @Override
    public void onLocationChanged(Location location) {
        this.location = location;

        // If accuracy is within 1 mi (1609 m)
        if (location.getAccuracy() <= 1609) {
            Log.d(TAG, "Found good location from listening: proceeding with new location.");
            gotGoodLocation();
        }
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}
}