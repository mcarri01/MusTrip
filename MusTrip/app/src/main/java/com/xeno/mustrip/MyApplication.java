package com.xeno.MusTrip;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.xeno.MusTrip.Song;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by david bernstein on 10/11/16.
 */



public class MyApplication extends Application {

    public String location = "";
    public int currLat = 0;
    public int currLng = 0;
    public String currUri = "";
    public String currPlace = "";
    public boolean changed = false;
    public Location currLocation;
    public ArrayList<Song> songQueue = new ArrayList<>();

    public void changeLocation(final Location location){
        //final Location loc = location;
        currLocation = location;
        final String lat = String.valueOf(location.getLatitude());
        final String lng = String.valueOf(location.getLongitude());
        final String url = "https://flask-mustrip.herokuapp.com/getPlaylist";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Toast.makeText(MainActivity.this,response,Toast.LENGTH_LONG).show();
                        Log.v("****SUCCESS****",response);
                        try {
                            JSONObject jObject = new JSONObject(response);
                            currPlace = jObject.getString("city");
                            currUri = jObject.getString("playlist");
                            changed = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(MainActivity.this,error.toString(),Toast.LENGTH_LONG).show();
                        Log.v("ERROR IN VOLLEY***",error.toString());
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("lat",lat);
                params.put("lng",lng);
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


}
