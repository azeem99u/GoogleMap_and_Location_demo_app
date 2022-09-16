package com.example.googlemapdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final double ISLAMABAD_LAT = 33.693590;
    public static final double ISLAMABAD_LNG = 73.068872;
    public static final int DEFAULT_ZOOM = 15;
    private static final int REQUEST_OVERLAY_PERMISSIONS = 234;
    private HandlerThread mHandlerThread;

    public static final int PERMISSION_REQUEST_CODE = 9001;
    private static final int PLAY_SERVICES_ERROR_CODE = 9002;
    public static final int GPS_REQUEST_CODE = 1000;
    boolean mLocationPermissionGranted;


    EditText etSearchAddress;
    ImageView searchBtn;
    GoogleMap mGoogleMap;


    private FusedLocationProviderClient mLocationClient;
    LocationCallback mLocationCallback;

    @SuppressLint("VisibleForTests")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSearchAddress = findViewById(R.id.et_locationName);
        searchBtn = findViewById(R.id.search_imgBtn);
        searchBtn.setOnClickListener(this::geoLocationUsingCode);


        initGoogleMap();
        mLocationClient = LocationServices.getFusedLocationProviderClient(this);
        findViewById(R.id.btn_animation).setOnClickListener(this::startLocationUpdate);


        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null){
                    return;
                }
                Location lastLocation = locationResult.getLastLocation();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gotoLocationAnimate(lastLocation.getLatitude(),lastLocation.getLongitude());
                    }
                });
            }
        };




/*  //bounds map view windows define
        findViewById(R.id.btn_animation).setOnClickListener(view -> {

            if (mGoogleMap != null) {

                double bottomBoundary = ISLAMABAD_LAT-0.1;
                double leftBoundary = ISLAMABAD_LNG-0.1;
                double topBoundary = ISLAMABAD_LAT+0.1;
                double rightBoundary = ISLAMABAD_LNG+0.1;

                LatLngBounds ISLAMABAD_BOUNDS = new LatLngBounds(
                        new LatLng(bottomBoundary,leftBoundary),
                        new LatLng(topBoundary,rightBoundary)
                );

                //center location
                //mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ISLAMABAD_BOUNDS.getCenter(),DEFAULT_ZOOM));

                //define width of city
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(ISLAMABAD_BOUNDS,800,1000,1));

                //target restriction
                //mGoogleMap.setLatLngBoundsForCameraTarget(ISLAMABAD_BOUNDS);

                showMarker(ISLAMABAD_BOUNDS.getCenter());
            }


        });*/

//        findViewById(R.id.btn_animation).setOnClickListener(view -> {
//            //zoom Animation for with other place
//            if (mGoogleMap != null) {
//
//                //zoom in button
//                //mGoogleMap.animateCamera(CameraUpdateFactory.zoomBy(3.1f));
//
////                LatLng latLng = new LatLng(23.324234,34.453545);
////                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
////                mGoogleMap.animateCamera(cameraUpdate, 5445, new GoogleMap.CancelableCallback() {
////                    @Override
////                    public void onCancel() {
////                        Toast.makeText(MainActivity.this, "Animation Cancelled", Toast.LENGTH_SHORT).show();
////                    }
////
////                    @Override
////                    public void onFinish() {
////                        Toast.makeText(MainActivity.this, "Animation Finished", Toast.LENGTH_SHORT).show();
////                    }
////                });
//            }
//
//        });


/*        //bounds using endpoints
        findViewById(R.id.btn_animation).setOnClickListener(view -> {

            if (mGoogleMap != null) {

                double bottomBoundary = 33.6065254;
                double leftBoundary = 72.9601927;
                double topBoundary = 33.7449624;
                double rightBoundary = 73.1067928;

                LatLngBounds ISLAMABAD_BOUNDS = new LatLngBounds(
                        new LatLng(bottomBoundary,leftBoundary),
                        new LatLng(topBoundary,rightBoundary)
                );

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(ISLAMABAD_BOUNDS,1));
                showMarker(ISLAMABAD_BOUNDS.getCenter());
            }


        });*/

