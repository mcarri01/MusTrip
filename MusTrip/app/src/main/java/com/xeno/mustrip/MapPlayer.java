// TutorialApp
// Created by Spotify on 25/02/14.
// Copyright (c) 2014 Spotify. All rights reserved.
package com.xeno.MusTrip;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;

public class MapPlayer extends CityFinder implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, PlayerNotificationCallback, ConnectionStateCallback {
    private Location myLocation;
    private String CurrLoc;
    private String CurrTrack;
    private Bitmap CurrImage;
    private ListView lv;
    private ImageView btnPlay;
    private ImageView btnBack;
    private ImageView btnForward;
    private static final int REQUEST_CODE = 1337;
    private boolean playing = false;
    public Player mPlayer;
    private static final String CLIENT_ID = "c3bc81a134e647d2bea359ec1db1f87d";
    private static final String REDIRECT_URI = "spotifytest://callback";
    private ImageView play;
    public Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify = api.getService();
    private Button btnMap;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_player);
        lv = (ListView) findViewById(R.id.lv);
        play = (ImageView) findViewById(R.id.btnPlay);
        if (checkPlayServices()) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
            retrieveLocation();
        }
        txtResult = (TextView) findViewById(R.id.city);

        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        final AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);


        btnPlay = (ImageView) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(playing) {
                    mPlayer.pause();
                    play.setImageResource(R.drawable.pause);

                } else {
                    mPlayer.resume();
                    play.setImageResource(R.drawable.play);

                }
                playing = !playing;
            }
        });

        btnForward = (ImageView) findViewById(R.id.btnForward);
        btnForward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayer.skipToNext();
                //if(!playing) {mPlayer.pause();}

            }
        });

        btnBack = (ImageView) findViewById(R.id.btnRewind);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayer.skipToPrevious();
                //if(!playing){mPlayer.pause();}

            }
        });
        btnMap = (Button) findViewById(R.id.map);
        btnMap.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               Intent intent = new Intent(MapPlayer.this,MapsActivity.class);
               startActivity(intent);
           }
        });
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
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
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private void retrieveLocation() {
        if(checkLocationPermission()) {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        }
    }
    public void addSong(Bitmap cover, String name, String place) {
        Song s = new Song(cover, name, place);
        ((MyApplication) this.getApplication()).songQueue.add(0, s);
    }
    public void getImage() {

        final RequestQueue queue = Volley.newRequestQueue(this);

        String requestUrl = "https://api.spotify.com/v1/search?q=" + CurrTrack + "&type=track";
        String encodedUrl = requestUrl.replaceAll(" ", "%20");

        StringRequest sr = new StringRequest(Request.Method.GET,
                encodedUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonRes = new JSONObject(response);
                            JSONObject tracks = jsonRes.getJSONObject("tracks");
                            JSONArray items = tracks.getJSONArray("items");
                            JSONObject album = items.getJSONObject(0);
                            JSONObject test = album.getJSONObject("album");
                            JSONArray images = test.getJSONArray("images");
                            JSONObject image = images.getJSONObject(0);
                            String url = image.getString("url");
                            ImageRequest request = new ImageRequest(url,
                                    new Response.Listener<Bitmap>() {
                                        @Override
                                        public void onResponse(Bitmap bitmap) {
                                            CurrImage = bitmap;
                                            txtResult.setText("Currently Playing from " + CurrLoc + "\n" + CurrTrack);
                                            addSong(CurrImage, CurrTrack, CurrLoc);
                                            updateView();
                                        }
                                    }, 0, 0, null,
                                    new Response.ErrorListener() {
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                        }
                                    });
                            request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                            queue.add(request);

                        }
                        catch (JSONException e) {
                            txtResult.setText("errrrrror");
                            e.printStackTrace();
                            return;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        sr.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }
    @Override
    public void onSearch(View view) throws Exception {
        final ProgressDialog progress;
        String myLat = "";
        String myLng = "";
        if (mLastLocation != null) {
            myLat = Double.toString(mLastLocation.getLatitude());
            myLng = Double.toString(mLastLocation.getLongitude());
        }
        else {
            myLat = "42";
            myLng = "-71";
        }
        final String Lat = myLat;
        final String Lng = myLng;


        progress = ProgressDialog.show(this, "One moment", "Retrieving location", true);
        String requestUrl = "https://flask-mustrip.herokuapp.com/getPlaylist";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.POST,
                requestUrl,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonRes = new JSONObject(response);
                            CurrLoc = (jsonRes.getString("city"));
                            if (CurrLoc != "error") {
                                CurrTrack = "spotify:user:thesoundsofspotify:playlist:" + jsonRes.getString("playlist");
                                mPlayer.play(CurrTrack);
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progress.dismiss();
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("lat", Lat);
                params.put("lng", Lng);
                return params;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        queue.add(sr);

    }
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
    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1);
        mLocationRequest.setFastestInterval(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
          //  LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, getTracks());
        }
        retrieveLocation();

    }
    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        try {
            // Check if result comes from the correct activity
            if (requestCode == REQUEST_CODE) {
                AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
                if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                    Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                    mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                        @Override
                        public void onInitialized(Player player) {
                            try {
                                mPlayer.addConnectionStateCallback(MapPlayer.this);
                                mPlayer.addPlayerNotificationCallback(MapPlayer.this);

                            } catch (Error e) {
                                Log.v("error",e.toString());
                            }
                        }
                        @Override
                        public void onError(Throwable throwable) {
                            Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                        }
                    });
                } else if (response.getType() == AuthenticationResponse.Type.ERROR) {
                    txtResult.setText("access token: " + response.getAccessToken() + "\nCode: " + response.getCode() + "\nError: " + response.getError());
                }
            }
        } catch (Error e) {
            Log.v("error",e.toString());

        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(PlayerNotificationCallback.EventType eventType, PlayerState playerState) {

        Log.d("MainActivity", "Playback event received: " + eventType.name());
        String uri = playerState.trackUri;
        String[] elems = uri.split(":");

        int i = 0;
        String mytrackuri = "";
        for (String elem : elems) {
            i++;
            if (i == 3) {
                mytrackuri = elem;
            }
        }
        if (eventType == EventType.TRACK_CHANGED) {

            spotify.getTrack(mytrackuri, new Callback<Track>() {
                @Override
                public void success(Track track, retrofit.client.Response response) {
                    CurrTrack = track.name;
                    getImage();
                }
                @Override
                public void failure(RetrofitError error) {
                    txtResult.setText("Error");
                }
            });
        }
    }
    public void updateView() {
        ArrayList<String> arrList=new ArrayList<>();
        //ArrayList<ImageDownloader> imagelist= new ArrayList<>();
        ArrayList<Bitmap> imageList = new ArrayList<Bitmap>();
        final ArrayList q = ((MyApplication) this.getApplication()).songQueue;

//        ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
//        for(int i = 0; i < q.size(); i++) {
//            Map<String, String> datum = new HashMap<String, String>(2);
//            Song s = (Song) q.get(i);
//            datum.put("url", s.getUri());
//            datum.put("track", q.get(i).toString());
//        }

        for(int i = 0; i < q.size(); i++) {
            Song s = (Song) q.get(i);
            // ImageView cover = new ImageView(this);

            //ImageDownloader image = new ImageDownloader(s.getUri());
            //Bitmap img = image.doInBackground();
            // imageList.add()
            imageList.add(s.getCover());
            arrList.add(q.get(i).toString());

        }

        lv.setAdapter(new LazyAdapter(this, arrList, imageList));
        //lv.setAdapter(new SimpleAdapter(this, , R.layout.row_layout, new String[] {"i", "name"}, new int[] {R.id.list_image, R.id.title}));
        //lv.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1 , arrlist));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v,int position, long arg3)
            {

                Toast.makeText(getApplicationContext(), "Location : "+ CurrLoc,   Toast.LENGTH_LONG).show();
            }
        });
    }


}




