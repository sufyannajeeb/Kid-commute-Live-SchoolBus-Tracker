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
import android.widget.TextView;
import android.widget.Toast;

public class custom_vehicle_status extends BaseAdapter {
    String[] pid,status,date;
    private Context context;

    public custom_vehicle_status(Context applicationContext,String[] status, String[] date,String[] pid) {
        this.context = applicationContext;
        this.date = date;
        this.status = status;
        this.pid = pid;

    }

    @Override
    public int getCount() {
        return status.length;
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
        LayoutInflater inflator=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;
        if(view==null)
        {
            gridView=new View(context);
            //gridView=inflator.inflate(R.layout.customview, null);
            gridView=inflator.inflate(R.layout.activity_custom_vehicle_status,null);//same class name

        }
        else
        {
            gridView=(View)view;

        }
//        ImageView tv7=(ImageView)gridView.findViewById(R.id.imageView2);
        TextView tv1=(TextView)gridView.findViewById(R.id.textView88 );
        TextView tv2=(TextView)gridView.findViewById(R.id.textView90 );

//        TextView tv3=(TextView)gridView.findViewById(R.id.textView4);
//        TextView tv4=(TextView)gridView.findViewById(R.id.textView11);
//        TextView tv5=(TextView)gridView.findViewById(R.id.textView8);
//        TextView tv6=(TextView)gridView.findViewById(R.id.textView10);


//        Button tv8=(Button)gridView.findViewById(R.id.button3);





        tv1.setTextColor(Color.BLACK);//color setting
        tv2.setTextColor(Color.BLACK);//color setting

//        tv3.setTextColor(Color.BLACK);
//        tv4.setTextColor(Color.BLACK);
//        tv5.setTextColor(Color.BLACK);
//        tv6.setTextColor(Color.BLACK);




        tv1.setText(status[i]);
        tv1.setText(date[i]);



//
        SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(context);
        // Load image using Picasso

//        String imgUrl = sh.getString("url", "") + photo[i]; //viewing image
//        Picasso.with(context).load(imgUrl).into(tv7);




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

