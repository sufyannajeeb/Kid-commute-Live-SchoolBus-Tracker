package com.example.kidcommute;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ayaah_home extends AppCompatActivity {
    Button b1,b2,b3,checkin,chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayaah_home);
        b1 = findViewById(R.id.buttonAyaah9);
        b2 = findViewById(R.id.buttonMessageToParent10);
        b3 = findViewById(R.id.buttonAyyahLogout11);
        checkin = findViewById(R.id.buttonQR19);
        chat = findViewById(R.id.buttonChat18);



        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),view_ayaah_profile.class));
            }
        });


        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Ayaahmessage_to_parent.class));
            }
        });


        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),login.class);
                startActivity(i);
            }
        });
        checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),AndroidBarcodeQrExample.class));

            }
        });
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),view_chat_parents.class));

            }
        });




    }
}