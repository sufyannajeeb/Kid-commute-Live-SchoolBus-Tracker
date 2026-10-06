package com.example.kidcommute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class dchange_password extends AppCompatActivity implements View.OnClickListener {
    EditText p1, p2, p3;
    Button bp8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dchange_password);
        p1 = findViewById(R.id.editTextTextPassword);
        p2 = findViewById(R.id.editTextTextPassword2);
        p3 = findViewById(R.id.editTextTextPassword3);
        bp8 = findViewById(R.id.button8);
        bp8.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view == bp8) {
            String currentpassword = p1.getText().toString();
            String newpassword = p2.getText().toString();
            String confirmpassword = p3.getText().toString();

            if (currentpassword.length() < 1) {
                p1.setError("Please enter current password");
            } else if (newpassword.length() < 1) {
                p2.setError("Please enter new password");
            } else if (confirmpassword.length() < 1) {
                p3.setError("password do not match");
            } else {
                SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String url = sh.getString("url", "") + "driver_change_password";
                Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
                VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                        new Response.Listener<NetworkResponse>() {
                            @Override
                            public void onResponse(NetworkResponse response) {
                                try {


                                    JSONObject obj = new JSONObject(new String(response.data));

                                    if (obj.getString("status").equals("ok")) {
                                        Toast.makeText(getApplicationContext(), "Password successfully updated", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(getApplicationContext(), driver_home.class);
                                        startActivity(i);
                                        // Handle success as needed
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Failed to update password. Please check your credentials.", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
//                                    int e1 = Log.e(e.toString());
                                    Toast.makeText(getApplicationContext(), "----" + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }) {


                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        SharedPreferences o = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        // Assuming the server identifies the user by the username
                        params.put("lid", o.getString("lid", ""));
                        params.put("current_password", currentpassword);
                        params.put("new_password", newpassword);

                        return params;
                    }


                    @Override
                    protected Map<String, DataPart> getByteData() {
                        Map<String, DataPart> params = new HashMap<>();
                        long imagename = System.currentTimeMillis();
//                        params.put("pic", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                        return params;
                    }
                };

                Volley.newRequestQueue(this).add(volleyMultipartRequest);


            }


        }
    }
}