package com.example.googlemapdemo;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;

public class MyService extends Service {

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }
                List<Location> locations = locationResult.getLocations();
                LocationResultHelper locationResultHelper = new LocationResultHelper(getApplicationContext(), locations);
                locationResultHelper.showNotification();
                locationResultHelper.saveTrackedHistoryInPrefs();
                Log.d("mytag", "onLocationResult: "+locationResult.getLocations().get(0));
            }

        };
    }

    public MyService() {}


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground(1001,getNotification());
        getLocationUpdate();

        return START_STICKY;
    }



    private Notification getNotification() {

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), App.CHANNEL_ID)
                .setContentTitle("LocationNotification")
                .setContentText("Location Service is running in the background.")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true);
        return notification.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private void getLocationUpdate() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setMaxWaitTime(5000);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }

        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());

    }
}