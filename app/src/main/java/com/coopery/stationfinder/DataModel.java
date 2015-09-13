package com.coopery.stationfinder;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by coopery on 8/20/15.
 */
public class DataModel {

    private static final String TAG = "DataModel";
    private static final String FILENAME = "station_data.json";

    public static synchronized ArrayList<Station> getStations(Context context) {
        ArrayList<Station> stations = new ArrayList<>();
        try {
            FileInputStream file = context.openFileInput(FILENAME);
            Log.d(TAG, "Successfully opened file for reading");

            // Read the entire InputStream into a string
            Scanner scanner = new Scanner(file).useDelimiter("\\A");
            String jsonFile = scanner.hasNext() ? scanner.next() : "";

            stations = parseStationJson(jsonFile);
            Log.d(TAG, "Successfully parsed file");
        }
        catch (Exception e) {

        }

        return stations;
    }

    // Returns true if successful, false otherwise
    public static synchronized boolean setStations(Context context, ArrayList<Station> stations) {
        try {
            FileOutputStream file = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            Log.d(TAG, "Successfully opened file for writing");

            file.write(makeStationJson(stations).getBytes());
            Log.d(TAG, "Successfully saved data to file");

            file.close();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized boolean clearStations(Context context) {
        return context.deleteFile(FILENAME);
    }

    private static String makeStationJson(ArrayList<Station> stations) {
        JSONObject baseJson = new JSONObject();

        try {
            JSONArray jsonArray = new JSONArray();

            for (Station station : stations) {
                JSONObject stationJson = new JSONObject();

                stationJson.put("callsign", station.getCallSign());
                stationJson.put("frequency", station.getFrequency());
                stationJson.put("ns", station.getNs());
                stationJson.put("dlat", station.getDlat());
                stationJson.put("mlat", station.getMlat());
                stationJson.put("slat", station.getSlat());
                stationJson.put("ew", station.getEw());
                stationJson.put("dlon", station.getDlon());
                stationJson.put("mlon", station.getMlon());
                stationJson.put("slon", station.getSlon());
                stationJson.put("format", station.getFormat());

                jsonArray.put(stationJson);
            }

            baseJson.put("stations", jsonArray);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return "";
        }

        return baseJson.toString();
    }

    private static ArrayList<Station> parseStationJson(String stationsJson) {
        ArrayList<Station> stations = new ArrayList<Station>();

        try {
            JSONObject baseJson = new JSONObject(stationsJson);
            JSONArray stationJsonArray = baseJson.getJSONArray("stations");

            for (int i = 0; i < stationJsonArray.length(); ++i) {
                JSONObject stationJson = stationJsonArray.getJSONObject(i);

                Station station = new Station();

                station.setCallSign(stationJson.getString("callsign"));
                station.setFrequency(stationJson.getDouble("frequency"));

                station.setNs(stationJson.getString("ns"));
                station.setDlat(stationJson.getDouble("dlat"));
                station.setMlat(stationJson.getDouble("mlat"));
                station.setSlat(stationJson.getDouble("slat"));

                station.setEw(stationJson.getString("ew"));
                station.setDlon(stationJson.getDouble("dlon"));
                station.setMlon(stationJson.getDouble("mlon"));
                station.setSlon(stationJson.getDouble("slon"));

                try {
                    station.setFormat(stationJson.getString("format"));
                }
                catch (JSONException e) {
                    station.setFormat("");
                }

                stations.add(station);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            stations.clear();
        }

        return stations;
    }
}
