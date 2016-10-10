// TutorialApp
// Created by Spotify on 25/02/14.
// Copyright (c) 2014 Spotify. All rights reserved.
package com.xeno.MusTrip;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends Activity implements
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

    TextView txtResult;

    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify;

    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtResult = (TextView) findViewById(R.id.tvResult);
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
//                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
//
//                startActivity(intent);
            }
        });

        btnForward = (Button) findViewById(R.id.btnFoward);
        btnForward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayer.skipToNext();
            }
        });

        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayer.skipToPrevious();
            }
        });
        spotify = api.getService();


        btnGoToMap = (Button) findViewById(R.id.btnGoToMap);
        btnGoToMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);


        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            txtResult.setText(String.valueOf(REQUEST_CODE));
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {

                txtResult.setText(String.valueOf("done5"));

                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        txtResult.setText(String.valueOf("Initialized"));
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addPlayerNotificationCallback(MainActivity.this);
                        //mPlayer.play("spotify:user:spotify:playlist:6OSEPlP10MXOIrTbxr4bHC");
                        mPlayer.play("spotify:user:thesoundsofspotify:playlist:3Ail1brqN8AJFrxL4FcTir");

                        mPlayer.pause();
                        playing = false;
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            } else if(response.getType() == AuthenticationResponse.Type.ERROR) {
                txtResult.setText("access token: " + response.getAccessToken() + "\nCode: " + response.getCode() + "\nError: " + response.getError());
            }else  {
                txtResult.setText(String.valueOf("misc"));
            }
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



        spotify.getTrack(mytrackuri, new Callback<Track>() {
            @Override
            public void success(Track track, Response response) {
                txtResult.setText(track.name);

            }

            @Override
            public void failure(RetrofitError error) {
                txtResult.setText("Error");
            }
        });
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
        spotify.getArtist(mytrackuri, new Callback<Artist>() {
            @Override
            public void success(Artist artist, Response response) {
                txtResult.setText(txtResult.getText() + "\n" + artist.name);
                Log.v("MESSAGE: " , "SUCCESS");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.v("MESSAGE: " , "Fail");

            }
        });




        //Log.v("track = ", track);

        //txtResult.setText(uri);

        switch (eventType) {
            // Handle event type as necessary
            default:
                break;
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
}

