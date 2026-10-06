package com.example.kidcommute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class login extends AppCompatActivity implements View.OnClickListener {
    EditText u_id,p_id;
    Button btn1;
    TextView signup;
    public static String lid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        u_id = findViewById(R.id.editTextTextPersonName2);
        p_id = findViewById(R.id.editTextTextPersonName3);
        signup = findViewById(R.id.textSignup27);
        btn1 = findViewById(R.id.button);
        btn1.setOnClickListener(this);
        signup.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        if (view == btn1){
            String username = u_id.getText().toString();
            String password = p_id.getText().toString();

            if (username.length()<1){
                u_id.setError("");
            }
            else if (password.length()<1){
                p_id.setError("");
            }
            else{
                SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String url= sh.getString("url","")+"login_and";
                Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
                VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                        new Response.Listener<NetworkResponse>(){
                            @Override
                            public void onResponse(NetworkResponse response) {
                                try {



                                    JSONObject obj = new JSONObject(new String(response.data));

                                    if(obj.getString("status").equals("ok")){
                                        Toast.makeText(getApplicationContext(), "login success", Toast.LENGTH_SHORT).show();
                                        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


                                        String usertype = obj.getString("usertype");
                                        Toast.makeText(getApplicationContext(),usertype, Toast.LENGTH_SHORT).show();
                                        lid=obj.getString("lid");
                                        if ("driver".equals((usertype))) {
                                            SharedPreferences.Editor ed = sh.edit();
                                            ed.putString("lid",obj.getString("lid"));
                                            ed.putString("uid", obj.getString("uid"));
                                            ed.putString("bid",obj.getString("bid"));
                                            ed.commit();

                                            Intent i = new Intent(getApplicationContext(), driver_home.class);
                                            startActivity(i);
                                        }else if("ayaah".equalsIgnoreCase((usertype))) {
//                                            Toast.makeText(getApplicationContext(), "Helooooooooo", Toast.LENGTH_SHORT).show();

                                            SharedPreferences.Editor ed = sh.edit();
                                            ed.putString("lid",obj.getString("lid"));
                                            ed.putString("aid", obj.getString("aid"));
                                            ed.putString("sid", obj.getString("lid"));
                                            ed.putString("bid",obj.getString("bid"));
                                            ed.commit();

                                            Intent i = new Intent(getApplicationContext(), ayaah_home.class);
                                            startActivity(i);
                                        }else if("parent".equalsIgnoreCase((usertype))){
                                            SharedPreferences.Editor ed = sh.edit();
                                            ed.putString("lid",obj.getString("lid"));
                                            ed.putString("pid", obj.getString("pid"));
                                            ed.putString("sid", obj.getString("lid"));
                                            ed.putString("bid",obj.getString("bid"));
                                            ed.commit();

                                            Intent i = new Intent(getApplicationContext(), parent_home.class);
                                            startActivity(i);

                                        }
 
                                  //      Intent ii=new Intent(getApplicationContext(),LocationService.class);startService(ii);

                                    }else{
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
                        SharedPreferences o = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        params.put("username", username);//passing to python
                        params.put("password", password);//passing to python

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
        }else{
            Intent i = new Intent(getApplicationContext(),student_signup.class);
            startActivity(i);
        }

    }
}