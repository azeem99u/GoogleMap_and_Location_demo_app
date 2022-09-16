package com.example.googlemapdemo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationResult;

import java.util.List;

public class LocationBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            if (LocationTrackActivity.UNIQUE_BROADCAST_ACTION_STRING_HERE.equals(intent.getAction())) {
                LocationResult locationResult = LocationResult.extractResult(intent);
                if (locationResult == null) {
                    return;
                }
                List<Location> locations = locationResult.getLocations();
                LocationResultHelper locationResultHelper = new LocationResultHelper(context, locations);
                locationResultHelper.saveTrackedHistoryInPrefs();
                locationResultHelper.showNotification();

            }
        }else {

            if (LocationTrackActivity.UNIQUE_BROADCAST_ACTION_STRING_HERE.equals(intent.getAction())) {
                LocationResult locationResult = LocationResult.extractResult(intent);
                if (locationResult == null) {
                    return;
                }
                List<Location> locations = locationResult.getLocations();
                LocationResultHelper locationResultHelper = new LocationResultHelper(context, locations);
                locationResultHelper.saveTrackedHistoryInPrefs();
                locationResultHelper.showNotification();
            }
        }
    }
}
