// TutorialApp
// Created by Spotify on 25/02/14.
// Copyright (c) 2014 Spotify. All rights reserved.
package com.xeno.mustrip;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MapPlayer extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "f1d15f994d784e85b6c78a6ed3ea62f0";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "spotifytest://callback";

    // Request code that will be passed together with authentication result to the onAuthenticationResult callback
    // Can be any integer
    private static final int REQUEST_CODE = 1337;
    private boolean playing = false;
    public Player mPlayer;
    private Button btnPlay;
    private Button btnBack;
    private Button btnForward;
    private Button btnGoToMap;
    private boolean loaded;
    private Button btnMap;
    private Boolean started = false;
    TextView txtResult;

    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify = api.getService();

    private int counter = 0;


    private ListView lv;
    TextView txtTemp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_player);
        btnMap = (Button) findViewById(R.id.btnMap);
        lv = (ListView) findViewById(R.id.lv);
        txtTemp = (TextView) findViewById(R.id.txtTemp);

        txtResult = (TextView) findViewById(R.id.txtResult);
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        btnPlay = (Button) findViewById(R.id.btnPlay);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(playing) {
                    mPlayer.pause();
                    btnPlay.setText("Play");
                } else {
                    mPlayer.resume();
                    btnPlay.setText("Pause");
                }
                playing = !playing;
            }
        });

        btnForward = (Button) findViewById(R.id.btnForward);
        btnForward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayer.skipToNext();
                if(!playing) {mPlayer.pause();}

            }
        });

        btnBack = (Button) findViewById(R.id.btnRewind);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayer.skipToPrevious();
                if(!playing){mPlayer.pause();}

            }
        });


        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapPlayer.this, MapsActivity.class);

                startActivity(intent);
            }
        });



        txtTemp.setText(String.valueOf(++counter));



    }

    public Player getPlayer() {
        return mPlayer;
    }

    public void onResume() {
        super.onResume();  // Always call the superclass method first
        //txtTemp.setText(String.valueOf(++counter));
//        Intent intent = getIntent();
//        int lng = intent.getIntExtra("lng",0);
//        int lat = intent.getIntExtra("lat",0);
//        String toSet = "lat: " + lat + "\nlng: " + lng;
//        int got = ((MyApplication) this.getApplication()).getX();
//        String toSet = "got: " + got;
//        txtTemp.setText(toSet);

        if(((MyApplication) this.getApplication()).changed) {
            ((MyApplication) this.getApplication()).changed = false;

            mPlayer.play(((MyApplication) this.getApplication()).currUri);

        }
        updateView();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        try {
            txtTemp.setText(String.valueOf(++counter));
            super.onActivityResult(requestCode, resultCode, intent);


            // Check if result comes from the correct activity
            if (requestCode == REQUEST_CODE) {
                //txtResult.setText(String.valueOf(REQUEST_CODE));
                AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
                if (response.getType() == AuthenticationResponse.Type.TOKEN) {

                    //      txtResult.setText(String.valueOf("done5"));

                    Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                    mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                        @Override
                        public void onInitialized(Player player) {
                            //            txtResult.setText(String.valueOf("Initialized"));
                            try {

                                mPlayer.addConnectionStateCallback(MapPlayer.this);
                                mPlayer.addPlayerNotificationCallback(MapPlayer.this);
                                //mPlayer.play("spotify:user:spotify:playlist:6OSEPlP10MXOIrTbxr4bHC");
                                mPlayer.play("spotify:user:thesoundsofspotify:playlist:3Ail1brqN8AJFrxL4FcTir");
                                mPlayer.pause();
                                updateView();
                                //          mPlayer.pause();
                                //        playing = false;
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
//                txtResult.setText("access token: " + response.getAccessToken() + "\nCode: " + response.getCode() + "\nError: " + response.getError());
                } else {
//                txtResult.setText(String.valueOf("misc"));
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
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
        updateView();

        String uri = playerState.trackUri;
        String [] elems = uri.split(":");
        int i = 0;
        String mytrackuri = "";
        for(String elem : elems) {
            i++;
            if(i == 3) {
                mytrackuri = elem;
            }
        }
        final String currentText = txtResult.getText().toString();
        final String u = mytrackuri;

        final String place = ((MyApplication) this.getApplication()).currPlace;

        spotify.getTrack(mytrackuri, new Callback<Track>() {
            @Override
            public void success(Track track, Response response) {
                String toSet = "Currently Playing:\n" + track.name;
                if(!place.equals("")) {
                    toSet = "Currently Playing From " + place + ":\n" + track.name;
                }
                if(!(new String(toSet).equals(currentText))) {
                    txtResult.setText(toSet);
                    txtResult.setText(Html.fromHtml("Currently Playing from <b>" + place + "</b> <br><em>" + track.name + "</em>"));

                    addSong(u,track.name);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                txtResult.setText("Error");
            }
        });
        updateView();


        spotify.getAlbum(mytrackuri, new Callback<Album>() {
            @Override
            public void success(Album album, Response response) {
                txtResult.setText(txtResult.getText() + " - " + album.name);

            }

            @Override
            public void failure(RetrofitError error) {
                Log.v("Trying to get album...", "fail");
            }
        });



    }

    public void addSong(String uri, String name) {
        Song s = new Song();
        s.setName(name);
        s.setPlace(((MyApplication) this.getApplication()).currPlace);
        if((!((MyApplication) this.getApplication()).containsSong(s))) {
            ((MyApplication) this.getApplication()).songQueue.add(s);
        }

    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
        switch (errorType) {
            // Handle error type as necessary
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    public void updateView() {
        ArrayList<String> arrlist=new ArrayList<String>();

        final ArrayList q = ((MyApplication) this.getApplication()).songQueue;

        String temp = "";
        for(int i = 0; i < q.size(); i++) {
            temp += String.valueOf(i) + ": " + q.get(i).toString() + "\n";
            arrlist.add(q.get(i).toString());
        }
        //txtTemp.setText(temp);
        lv.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1 , arrlist));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v,int position, long arg3)
            {
                Song curr = (Song)q.get(position);
                String str = "temp";curr.getPlace();
                Toast.makeText(getApplicationContext(), "Location : "+ str,   Toast.LENGTH_LONG).show();
            }
        });
    }




}
