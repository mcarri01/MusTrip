package com.xeno.MusTrip;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by mcarr on 11/6/2016.
 */
public class ImageDownloader extends AsyncTask<Void, Void, Bitmap> {
    private String myUrl;
    private Bitmap myMap;

    public ImageDownloader(String url) {
        myUrl = url;

    }

    public Bitmap doInBackground(Void... params) {

        ImageRequest request = new ImageRequest(myUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {

                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
    }
    public Bitmap getMap() {
        return myMap;
    }
}
