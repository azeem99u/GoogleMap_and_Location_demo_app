package com.example.googlemapdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.BaseMovementMethod;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;

public class LocationTrackActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String UNIQUE_BROADCAST_ACTION_STRING_HERE = "UNIQUE_BROADCAST_ACTION_STRING_HERE";
    public static final String SERVICE_RUNNING_KEY = "serviceRunningKey";
    FusedLocationProviderClient mFusedLocationProviderClient;
    LocationCallback mLocationCallback;
    Button btnTracker, btnStopTrack;
    TextView historyTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_track);
        btnTracker = findViewById(R.id.btnTrackerService);
        btnStopTrack = findViewById(R.id.btnTrackerServiceStop);
        historyTxt = findViewById(R.id.txtTrackingHistory);
        historyTxt.setMovementMethod(new BaseMovementMethod());

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        btnTracker.setOnClickListener(view -> {
            //getLocationUpdate();
            //startLocationService();
            setServiceRunningStatus(true);
            getLocationUpdateUsingBroadcast();
        });

        btnStopTrack.setOnClickListener(view -> {
            stopLocationService();
            setServiceRunningStatus(false);
            mFusedLocationProviderClient.removeLocationUpdates(getPendingIntent());
        });


        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
                List<Location> locations = locationResult.getLocations();
                LocationResultHelper locationResultHelper = new LocationResultHelper(LocationTrackActivity.this, locations);
                locationResultHelper.showNotification();
                locationResultHelper.saveTrackedHistoryInPrefs();
            }
        };
    }

    public void getLocationUpdateUsingBroadcast() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setMaxWaitTime( 5000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest,getPendingIntent());

    }

    private PendingIntent getPendingIntent() {

        Intent intent = new Intent(this,LocationBroadcast.class);
        intent.setAction(LocationTrackActivity.UNIQUE_BROADCAST_ACTION_STRING_HERE);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private void startLocationService() {
        Intent intent = new Intent(this, MyService.class);
        ContextCompat.startForegroundService(this, intent);
    }

    private void stopLocationService() {
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);
    }


    private void getLocationUpdate() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setMaxWaitTime(15 * 1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, getMainLooper());

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(LocationResultHelper.LOCATION_TRACK_KEY)) {
            String trackedHistoryFormPref = LocationResultHelper.getTrackedHistoryFormPref(this);
            historyTxt.setText(trackedHistoryFormPref);
        }
        if (key.equals(LocationTrackActivity.SERVICE_RUNNING_KEY)){
            setButtonEnableORDisable();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setButtonEnableORDisable();

        historyTxt.setText(LocationResultHelper.getTrackedHistoryFormPref(this));
    }

    private void setButtonEnableORDisable() {
        if (getServiceRunningStatus()){
            btnTracker.setEnabled(false);
            btnStopTrack.setEnabled(true);
        }else {
            btnTracker.setEnabled(true);
            btnStopTrack.setEnabled(false);
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    void setServiceRunningStatus(boolean value){
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(SERVICE_RUNNING_KEY,value).apply();
    }
    boolean getServiceRunningStatus(){
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SERVICE_RUNNING_KEY,false);
    };



}