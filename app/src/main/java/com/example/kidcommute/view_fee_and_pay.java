package com.example.kidcommute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class view_fee_and_pay extends AppCompatActivity implements View.OnClickListener {
    TextView t1,t2,t3,t4,t5;
    Button payment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_fee_and_pay);
        t1 = findViewById(R.id.textView41);
        t2 = findViewById(R.id.textView42);
        t3 = findViewById(R.id.textView43);
        t4 = findViewById(R.id.textView39);
        t5 = findViewById(R.id.textView40);
        payment = findViewById(R.id.buttonPayment10);
        payment.setOnClickListener(this);

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url = sh.getString("url", "") + "view_fees";
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
                                t1.setText(obj.getString("student_name"));
                                t4.setText(obj.getString("student_class"));
                                t5.setText(obj.getString("student_division"));
                                t2.setText(obj.getString("month"));
                                t3.setText(obj.getString("amount"));

                                String imgurl = sh.getString("url", "") + obj.getString("photo");
                                Log.d("urlllll", imgurl);
                            } else {
                                Toast.makeText(getApplicationContext(), "Invalid user", Toast.LENGTH_SHORT).show();
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
        startActivity(new Intent(getApplicationContext(),bank.class));
    }
}
