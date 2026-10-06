package com.example.kidcommute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class view_reply extends AppCompatActivity {
    ListView li;
    SharedPreferences sh;
    String url, ip;
    FloatingActionButton send_complaint;

    String[] complaints, reply, pid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reply);

        li = findViewById(R.id.li);
        send_complaint = findViewById(R.id.floatingActionButton);


        sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ip = sh.getString("url", "");
        url = ip + "view_reply";
//        Toast.makeText(getApplicationContext(),sh.getString("pid",""),Toast.LENGTH_SHORT).show();




        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    if (jsonObj.getString("status").equalsIgnoreCase("ok")) {
                        JSONArray js = jsonObj.getJSONArray("q");//from python
                        pid = new String[js.length()];
                        complaints = new String[js.length()];
                        reply = new String[js.length()];

                        for (int i = 0; i < js.length(); i++) {
                            JSONObject u = js.getJSONObject(i);
                            pid[i] = u.getString("id");//dbcolumn name in double quotes
                            complaints[i] = u.getString("complaints");
                            reply[i] = u.getString("reply");
                        }
                        li.setAdapter(new custom_view_reply(getApplicationContext(), pid, complaints, reply));

                    } else {
                        Toast.makeText(getApplicationContext(), "Not found", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error" + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        },


        new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                Toast.makeText(getApplicationContext(), "eeeee" + error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        ) {
            // value Passing android to python
            @Override
            protected Map<String, String> getParams() {
             //   SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Map<String,String>params=new HashMap<String,String>();
                SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                params.put("pid", sh.getString("pid",""));//passing to python

                return params;
            }
        };

        int MY_SOCKET_TIMEOUT_MS = 100000;

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(postRequest);



        send_complaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),send_complaints.class));

            }
        });



    }
}
