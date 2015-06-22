package com.coopery.stationfinder;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class MainActivity extends ActionBarActivity {

    TextView tv;
    Button bGetStations;
    Button bGetLocation;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.textView);
        bGetStations = (Button) findViewById(R.id.bGetStations);
        bGetLocation = (Button) findViewById(R.id.bGetLocation);

        // Start background service
        Intent stationDataIntent = new Intent(this, StationDataService.class);
        stationDataIntent.setData(Uri.parse("dataUri"));

        startService(stationDataIntent);
        //

        // Register broadcast receiver with StationDataService broadcasts
        IntentFilter statusIntentFilter = new IntentFilter(StationDataService.BROADCAST_ACTION);
        statusIntentFilter.addDataScheme("http");

        ResponseReceiver responseReceiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(responseReceiver, statusIntentFilter);
        //

//        // Handle location changes
//        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        LocationListener locationListener = new LocationListener() {
//            @Override
//            public void onLocationChanged(Location newLocation) {
//                location = newLocation;
//            }
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {}
//
//            @Override
//            public void onProviderEnabled(String provider) {}
//
//            @Override
//            public void onProviderDisabled(String provider) {}
//        };
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
//
//        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//
//
//        new DownloadStationsTask().execute();





//        bGetStations.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tv.setText("new text" + "\n" + tv.getText());
//
//                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//
//                if (networkInfo != null && networkInfo.isConnected()) {
//                    tv.setText("we good" + "\n" + tv.getText());
//
////                    double dist = 50;
////
////                    double lat = location.getLatitude();
////                    double lon = location.getLongitude();
////
////                    String ns = (lat > 0) ? "N" : "S";
////                    String ew = (lon > 0) ? "E" : "W";
////
////                    lat = Math.abs(lat);
////                    lon = Math.abs(lon);
////
////                    double dlat = (int) lat;
////                    double mlat = ((int) (lat * 60)) % 60;
////                    double slat = (lat * 3600) % 60;
////
////                    double dlon = (int) lon;
////                    double mlon = ((int) (lon * 60)) % 60;
////                    double slon = (lon * 3600) % 60;
////
////                    tv.setText(dlat + " " + mlat + " " + slat + " $ " + dlon + " " + mlon + " " + slon + "\n" + tv.getText());
////
////                    String url = "https://transition.fcc.gov/fcc-bin/fmq?state=&call=&city=&arn=&serv=&vac=&freq=0.0&fre2=107.9&facid=&asrn=&class=&dkt=&"
////                            + "list=4" + "&dist=" + dist + "&dlat2=" + dlat + "&mlat2=" + mlat + "&slat2=" + slat + "&NS=" + ns
////                            + "&dlon2=" + dlon + "&mlon2=" + mlon + "&slon2=" + slon + "&EW=" + ew + "&size=9";
//
////                    Log.d("MainActivity", url);
//
////                    new DownloadStationsTask().execute();
//                } else {
//                    tv.setText("we bad" + "\n" + tv.getText());
//                }
//            }
//        });
//
//        bGetLocation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                tv.setText(location.getLatitude() + " || " + location.getLongitude() + "\n" + tv.getText());
//            }
//        });
    }


    // BroadcastReceiver for receiving status updates from the station data service
    private class ResponseReceiver extends BroadcastReceiver {

        // Prevents instantiation
//        private DownloadStateReceiver() {}

        // Called when BroadcastReceiver gets an Intent it is registered to receive.
        @Override
        public void onReceive(Context context, Intent intent) {
            // Handle intents here

        }
    }


//    // Create a task separate from the main UI thread.
//    private class DownloadStationsTask extends AsyncTask<String, Void, String> {
//        @Override
//        protected String doInBackground(String... params) {
//
//            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//
//            if (networkInfo != null && networkInfo.isConnected()) {
//                double dist = 50;
//
//                double lat = location.getLatitude();
//                double lon = location.getLongitude();
//
//                String ns = (lat > 0) ? "N" : "S";
//                String ew = (lon > 0) ? "E" : "W";
//
//                lat = Math.abs(lat);
//                lon = Math.abs(lon);
//
//                double dlat = (int) lat;
//                double mlat = ((int) (lat * 60)) % 60;
//                double slat = (lat * 3600) % 60;
//
//                double dlon = (int) lon;
//                double mlon = ((int) (lon * 60)) % 60;
//                double slon = (lon * 3600) % 60;
//
//                tv.setText(dlat + " " + mlat + " " + slat + " $ " + dlon + " " + mlon + " " + slon + "\n" + tv.getText());
//
//                String url = "https://transition.fcc.gov/fcc-bin/fmq?state=&call=&city=&arn=&serv=&vac=&freq=0.0&fre2=107.9&facid=&asrn=&class=&dkt=&"
//                        + "list=4" + "&dist=" + dist + "&dlat2=" + dlat + "&mlat2=" + mlat + "&slat2=" + slat + "&NS=" + ns
//                        + "&dlon2=" + dlon + "&mlon2=" + mlon + "&slon2=" + slon + "&EW=" + ew + "&size=9";
//
//                // params comes from the execute() call, params[0] is the url
//                try {
//                    return downloadUrl(url);
//                } catch (Exception e) {
//                    return "Can't find stations. Check internet connection?";
//                }
//            }
//
//            return "Can't find stations. Are you connected to the internet?";
//        }
//
//        // displays results of the AsyncTask
//        @Override
//        protected void onPostExecute(String result) {
//            tv.setText(result + "\n" + tv.getText());
//            Log.d("MainActivity", "The response is " + result);
//        }
//    }


    // Given a URL, creates an HttpUrlConnection, retrieves page as
    // InputStream, returns as a String
    private String downloadUrl(String urlString) throws Exception {
        InputStream is = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("MainActivity", "The response is " + response);
            is = conn.getInputStream();
            Log.d("MainActivity", "here");
//            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
//            Log.d("MainActivity", "here");
//            XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//span[position()>2]//a[1]");
//            Log.d("MainActivity", "here");
//            String result = (String) xpath.evaluate(doc, XPathConstants.STRING);
//            return result;

            // Convert InputStream to String
            String contentString = readIt(is);
            String delims = "[\\|\n]";
            String tokens[] = contentString.split(delims);

            String result = "";
            for (int i = 2; i < tokens.length; i += 39) {
                result += tokens[i].trim() + "|";
                Log.d("MainActivity1", i + " : " + tokens[i]);
            }


            return result;
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Turns an InputStream into a String
    private String readIt(InputStream is) throws Exception {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "emptyyy";
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
