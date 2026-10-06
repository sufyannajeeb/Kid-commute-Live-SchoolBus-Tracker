package com.example.kidcommute;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class view_chat_ayaahs extends AppCompatActivity implements AdapterView.OnItemClickListener {
//    TextView cayaah;
    String [] pid,cayaah;
    ListView l1;
    SharedPreferences sh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_chat_ayaahs);

        sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        l1 = findViewById(R.id.li);
        l1.setOnItemClickListener(this);
//        cayaah = findViewById(R.id.li);

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String url = sh.getString("url", "") + "view_chat_ayaah";
        Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            if (obj.getString("status").equals("ok")) {
                                JSONArray js = obj.getJSONArray("q");
//                                JSONArray js = obj.getJSONArray("q");//from python
                                pid = new String[js.length()];
                                cayaah = new String[js.length()];

                                for (int i = 0; i < js.length(); i++) {
                                    JSONObject u = js.getJSONObject(i);
                                    pid[i] = u.getString("id");//dbcolumn name in double quotes
                                    cayaah[i] = u.getString("ayaah_name");
//                                    did[i] = u.getString("did");
                                }
                                ArrayAdapter<String> ar = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, cayaah);
                                l1 .setAdapter(ar);
//                                l1.setAdapter(new custom_notification(getApplicationContext(), pid, cayaah));
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String pids=pid[position];

        SharedPreferences.Editor ed = sh.edit();
        ed.putString("rid",pids);
        ed.commit();

        final CharSequence[] items = {"Chat","Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(view_chat_ayaahs.this);
        // builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Chat"))
                {
                    Intent in = new Intent(getApplicationContext(),ChatHere.class);
                    startActivity(in);
                }
//                else if (items[item].equals("View Map"))
//                {
//                	String url = "http://www.google.com/maps?saddr="+LocationService.lati+""+","+LocationService.logi+""+"&&daddr="+lts+","+lgs;
//                    Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    startActivity(in);
//                }

                else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }

        });
        builder.show();

    }

//    @Override
//    public void onClick(View view) {
//        startActivity(new Intent(getApplicationContext(),bank.class));
//    }
}
