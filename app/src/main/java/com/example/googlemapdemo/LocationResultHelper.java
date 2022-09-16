package com.example.googlemapdemo;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class LocationResultHelper {


    public static final String LOCATION_TRACK_KEY = "location_Track_key";
    private Context mContext;
    private List<Location> mLocations;

    public LocationResultHelper(Context context, List<Location> locations) {
        this.mContext = context;
        this.mLocations = locations;
    }


    @SuppressLint("CommitPrefEdits")
    public void saveTrackedHistoryInPrefs(){
        PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                .putString(LOCATION_TRACK_KEY,getLocationResultTitle()+"\n"+getLocationText()).apply();
    }

    public static String getTrackedHistoryFormPref(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(LOCATION_TRACK_KEY,"History not available");
    }


    public String getLocationText() {


        if (mLocations.isEmpty()) {
            return "Location not received";
        } else {

            StringBuilder stringBuilder = new StringBuilder();
            for (Location location : mLocations) {

                stringBuilder.append(DateFormat.getDateTimeInstance().format(new Date()));
                stringBuilder.append("\n");
                stringBuilder.append("(");
                stringBuilder.append(location.getLatitude());
                stringBuilder.append(",");
                stringBuilder.append(location.getLongitude());
                stringBuilder.append(")");
                stringBuilder.append("\n");
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        }
    }

    public void showNotification() {

        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(mContext);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext, App.CHANNEL_ID)
                .setContentTitle(getLocationResultTitle())
                .setContentText(getLocationText())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification.build());


    }

    private CharSequence getLocationResultTitle() {
        String result = mContext.getResources()
                .getQuantityString(R.plurals.num_locations_reported, mLocations.size(), mLocations.size());
        return result+" : "+ DateFormat.getDateTimeInstance().format(new Date());

    }



}
