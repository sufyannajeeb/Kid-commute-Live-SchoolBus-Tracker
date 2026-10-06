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
import android.widget.TextView;
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

public class bank extends AppCompatActivity implements View.OnClickListener {
    TextView amount;
    Button confirm_payment;
    EditText acctnum,acctholder,cvv,expirydte,crdnumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);
        amount = findViewById(R.id.textViewAmount67);
        acctnum = findViewById(R.id.editTextTextPersonName9);
        acctholder = findViewById(R.id.editTextTextPersonName8);
        cvv = findViewById(R.id.editTextTextPersonName10);
        expirydte = findViewById(R.id.editTextTextPersonName11);
        crdnumber = findViewById(R.id.editTextTextPersonName12);
        confirm_payment = findViewById(R.id.buttonConfirmPayment11);
        confirm_payment.setOnClickListener(this);



        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url = sh.getString("url", "") + "view_amount";
        Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            if (obj.getString("status").equals("ok")) {
//                                JSONArray js=obj.getJSONArray("dv");
//                                JSONObject js1=js.getJSONObject(0);
                                amount.setText(obj.getString("amount"));

                                String imgurl = sh.getString("url", "") + obj.getString("photo");
                                Log.d("urlllll", imgurl);
                            } else {
                                Toast.makeText(getApplicationContext(), "Invalid user", Toast.LENGTH_SHORT).show();
                            }
                        }  catch (
                    JSONException e) {
                        e.printStackTrace();
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
                SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                params.put("pid", sh.getString("pid", ""));


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

    @Override
    public void onClick(View view) {
        String account_number  =acctnum.getText().toString();
        String account_holder  =acctholder.getText().toString();
        String cv_v = cvv.getText().toString();
        String expirydate = expirydte.getText().toString();
        String cardnumber = crdnumber.getText().toString();



        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url = sh.getString("url", "") + "confirm_payment";
        Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
        VolleyMultipartRequest volleyMultipartRequest;
        volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {


                            JSONObject obj = new JSONObject(new String(response.data));

                            if (obj.getString("status").equals("ok")) {
                                Toast.makeText(getApplicationContext(), " Payment Success", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(getApplicationContext(), parent_home.class);
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
                params.put("pid", sh.getString("pid",""));//passing to python
                params.put("account_number", account_number);//passing to
                params.put("holder", account_holder);//passing to python
                params.put("cvv", cv_v);//passing to python
                params.put("expiry_date ", expirydate);//passing to
                params.put("card_no ", cardnumber);//passing to python


                return params;
            }
        };

        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }
}

