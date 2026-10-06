package com.example.kidcommute;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class view_student_profile extends AppCompatActivity implements View.OnClickListener {
    TextView t1,t2,t3,t4,t5,t6,t7,t8;
    ImageView img3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_student_profile);
        t1 = findViewById(R.id.textViewStudentName30);
        t2 = findViewById(R.id.textViewStudentClass31);
        t3 = findViewById(R.id.textViewStudentDivision38);
        t4 = findViewById(R.id.textViewPlace39);
        t5 = findViewById(R.id.textViewPost40);
        t6 = findViewById(R.id.textViewPincode41);
        t7 = findViewById(R.id.textViewDistrict42);
        t8 = findViewById(R.id.textViewPickupPoint43);
        img3 = findViewById(R.id.imageView3);

        img3.setOnClickListener(this);

        SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url= sh.getString("url","")+"student_view_profile";
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
                                t1.setText(obj.getString("student_name"));
                                t2.setText(obj.getString("student_class"));
                                t3.setText(obj.getString("student_division"));
                                t4.setText(obj.getString("place"));
                                t5.setText(obj.getString("post"));
                                t6.setText(obj.getString("pincode"));
                                t7.setText(obj.getString("district"));
                                t8.setText(obj.getString("pickup_point"));
                                String imgurl=sh.getString("url","")+obj.getString("photo");
                                Log.d("urlllll",imgurl);
                                Picasso.with(getApplicationContext()).load(imgurl).into(img3);
                                Picasso.with(getApplicationContext()).load(imgurl).transform(new CircleTransform()).into(img3);
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

                params.put("pid",sh.getString("pid",""));


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