package com.xeno.MusTrip;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by mcarr on 11/7/2016.
 *
 * The About class simply displays the "about" section layout
 */
public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        getSupportActionBar().setTitle("About MusTrip");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3399FF")));
    }
}
