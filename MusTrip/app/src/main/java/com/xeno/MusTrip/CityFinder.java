package com.xeno.MusTrip;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

/**
 * Created by mcarr on 11/1/2016.
 * The CityFinder class allows users to search for music based off city. It begins by initializing
 * the user to Spotify, and then has appropriate event listeners to store necessary information
 * about a song and make further requests before playing the song and displaying it in a collective
 * queue.
 */
public class CityFinder extends AppCompatActivity implements
        PlayerNotificationCallback, ConnectionStateCallback {


    private static final String CLIENT_ID = "c3bc81a134e647d2bea359ec1db1f87d";
    private static final String REDIRECT_URI = "spotifytest://callback";

    // Request code that will be passed together with authentication result to the onAuthenticationResult callback
    // Can be any integer

    private static final int REQUEST_CODE = 1337;
    private boolean playing = true;
    public Player mPlayer;
    private ImageView btnPlay;
    private ImageView btnBack;
    private ImageView btnForward;
    private String CurrTrack;
    private String CurrLoc;
    private String CurrArtist;
    private Bitmap CurrImage;
    private ArrayList<Song> songQueue = new ArrayList<>();
    private ImageView play;
    TextView txtResult;
    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify = api.getService();
    private ListView lv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_finder);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3399FF")));
        getSupportActionBar().setTitle("Search for Music by City");

        lv = (ListView) findViewById(R.id.lv);
        play = (ImageView) findViewById(R.id.btnPlay);
        txtResult = (TextView) findViewById(R.id.city);

        /* Spotify authentication */
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        final AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        /* onClick listeners for player buttons */
        btnPlay = (ImageView) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(playing) {
                    mPlayer.pause();
                    play.setBackgroundResource(R.drawable.play);

                } else {
                    mPlayer.resume();
                    play.setBackgroundResource(R.drawable.pause);

                }
                playing = !playing;
            }
        });

        btnForward = (ImageView) findViewById(R.id.btnForward);
        btnForward.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mPlayer.skipToNext();
                play.setBackgroundResource(R.drawable.pause);

            }
        });

        btnBack = (ImageView) findViewById(R.id.btnRewind);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /* Removes song (and the previous, because it will just be added back again,
                 from the queue, unless there is only a single song left */
                if (songQueue.size() > 1) {
                    songQueue.remove(1);
                    songQueue.remove(0);
                }
                mPlayer.skipToPrevious();
                play.setBackgroundResource(R.drawable.pause);
            }
        });
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
                                mPlayer.addConnectionStateCallback(CityFinder.this);
                                mPlayer.addPlayerNotificationCallback(CityFinder.this);

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
        /* Using TRACK_CHANGED because it encompasses all "play" events */
        if (eventType == EventType.TRACK_CHANGED) {
                spotify.getTrack(mytrackuri, new Callback<Track>() {
                    @Override
                    public void success(Track track, retrofit.client.Response response) {
                        /* Once it works, we can store the image, now we just need the image for it */
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

    public void addSong(Bitmap cover, String name, String place, String artist) {
        Song s = new Song(cover, name, place, artist);
        songQueue.add(0, s);
    }
    /* Retrieves cover art for the image */
    public void getImage() {

        final RequestQueue queue = Volley.newRequestQueue(this);

        String requestUrl = "https://api.spotify.com/v1/search?q=" + CurrTrack + "&type=track";
        /* Some songs have multiple words */
        String encodedUrl = requestUrl.replaceAll(" ", "%20");

        StringRequest sr = new StringRequest(Request.Method.GET,
                encodedUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        /* Probably a better way to extract the url for the album, but hey */
                        try {
                            JSONObject jsonRes = new JSONObject(response);
                            JSONObject tracks = jsonRes.getJSONObject("tracks");
                            JSONArray items = tracks.getJSONArray("items");
                            JSONObject album = items.getJSONObject(0);
                            JSONObject test = album.getJSONObject("album");
                            JSONArray artists = test.getJSONArray("artists");
                            JSONObject artist = artists.getJSONObject(0);
                            CurrArtist = artist.getString("name");

                            JSONArray images = test.getJSONArray("images");
                            JSONObject image = images.getJSONObject(0);
                            String url = image.getString("url");
                            ImageRequest request = new ImageRequest(url,
                                    new Response.Listener<Bitmap>() {
                                        @Override
                                        public void onResponse(Bitmap bitmap) {
                                            /* Stores current bitmap */
                                            CurrImage = bitmap;
                                            /* Updates text at top of queue*/
                                            txtResult.setText(Html.fromHtml("Currently Playing from " + CurrLoc + "\n<br>" + CurrTrack + "</br>"));
                                            /* Create song object and add to queue */
                                            addSong(CurrImage, CurrTrack, CurrLoc, CurrArtist);
                                            /* Update queue with new info */
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
        sr.setRetryPolicy(new DefaultRetryPolicy(5000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }
    @Override
    public void onPlaybackError(PlayerNotificationCallback.ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
        switch (errorType) {
            // Handle error type as necessary
            default:
                break;
        }
    }
    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }
    /* Where song queue is updated */
    public void updateView() {
        ArrayList<String> arrList=new ArrayList<>();
        ArrayList<Bitmap> imageList = new ArrayList<Bitmap>();
        final ArrayList q = songQueue;
        /* Create separate arrayLists for both the song names and album covers */
        for(int i = 0; i < q.size(); i++) {
            Song s = (Song) q.get(i);
            imageList.add(s.getCover());
            arrList.add(q.get(i).toString());

        }
        /* Use custom adaptor to pair up images w/ text */
        lv.setAdapter(new LazyAdapter(this, arrList, imageList));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v,int position, long arg3)
            {

                Toast.makeText(getApplicationContext(), "Location : "+ CurrLoc,   Toast.LENGTH_LONG).show();
            }
        });
    }
    /* When user searches for a city  */
    public void onSearch(View view) throws Exception {
        /* Gives user information that process is currently running to retrieve playlists */
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Retrieving Location");
        progress.setProgress(0);
        progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        progress.show();
        /* Get whichever city the user inputted */
        SearchView cityinput = (SearchView) findViewById(R.id.input);
        final String cityName = cityinput.getQuery().toString();
        /* Url for request to server */
        String requestUrl = "https://flask-mustrip.herokuapp.com/playlistbycity";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.POST,
                requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            /* Pulls out both the city name and the actual playlist for updating */
                            JSONObject jsonRes = new JSONObject(response);
                            String location = (jsonRes.getString("city"));

                            if (!location.equals("error")) {
                                CurrTrack = "spotify:user:thesoundsofspotify:playlist:" + jsonRes.getString("playlist");
                                CurrLoc = location;
                                if (CurrTrack != null) {
                                    mPlayer.play(CurrTrack);
                                }
                            }
                            else {
                                Toast.makeText(CityFinder.this, "Please provide a valid city",
                                        Toast.LENGTH_LONG).show();
                                mPlayer.clearQueue();
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progress.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        progress.dismiss();
                        Toast.makeText(CityFinder.this, "Please provide a valid city",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        })
        {
            @Override
            /* Creates parameters to pass to request */
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("city", cityName);
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

}