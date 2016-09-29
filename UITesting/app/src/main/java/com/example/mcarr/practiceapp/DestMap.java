package com.example.mcarr.practiceapp;

import android.os.Bundle;
import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * Created by mcarr on 9/27/2016.
 */
public class DestMap extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapwindow);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout(width, height);
    }
}
