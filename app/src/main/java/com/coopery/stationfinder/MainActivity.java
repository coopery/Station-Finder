package com.coopery.stationfinder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    Switch swListen;
    ListView lvStations;
    Location location;

    private BroadcastReceiver broadcastReceiver;
    ArrayList<Station> stations;

//    public final String[] formats = {"80's Rock", "Adult Album Alternative", "Adult Contemporary", "Adult Hits", "Alternative", "Americana", "Asian", "Blues", "Business News", "Carribean", "Children's", "Christian Contemporary", "Classic Hits", "Classic Rock", "Classical", "College", "Comedy", "Country", "Dance", "Easy Listening", "Electronica", "Ethnic", "Farm", "Folk", "Gospel Music", "Grade School (K-12)", "Hip Hop", "Hot AC", "International", "Jazz", "News", "News/Talk", "Nostalgia", "Oldies", "Other", "Public Radio", "Regional Mexican", "Religious", "Rhythmic Oldies", "Rock", "Smooth Jazz", "Spanish", "Spanish Christian", "Sports", "Talk", "Tejano", "Top-40", "Travelers' Information", "Tropical", "Urban Contemporary", "Variety"};

    // Start up AlarmManager and register our service with it
    private void startGettingUpdates() {
        // Set alarm, register pendingIntent with it
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                0,
                15000,
                pendingIntent);
    }

    // Stop AlarmManager from starting the service
    private void stopGettingUpdates() {
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter("com.coopery.stationfinder.newdata"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        stations = new ArrayList<Station>();
        stations = DataModel.getStations(this);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Received notification that data changed.");

                stations = DataModel.getStations(getApplicationContext());
                resetStationList();
            }
        };

        swListen = (Switch) findViewById(R.id.swListen);

        // Set switch to the state of the alarm
        boolean alarmSet = PendingIntent.getBroadcast(this, 0,
                new Intent(this, AlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE)
                != null;
        swListen.setChecked(alarmSet);

        // Create pendingIntent to broadcast to AlarmReceiver
        Intent broadcastAlarm = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, broadcastAlarm, 0);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Set UI widget listeners
        swListen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG, "Started listening for alarms");
                    startGettingUpdates();
                } else {
                    Log.d(TAG, "Stopped listening for alarms");
                    stopGettingUpdates();
                }
            }
        });

        lvStations = (ListView) findViewById(R.id.lvStations);

        ArrayAdapter<Station> adapter = new ArrayAdapter<Station>(this, android.R.layout.simple_list_item_1, stations);
        lvStations.setAdapter(adapter);

        lvStations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "Item #" + position, Toast.LENGTH_SHORT).show();
            }
        });


        // Make sure Location Services are enabled
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!service.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent goTurnItOnPls = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(goTurnItOnPls);
        }
    }

    private void resetStationList() {
        ArrayAdapter<Station> adapter = (ArrayAdapter<Station>) lvStations.getAdapter();
        adapter.clear();
        adapter.addAll(stations);
        adapter.notifyDataSetChanged();
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
        switch (id) {
            case R.id.action_clearstations:
                DataModel.clearStations(this);
                stations.clear();
                resetStationList();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
