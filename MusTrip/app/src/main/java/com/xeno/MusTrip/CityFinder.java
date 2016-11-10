package com.xeno.MusTrip;
import android.view.View;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentCallbacks2;
import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Typeface;
        import android.os.Bundle;
        import android.support.test.espresso.core.deps.dagger.Component;
        import android.text.Html;
        import android.text.SpannableString;
        import android.text.style.StyleSpan;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.ListView;
        import android.widget.SimpleAdapter;
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

        import com.spotify.sdk.android.authentication.AuthenticationClient;
        import com.spotify.sdk.android.authentication.AuthenticationRequest;
        import com.spotify.sdk.android.authentication.AuthenticationResponse;
        import com.spotify.sdk.android.player.Config;
        import com.spotify.sdk.android.player.ConnectionStateCallback;
        import com.spotify.sdk.android.player.Player;
        import com.spotify.sdk.android.player.PlayerNotificationCallback;
        import com.spotify.sdk.android.player.PlayerState;
        import com.spotify.sdk.android.player.Spotify;
import com.xeno.MusTrip.LazyAdapter;
import com.xeno.MusTrip.MyApplication;
import com.xeno.MusTrip.R;
import com.xeno.MusTrip.Song;

import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.io.IOException;
        import java.io.InputStream;
        import java.io.UnsupportedEncodingException;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.net.URLEncoder;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.concurrent.Callable;

        import kaaes.spotify.webapi.android.SpotifyApi;
        import kaaes.spotify.webapi.android.SpotifyService;
        import kaaes.spotify.webapi.android.models.Album;
        import kaaes.spotify.webapi.android.models.Artist;
        import kaaes.spotify.webapi.android.models.Track;
        import retrofit.Callback;
        import retrofit.RetrofitError;

/**
 * Created by mcarr on 11/1/2016.
 */
public class CityFinder extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback, ComponentCallbacks2{
    public void onTrimMemory(int level) {

        // Determine which lifecycle or system event was raised.
        switch (level) {

            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:

                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */

                break;

            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */

                break;

            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }


    private static final String CLIENT_ID = "c3bc81a134e647d2bea359ec1db1f87d";

    private static final String REDIRECT_URI = "spotifytest://callback";

    // Request code that will be passed together with authentication result to the onAuthenticationResult callback
    // Can be any integer
    private static final int REQUEST_CODE = 1337;
    private boolean playing = false;
    public Player mPlayer;
    private ImageView btnPlay;
    private ImageView btnBack;
    private ImageView btnForward;
    private Boolean started = false;
    private String CurrTrack;
    private String CurrLoc;
    private Bitmap CurrImage;
   // private ArrayList<Song> songQueue;
    private ImageView play;
    TextView txtResult;
    SpotifyApi api = new SpotifyApi();
    SpotifyService spotify = api.getService();

    private ListView lv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_finder);
        lv = (ListView) findViewById(R.id.lv);
        play = (ImageView) findViewById(R.id.btnPlay);

        txtResult = (TextView) findViewById(R.id.city);
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        final AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        Log.d("Why me", "does this work");

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
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
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

    public void onSearch(View view) throws Exception {
        final ProgressDialog progress;
        progress = ProgressDialog.show(this, "One moment", "Sending request", true);
        EditText cityinput = (EditText) findViewById(R.id.input);
        final String cityName = cityinput.getText().toString();
        String requestUrl = "https://flask-mustrip.herokuapp.com/playlistbycity";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.POST,
                requestUrl,
                new Response.Listener<String>() {
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
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
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