package com.coopery.stationfinder;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;

public class ApiHandler {

    private static final String TAG = "ApiHandler";

    // Get a list of radio stations within a 'radius' of 'location'
    // Returns null if it can't get them for some reason
    public static ArrayList<Station> getStations(Context context, Location location, double radius) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        // Convert Decimal coordinates to DMS coordinates
        String ns = (lat > 0) ? "N" : "S";      // north or south
        String ew = (lon > 0) ? "E" : "W";      // east or west

        lat = Math.abs(lat);                    // don't need negatives in DMS
        lon = Math.abs(lon);

        double dlat = (int) lat;                // degrees latitude
        double mlat = ((int) (lat * 60)) % 60;  // minutes latitude
        double slat = (lat * 3600) % 60;        // seconds latitude

        double dlon = (int) lon;                // degrees longitude
        double mlon = ((int) (lon * 60)) % 60;  // minutes longitude
        double slon = (lon * 3600) % 60;        // seconds longitude

        // Form the relevant URL to get a response from the FCC database
        String url = "https://transition.fcc.gov/fcc-bin/fmq?state=&call=&city=&arn=&serv=&vac=&freq=0.0&fre2=107.9&facid=&asrn=&class=&dkt=&"
                + "list=4" + "&dist=" + radius + "&dlat2=" + dlat + "&mlat2=" + mlat + "&slat2=" + slat + "&NS=" + ns
                + "&dlon2=" + dlon + "&mlon2=" + mlon + "&slon2=" + slon + "&EW=" + ew + "&size=9";

        // Try to get a response from the FCC
        try {
            String fccResponse = getResponse(context, url);
            return parseFCCData(fccResponse);
        }
        catch (IOException e) {
            Log.d(TAG, "Connection messed up somehow");
            e.printStackTrace();
            return null;
        }
    }

    private static ArrayList<Station> parseFCCData(String fccResponse) {
        ArrayList<Station> stations = new ArrayList<Station>();

        String delims = "[\\|\n]";
        String tokens[] = fccResponse.split(delims);

        // for each station
        for (int i = 2; i < tokens.length; i += 39) {
            Station station = new Station();

            // Check service designation to make sure it's an FM Full Service station
            if (!tokens[i + 2].trim().equals("FM"))
                continue;

            // for each data point offset in the station
            for (int j = 0; j < 39; ++j) {
                int index = i + j;

                switch (j) {
                    // Callsign
                    case 0:
                        String callSign = tokens[index].trim();
//                        if (callSign.endsWith("-FM")) {
//                            callSign = callSign.substring(0, callSign.length() - 3);
//                        }
                        if (callSign.contains("-")) {
                            callSign = callSign.split("-")[0];
                        }

                        station.setCallSign(callSign);
                        break;
                    // Frequency
                    case 1:
                        station.setFrequency(Double.parseDouble(tokens[index].split("\\s", 2)[0]));
                        break;
                    // North-south
                    case 18:
                        station.setNs(tokens[index].trim());
                        break;
                    // Degrees latitude
                    case 19:
                        station.setDlat(Double.parseDouble(tokens[index].trim()));
                        break;
                    // Minutes longitude
                    case 20:
                        station.setMlat(Double.parseDouble(tokens[index].trim()));
                        break;
                    // Seconds longitude
                    case 21:
                        station.setSlat(Double.parseDouble(tokens[index].trim()));
                        break;
                    // East-west
                    case 22:
                        station.setEw(tokens[index].trim());
                        break;
                    // Degrees longitude
                    case 23:
                        station.setDlon(Double.parseDouble(tokens[index].trim()));
                        break;
                    // Minutes longitude
                    case 24:
                        station.setMlon(Double.parseDouble(tokens[index].trim()));
                        break;
                    // Seconds longitude
                    case 25:
                        station.setSlon(Double.parseDouble(tokens[index].trim()));
                        break;
                }
            }

            stations.add(station);
        }

        return stations;
    }




    public static void findFormats(Context context, ArrayList<Station> stations) {
        Log.d(TAG, "Downloading station formats");

        for (Station station : stations) {
            try {
                Log.d(TAG, "Asking for: "  + station.getCallSign());

                URL url = new URL("http://www1.arbitron.com/sip/displaySip.do");

                ContentValues formData = new ContentValues();
                formData.put("surveyID", "SU15");
                formData.put("callLetter", station.getCallSign());
                formData.put("band", "FM");

                String format = postResponse(context, url, formData);

                Log.d(TAG, format);

                station.setFormat(format);
            }
            catch (MalformedURLException e) {
                Log.e(TAG, "Error creating URL for POST request");
                e.printStackTrace();
            }
            catch (IOException e) {
                Log.e(TAG, "Error POSTing to get station formats");
                e.printStackTrace();
            }
        }

        Log.d(TAG, "Got all formats");
    }

    private static String postResponse(Context context, URL url, ContentValues formData) throws IOException {
        boolean first = true;
        StringBuilder postData = new StringBuilder();
        for (String key : formData.keySet()) {
            if (first) {
                first = false;
            }
            else {
                postData.append("&");
            }

            postData.append(URLEncoder.encode(key, "UTF-8"));
            postData.append("=");
            postData.append(URLEncoder.encode(formData.getAsString(key), "UTF-8"));
        }

        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        // Write to server
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);


        // Read from the server if we got an good response
        String format = "";
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                builder.append(line + "\n");
            }

            br.close();

            Document doc = Jsoup.parse(builder.toString());
            Element main = doc.body();

            // firefox > F12 > inspect element > copy unique selector
            format = main.select("#container > table:nth-child(4) > tbody:nth-child(1) > tr:nth-child(4) > td:nth-child(2)").text();
            if (format.equals("")) {
                format = main.select("#container > table:nth-child(5) > tbody:nth-child(1) > tr:nth-child(4) > td:nth-child(2)").text();
            }
        }
        else {
            Log.d(TAG, conn.getResponseMessage());
        }

        return format;
    }




    // Try to get a response from the given URL
    //
    // Returns null if it can't connect to the internet
    // Throws an IOException if bad things happen
    private static String getResponse(Context context, String urlString) throws IOException {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        // If we couldn't connect to the internet
        if (networkInfo == null || !networkInfo.isConnected()) {
            Log.d(TAG, "Unable to connect to the internet.");
            return null;
        }
        Log.d(TAG, "Connected to the internet.");

        InputStream is = null;

        try {
            // Set up the HTTP request
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            Log.d(TAG, "Trying to connect to URL: " + urlString);

            // Send request and get response from the URL
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            // Convert InputStream response to String
            String responseString = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
            Log.d(TAG, "HTTP response code: " + response);

            return responseString;
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
