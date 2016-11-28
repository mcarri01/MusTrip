package com.xeno.MusTrip;

import android.content.Intent;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;


import io.fabric.sdk.android.Fabric;

/**
 * Main Activity for MusTrip
 * Sets up main page for the app, user can select whether to read the About section, or whether
 * to select the "Trip" or "Search" modes.
 *
 */

public class MainActivity extends AppCompatActivity {

    ImageButton btnStart;
    ImageButton btnSearch;
    ImageView about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3399FF")));
        getSupportActionBar().setTitle("Welcome to MusTrip");

        setContentView(R.layout.activity_main);
        ImageView search = (ImageView) findViewById(R.id.find);
        ImageView trip = (ImageView) findViewById(R.id.trip);

        search.setImageResource(R.drawable.magnifying);
        trip.setImageResource(R.drawable.trip);
    }
    /* If user selects trip mode */
    public void onTripClick(View view) {
        Intent intent = new Intent(MainActivity.this,MapPlayer.class);
        startActivity(intent);
    }
    /* If user wants to search for music by city */
    public void onSearchClick(View view) {
        Intent intent = new Intent(MainActivity.this, CityFinder.class);
        startActivity(intent);
    }
    /* Opens about section */
    public void onAboutClick(View view) {
        Intent intent = new Intent(MainActivity.this, About.class);
        startActivity(intent);
    }

}
