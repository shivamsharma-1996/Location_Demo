package com.shivam.location_demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private LocationRequest mLocationRequest;

    private LocationCallback mLocationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations())
                {
                    // Update UI with location data
                    Log.i(TAG, String.valueOf(location.getLatitude()));
                    Log.i(TAG, String.valueOf(location.getLongitude()));

                    Toast.makeText(MainActivity.this, "Lat:" + location.getLatitude() + "\n" + "Long" + location.getLongitude(), Toast.LENGTH_LONG).show();
                }
            }

            ;
        };


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkPlayServices()) {
            if (!checkPermissions()) {
                requestPermissions();
            } else {
                getLastLocation();
            }
        }
    }

    public boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //shouldProvideRationale contains true if user previously denied requested permission

        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            startLocationPermissionRequest();
        } else {
            //shouldProvideRationale contains false if user still has not denied requested permission
            startLocationPermissionRequest();
        }
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.i(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length <= 0) {
                    //If user interaction was interupted, the permission request is cancelled & you
                    // recieve empty arrays.
                    Log.i(TAG, "User interaction was cancelled");
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Permission granted
                    getLastLocation();

                    //createLocationRequest();
                } else {
                    //Permission denied.

                    Log.i(TAG, "Permission denied");
                    showSnackBar(R.string.textwarn, R.string.settings, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                            try {
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });
                }
            }
            break;
        }
    }


    private void showSnackBar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }


    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.i(TAG, String.valueOf(location.getLatitude()));
                            Log.i(TAG, String.valueOf(location.getLongitude()));
                        } else {
                            Toast.makeText(MainActivity.this, "location is null", Toast.LENGTH_LONG);
                        }
                    }
                });
    }


    protected void createLocationRequest()
    {
        //        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(10000);
//        mLocationRequest.setFastestInterval(5000);
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        startLocationUpdates();

    }

    //Starting the location updates
    private void startLocationUpdates()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        else
        {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    //Stopping location updates
    private void stopLocationUpdates()
    {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }



    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

}




