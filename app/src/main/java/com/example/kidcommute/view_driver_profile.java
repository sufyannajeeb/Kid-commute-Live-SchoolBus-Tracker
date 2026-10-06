package com.example.kidcommute;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class view_driver_profile extends AppCompatActivity implements View.OnClickListener {
    TextView t1,t2,t3,t4,t5,t6,t7;
//    Button b4;
    ImageView img1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.
                layout.activity_view_driver_profile);
        t1 = findViewById(R.id.textView2);
        t2 = findViewById(R.id.textView3);
        t3 = findViewById(R.id.textView4);
        t4 = findViewById(R.id.textView5);
        t5 = findViewById(R.id.textView6);
        t6 = findViewById(R.id.textView8);
        t7 = findViewById(R.id.textView9);
//        b4 = findViewById(R.id.button2);
        img1 = findViewById(R.id.imageView);
//        b4.setOnClickListener(this);
        img1.setOnClickListener(this);



        SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url= sh.getString("url","")+"driver_view_profile";
        Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>(){
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {

                            JSONObject obj = new JSONObject(new String(response.data));

                            if(obj.getString("status").equals("ok")){
//                                JSONArray js=obj.getJSONArray("dv");
//                                JSONObject js1=js.getJSONObject(0);
                                t1.setText(obj.getString("driver_name"));
                                t2.setText(obj.getString("license_number"));
                                t3.setText(obj.getString("contact_number"));
                                t4.setText(obj.getString("place"));
                                t5.setText(obj.getString("post"));
                                t6.setText(obj.getString("pincode"));
                                t7.setText(obj.getString("district"));
                                String imgurl=sh.getString("url","")+obj.getString("photo");
                                Log.d("urlllll",imgurl);
                                Picasso.with(getApplicationContext()).load(imgurl).into(img1);
                                Picasso.with(getApplicationContext()).load(imgurl).transform(new CircleTransform()).into(img1);
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"Invalid user" ,Toast.LENGTH_SHORT).show();
                            }



                            } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"----" +e.getMessage().toString(),Toast.LENGTH_SHORT).show();
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

                params.put("uid",sh.getString("uid",""));


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

    }
}