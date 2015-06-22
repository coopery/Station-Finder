package com.coopery.stationfinder;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Does work in the background
 */
public class StationDataService extends IntentService {

    public static final String BROADCAST_ACTION = "com.coopery.stationfinder.BROADCAST";
    public static final String EXTENDED_DATA_STATUS = "com.coopery.stationfinder.STATUS";

    private Location location;

    public StationDataService() {
        super("StationDataService");

        // Start listening to location changes
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location newLocation) {
                location = newLocation;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        //

        // Get the last known location
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    // Broadcast stuff
    @Override
    protected void onHandleIntent(Intent intent) {
        // Get data from incoming intent
        String dataString = intent.getDataString();

        // Do work based on data string
        double status = 0;


        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            double dist = 50;

            double lat = location.getLatitude();
            double lon = location.getLongitude();

            String ns = (lat > 0) ? "N" : "S";
            String ew = (lon > 0) ? "E" : "W";

            lat = Math.abs(lat);
            lon = Math.abs(lon);

            double dlat = (int) lat;
            double mlat = ((int) (lat * 60)) % 60;
            double slat = (lat * 3600) % 60;

            double dlon = (int) lon;
            double mlon = ((int) (lon * 60)) % 60;
            double slon = (lon * 3600) % 60;

            tv.setText(dlat + " " + mlat + " " + slat + " $ " + dlon + " " + mlon + " " + slon + "\n" + tv.getText());

            String url = "https://transition.fcc.gov/fcc-bin/fmq?state=&call=&city=&arn=&serv=&vac=&freq=0.0&fre2=107.9&facid=&asrn=&class=&dkt=&"
                    + "list=4" + "&dist=" + dist + "&dlat2=" + dlat + "&mlat2=" + mlat + "&slat2=" + slat + "&NS=" + ns
                    + "&dlon2=" + dlon + "&mlon2=" + mlon + "&slon2=" + slon + "&EW=" + ew + "&size=9";

            // params comes from the execute() call, params[0] is the url
            try {
                return downloadUrl(url);
            } catch (Exception e) {
                return "Can't find stations. Check internet connection?";
            }
        }

        return "Can't find stations. Are you connected to the internet?";



        Intent broadcastIntent = new Intent(BROADCAST_ACTION).putExtra(EXTENDED_DATA_STATUS, status);

        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }
}