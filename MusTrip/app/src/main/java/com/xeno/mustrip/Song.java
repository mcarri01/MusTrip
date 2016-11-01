package com.xeno.mustrip;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by davidbernstein on 10/11/16.
 */

public class Song {
    private String uri;
    private String name;
    private double lat;
    private double lng;
    private String place;

    public String getName() {return name;}
    public void setUri(String s) {uri = s;};
    public void setName(String s) {name = s;};
    public void setLocation(double la, double ln) {lat = la; lng = ln; place = findPlace(la,ln);}

    // Constructor
    public Song(String u, String s, double la, double ln, String p) {
        setUri(u);
        setName(s);
        setLocation(la,ln);
        place = p;
    }

    public Song() {
        setUri("");
        setName("");
        setLocation(0,0);
        place = "";
    }

    public String getUri() {return uri;}

    @Override
    public String toString() {
        String p = "";
        if(place.equals("")) {
            p = "Boston";
        } else {
            p = place;
        }
        return name + " - " + p;// + "[" + lat + "," + lng + "]";
    }

    public String getLocation(){return "[" + lat + "," + lng + "]";}

    private String findPlace(double lat, double lng) {

        HashMap<String, String> postDataParams = new HashMap<>( );
        postDataParams.put("lat",String.valueOf(lat));
        postDataParams.put("lng",String.valueOf(lng));

        URL url;
            String response = "";
            try {
                url = new URL("https://flask-mustrip.herokuapp.com/getPlaylist");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.v("***RESPONSE:::",response);
            return response;


    }
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getPlace() {
        return place;
    }
}
