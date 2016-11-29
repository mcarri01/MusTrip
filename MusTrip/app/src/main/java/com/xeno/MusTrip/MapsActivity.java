package com.xeno.MusTrip;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private MapView mMapView;
    GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private String CurrSong;
    private String CurrArtist;
    private Bitmap CurrImage;
    private ArrayList<String> songs = new ArrayList<String>();
    private ArrayList<String> artists = new ArrayList<String>();
    private ArrayList<Bitmap> covers = new ArrayList<Bitmap>();
    private ArrayList<MarkerOptions> markers = new ArrayList<MarkerOptions>();
    private CameraPosition cp = null;
    private LatLng latLng;

    private int counter = 0;
    private Button btn;

    public static int temp() {
        return 0;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            songs = savedInstanceState.getStringArrayList("songs");
            artists = savedInstanceState.getStringArrayList("artists");
            covers = savedInstanceState.getParcelableArrayList("covers");
            cp = savedInstanceState.getParcelable("camera");
        }
        setContentView(R.layout.activity_maps);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            CurrSong = b.getString("song");
            CurrArtist = b.getString("artist");
            CurrImage = b.getParcelable("album");
            cp = b.getParcelable("camera");
            songs.add(CurrSong);
            artists.add(CurrArtist);
            covers.add(CurrImage);
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        btn = (Button) findViewById(R.id.update);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("CURR LOCATION:","lat: " + mLastLocation.getLatitude() + " lng: " + mLastLocation.getLongitude());
                onLocationChanged(mLastLocation);
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.getTracks);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putStringArrayList("songs", songs);
        savedInstanceState.putStringArrayList("artists", artists);
        savedInstanceState.putParcelableArrayList("covers", covers);
        savedInstanceState.putParcelable("camera", mMap.getCameraPosition());
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed()
    {

        super.onBackPressed();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (cp != null) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
        }

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
       if (checkLocationPermission()) {
           mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                   mGoogleApiClient);
           if (mLastLocation != null) {
               //place marker at current position
               //mGoogleMap.clear();
               addMarkers(mLastLocation);
               mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
               cp = mMap.getCameraPosition();
           }

           mLocationRequest = new LocationRequest();
           mLocationRequest.setInterval(5000); //5 seconds
           mLocationRequest.setFastestInterval(3000); //3 seconds
           mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

           LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
       }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            ((MyApplication) this.getApplication()).changeLocation(location);
        }

        Log.v("*******LOCATION: ","CHANGED!!!");
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        addMarkers(location);


        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        cp = mMap.getCameraPosition();
        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }
    private void addMarkers(Location location) {
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        if (CurrSong != null) {
            markerOptions.title(CurrSong + " - " + CurrArtist);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(CurrImage));

        } else {
            markerOptions.title("Currently no song playing");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker());
        }
        markers.add(markerOptions);

        for (int i = 0; i < markers.size(); i++) {
            mMap.addMarker(markers.get(i));
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }
}