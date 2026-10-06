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

    public class dmessage_to_admin extends AppCompatActivity implements View.OnClickListener {
        EditText send1;
        Button b5;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_dmessage_to_admin);
            send1 = findViewById(R.id.editTextTextPersonName4);
            b5 = findViewById(R.id.button4);
            b5.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {

                String message  =send1.getText().toString();

                    SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    String url = sh.getString("url", "") + "dmessage_to_admin";
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
                                            Intent i = new Intent(getApplicationContext(), driver_home.class);
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
                            params.put("did", sh.getString("uid",""));//passing to python
                            params.put("message", message);//passing to python


                            return params;
                        }


                        @Override
                        protected Map<String, DataPart> getByteData() {
                            Map<String, DataPart> params = new HashMap<>();
                            long imagename = System.currentTimeMillis();
    //                        params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                            return params;
                        }
                    };

                    Volley.newRequestQueue(this).add(volleyMultipartRequest);


                }
            }
    //        }


    //}
