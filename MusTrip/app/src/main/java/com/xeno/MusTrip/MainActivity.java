package com.xeno.MusTrip;

import android.content.Intent;

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
        setContentView(R.layout.activity_main);
        ImageView search = (ImageView) findViewById(R.id.find);
        ImageView trip = (ImageView) findViewById(R.id.trip);

        search.setImageResource(R.drawable.find);
        trip.setImageResource(R.drawable.trip);

        /* If user selects trip mode */
        btnStart = (ImageButton) findViewById(R.id.trip);
        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MapPlayer.class);
                startActivity(intent);
            }
        });
        /* If user wants to search for music by city */
        btnSearch = (ImageButton) findViewById(R.id.find);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CityFinder.class);
                startActivity(intent);
            }
        });
        /* Opens about section */
        about = (ImageView) findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, About.class);
                startActivity(intent);
            }
        });
    }
}
