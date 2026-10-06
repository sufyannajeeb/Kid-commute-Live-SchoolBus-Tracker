package com.example.kidcommute;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class custom_view_reply extends BaseAdapter {
    String[] pid, complaints, reply;
    private Context context;
    Button bt;

    public custom_view_reply(Context applicationContext, String[] pid, String[] complaints, String[] reply) {
        this.context = applicationContext;
        this.pid = pid;
        this.complaints = complaints;
        this.reply = reply;

    }

    @Override
    public int getCount() {
        return complaints.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;
        if (view == null) {
            gridView = new View(context);
            //gridView=inflator.inflate(R.layout.customview, null);
            gridView = inflator.inflate(R.layout.activity_custom_view_reply, null);//same class name

        } else {
            gridView = (View) view;

        }
        TextView tv1 = (TextView) gridView.findViewById(R.id.textView74);
        TextView tv2 = (TextView) gridView.findViewById(R.id.textView76);

        tv1.setTextColor(Color.BLACK);//color setting
        tv2.setTextColor(Color.BLACK);

        tv1.setText(complaints[i]);
        tv2.setText(reply[i]);

        SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(context);

return gridView;
    }
    private void handleButtonClick(String cid) {
        Toast.makeText(context, cid, Toast.LENGTH_SHORT).show();
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor ed = sh.edit();
        ed.putString("pid",cid);
        ed.commit();
        Intent u=new Intent(context,parent_home.class);
        u.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(u);
    }
}
