package com.xeno.MusTrip;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;


import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    Button btnStart;
    Button btnSearch;
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



        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MapPlayer.class);
                startActivity(intent);
            }
        });
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CityFinder.class);
                startActivity(intent);
            }
        });
        about = (ImageView) findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, About.class);
                startActivity(intent);
            }
        });
    }
}
