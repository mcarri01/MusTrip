package com.xeno.MusTrip;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class Main2Activity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Button btn = (Button) findViewById(R.id.btnPlay);
//        btn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//
//                Intent intent = new Intent(Main2Activity.this, MainActivity.class);
//                startActivity(intent);
//            }
//        });
    }
}
