package com.example.kidcommute;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class parent_home extends AppCompatActivity {
    Button std, feenpay, reply, notification, vehicle_status, logout, Paynot, chat, trackBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        std = findViewById(R.id.buttonStudent10);
        feenpay = findViewById(R.id.button12);
        reply = findViewById(R.id.button11);
        notification = findViewById(R.id.button15);
        vehicle_status = findViewById(R.id.button16);
        Paynot = findViewById(R.id.buttonAdminMsg11);
        logout = findViewById(R.id.button17);
        chat = findViewById(R.id.button18);
        trackBus = findViewById(R.id.buttonTrackBus);

        std.setOnClickListener(v -> open(view_student_profile.class));
        Paynot.setOnClickListener(v -> open(payment_notification.class));
        feenpay.setOnClickListener(v -> open(view_fee_and_pay.class));
        reply.setOnClickListener(v -> open(view_reply.class));
        notification.setOnClickListener(v -> open(com.example.kidcommute.notification.class));
        vehicle_status.setOnClickListener(v -> open(com.example.kidcommute.view_vehicle_status.class));
        chat.setOnClickListener(v -> open(com.example.kidcommute.view_chat_ayaahs.class));
        trackBus.setOnClickListener(v -> { Intent i=new Intent(getApplicationContext(),track_bus.class); startActivity(i); });

        logout.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), com.example.kidcommute.login.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }

    private void open(Class<?> target) {
        startActivity(new Intent(getApplicationContext(), target));
    }
}
