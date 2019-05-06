package com.e.friendfinder.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import android.view.Menu;

import com.e.friendfinder.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.List;


public class FriendMap extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "Check";
    private static final String TAG2 = "location";

    private GoogleMap mGoogleMap;
    private final double radius = 1609.34; //radius of drawn circle in meters, one mile
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private FusedLocationProviderClient mFusedLocationClient;
    private FirebaseDatabase mfirebaseDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    private String time = "timestamp";
    private String lat = "latitude";
    private String longi = "longitude";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_map);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        Log.d(TAG, "Calling Create FriendMap");
        mAuth = FirebaseAuth.getInstance();
        mfirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mfirebaseDatabase.getReference().child("Users");


    }

    @Override
    protected void onStart() {
        super.onStart();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    mGoogleMap.clear();
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        String userID = user.getKey();

                        String username = user.child("username").getValue(String.class);
                        String timeStamp = user.child(time).getValue(String.class);
                        Double lati = user.child(lat).getValue(Double.class);
                        Double lon = user.child(longi).getValue(Double.class);

                            if (mLastLocation != null && lati != null && lon != null) {
                                LatLng friendLatLng = new LatLng(lati, lon);

                                float[] results = new float[1];
//                                Log.d(TAG2, "location Not NUll");

                                Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(), lati, lon, results);
                                float distanceInMeters = results[0];
                                if (distanceInMeters <= radius) {
//                                    Log.d(TAG2, "correct radius");

                                    markFriends(friendLatLng, userID, username, timeStamp);
                                }
                            }


//                            Log.d(TAG, "Username" + username);
//                            Log.d(TAG, "Latitude" + lati.toString());
//                            Log.d(TAG, "Longitude" + lon.toString());
//                            Log.d(TAG, "---------------------");

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void markFriends(LatLng friendLatLng, String s, String username, String timeStamp) {
        String currID = mAuth.getUid();

        MarkerOptions friend = new MarkerOptions();
        //mGoogleMap.clear();
        friend.position(friendLatLng);
        String userTime = username + " |" + timeStamp + "|";
        friend.title(userTime);
        if(currID.equals(s)){
            friend.title("ME" + " |" + timeStamp + "|");
            friend.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        }
        mGoogleMap.addMarker(friend);
        LatLng prev = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
        drawCircle(prev);
//        Log.d(TAG2, "DRAW");


    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000); // one minute interval
        mLocationRequest.setFastestInterval(60000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                Log.d(TAG, "Calling LocationCallback");

                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
//                LatLng oldLatlng = new LatLng(location.getLatitude(), location.getLongitude());
//                drawCircle(oldLatlng);
//                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                //Place current location marker
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                updateDatabaseLocation(location);
//                getFriends();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("You");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
                //move map camera
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                drawCircle(latLng);

            }
        }
    };


    private void updateDatabaseLocation(Location location) {
        String userID = mAuth.getUid();
        DatabaseReference currRef;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM-hh:mm:ss");
        String currTime = simpleDateFormat.format(new Date());
        Log.d(TAG2, "Current Timestamp: " + currTime);

        currRef = mfirebaseDatabase.getReference().child("Users").child(userID);
        currRef.child(lat).setValue(location.getLatitude());
        currRef.child(longi).setValue(location.getLongitude());
        currRef.child(time).setValue(currTime);
//        Log.d(TAG, "Update MyLocation");
//        Log.d(TAG, "User ID " + userID );
//        Log.d(TAG, "Latitude " + Double.toString(location.getLatitude()));
//        Log.d(TAG, "Longitude " + Double.toString(location.getLongitude()));
//        Log.d(TAG, "---------------------");

    }

    //draw circle keeps will only friends within 1 mile are in the circle
    private void drawCircle(LatLng latLng) {
        CircleOptions circleOpt = new CircleOptions().center(latLng).radius(radius).strokeColor(Color.RED);
       // circleOpt.strokeColor(255);
        mGoogleMap.addCircle(circleOpt);
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("FriendFinder Permission Needed")
                        .setMessage("FriendFinder needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(FriendMap.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);

                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logButton){
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logged out", Toast.LENGTH_LONG).show();
        Intent log = new Intent(FriendMap.this, LoginAccount.class);
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        startActivity(log);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
