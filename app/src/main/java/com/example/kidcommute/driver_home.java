package com.example.kidcommute;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class driver_home extends AppCompatActivity  {
    Button b3;
    Button b4;
    Button b5,b6;
    Button logout,vehicle_status;

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        b3 = findViewById(R.id.button3);
        b4 = findViewById(R.id.button5);
        b5 = findViewById(R.id.button6);
        b6 = findViewById(R.id.button7);
        vehicle_status = findViewById(R.id.button14);
        logout = findViewById(R.id.button2);


        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),view_driver_profile.class));
            }
        });


        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),dmessage_to_admin.class));
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),dmessage_to_parent.class));
            }
        });

        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),dchange_password.class));
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),login.class);
                startActivity(i);
            }
        });


        vehicle_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),vehicle_status.class));
            }

        });

        // live GPS sharing to the admin side
        startLocationSharing();

    }

    private void startLocationSharing() {
        boolean fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fine || coarse) {
            startService(new Intent(getApplicationContext(), LocationService.class));
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST) {
            return;
        }
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_GRANTED) {
                startService(new Intent(getApplicationContext(), LocationService.class));
                Toast.makeText(getApplicationContext(), "Sharing this bus's live location with parents", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Toast.makeText(getApplicationContext(),
                "Location permission denied - parents cannot see this bus live",
                Toast.LENGTH_LONG).show();
    }


}