/*        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container_view, supportMapFragment);*/


    }


    @Override
    protected void onPause() {
        super.onPause();
       if(mLocationCallback != null){
           stopLocationUpdate();
       }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandlerThread != null){
            mHandlerThread.quit();
        }
    }

    private void stopLocationUpdate() {
        mLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
            //startLocationUpdate()
    }

    private void startLocationUpdate(View view) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        mHandlerThread = new HandlerThread("locationThread");
        mHandlerThread.start();

        if (checkLocationPermission() && isGPSEnabled()){
            mLocationClient.requestLocationUpdates(locationRequest,
                    mLocationCallback,mHandlerThread.getLooper());
        }

    }


    private void getCurrantLocation(View view) {
        if (checkLocationPermission() && isGPSEnabled()) {
            mLocationClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Location result = task.getResult();
                    if (result != null) {
                        gotoLocationAnimate(result.getLatitude(), result.getLatitude());
                        Toast.makeText(MainActivity.this, "" + result.getLatitude(), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "" + Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

    }


    private void geoLocationUsingName(View view) {

        String locationName = etSearchAddress.getText().toString();

        if (TextUtils.isEmpty(locationName.trim())) {
            return;
        }
        hideSoftKeyBoard(view);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName, 1);
            if (!addressList.isEmpty()) {
                Address address = addressList.get(0);
                gotoLocation(address.getLatitude(), address.getLongitude());
                Toast.makeText(this, "" + address.getLocality(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void geoLocationUsingCode(View view) {

        double ISLAMABAD_LAT = 33.693590;
        double ISLAMABAD_LNG = 73.068872;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addressList = geocoder.getFromLocation(ISLAMABAD_LAT, ISLAMABAD_LNG, 3);
            if (!addressList.isEmpty()) {
                Address address = addressList.get(0);
                gotoLocation(address.getLatitude(), address.getLongitude());
                Toast.makeText(this, "" + address.getLocality(), Toast.LENGTH_SHORT).show();
            }
            for (Address address1 : addressList) {
                Log.d("tag1", "geoLocationUsingCode: " + address1.getMaxAddressLineIndex());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mGoogleMap = googleMap;
       // gotoLocation(ISLAMABAD_LAT, ISLAMABAD_LNG);

    }

    private void gotoLocation(double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        showMarker(latLng);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

    }

    private void gotoLocationAnimate(double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
        mGoogleMap.animateCamera(cameraUpdate);
        mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        showMarker(latLng);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

    }


    private void showMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng);
        mGoogleMap.addMarker(markerOptions);


    }

    private void initGoogleMap() {

        if (isServicesOk()) {
            if (checkLocationPermission()) {
                SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
                if (supportMapFragment != null) {
                    supportMapFragment.getMapAsync(this);
                }
            } else {
                requestLocationPermission();
            }
        }
    }

    private boolean isGPSEnabled() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (providerEnable) {
            return true;
        } else {

            new AlertDialog.Builder(this)
                    .setTitle("GPS Permissions")
                    .setMessage("GPS is required for this app to work. Please enable GPS.")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, GPS_REQUEST_CODE);
                    })
                    .setCancelable(false)
                    .show();

        }
        return false;
    }


    private boolean isServicesOk() {

        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();

        int result = googleApi.isGooglePlayServicesAvailable(this);

        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApi.isUserResolvableError(result)) {
            Dialog dialog = googleApi.getErrorDialog(this, result, PLAY_SERVICES_ERROR_CODE, task ->
                    Toast.makeText(this, "Dialog is cancelled by User", Toast.LENGTH_SHORT).show());
            dialog.show();
        } else {
            Toast.makeText(this, "Play services are required by this application", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }


    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.track_location: {
                startActivity(new Intent(this,LocationTrackActivity.class));
                break;
            }
            case R.id.map_type_none: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            }
            case R.id.map_type_normal: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            }
            case R.id.satellite: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            }
            case R.id.map_type_terrain: {

                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

                break;
            }
            case R.id.map_type_hybrid: {
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            }
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            mLocationPermissionGranted = true;
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideSoftKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        if (requestCode == GPS_REQUEST_CODE) {

            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean providerEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (providerEnable) {
                Toast.makeText(this, "GPS is enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "GPS not enabled. Unable to show user location", Toast.LENGTH_SHORT).show();
            }

        }
    }
}