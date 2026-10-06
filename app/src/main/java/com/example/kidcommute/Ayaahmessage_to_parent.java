package com.example.kidcommute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Ayaahmessage_to_parent extends AppCompatActivity implements View.OnClickListener {
    EditText send3;
    Button btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayaahmessage_to_parent);
        send3 = findViewById(R.id.editTextTextPersonName6);
        btn3 = findViewById(R.id.button9);
        btn3.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String message  =send3.getText().toString();

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url = sh.getString("url", "") + "amessage_to_parent";
        Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
        VolleyMultipartRequest volleyMultipartRequest;
        volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {


                            JSONObject obj = new JSONObject(new String(response.data));
                            if (obj.getString("status").equals("ok")) {
                                Toast.makeText(getApplicationContext(), " success", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(getApplicationContext(), ayaah_home.class);
                                startActivity(i);
                            } else {
                                Toast.makeText(getApplicationContext(), " fail", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "----" + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                        }
                }
    },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                SharedPreferences o = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                params.put("aid", sh.getString("aid",""));//passing to python
                params.put("message", message);//passing to python

                return params;
        }
};
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }
}
