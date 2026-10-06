package com.example.kidcommute;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;


public class custom_payment_notification extends BaseAdapter  {
    String[] pid,message,date_time;
    private Context context;
    Button bt;

    public custom_payment_notification(Context applicationContext, String[] pid, String[] message, String[] date_time) {
        this.context = applicationContext;
        this.pid = pid;
        this.message = message;
        this.date_time = date_time;

    }

    @Override
    public int getCount() {
        return message.length;
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
            gridView=inflator.inflate(R.layout.custom_payment_notification,null);//same class name

        }
        else
        {
            gridView=(View)view;

        }
//        ImageView tv7=(ImageView)gridView.findViewById(R.id.imageView2);
        TextView tv1=(TextView)gridView.findViewById(R.id.textView68 );
        TextView tv2=(TextView)gridView.findViewById(R.id.textView71);
//        TextView tv3=(TextView)gridView.findViewById(R.id.textView4);
//        TextView tv4=(TextView)gridView.findViewById(R.id.textView11);
//        TextView tv5=(TextView)gridView.findViewById(R.id.textView8);
//        TextView tv6=(TextView)gridView.findViewById(R.id.textView10);


//        Button tv8=(Button)gridView.findViewById(R.id.button3);





        tv1.setTextColor(Color.BLACK);//color setting
        tv2.setTextColor(Color.BLACK);
//        tv3.setTextColor(Color.BLACK);
//        tv4.setTextColor(Color.BLACK);
//        tv5.setTextColor(Color.BLACK);
//        tv6.setTextColor(Color.BLACK);




        tv1.setText(message[i]);
        tv2.setText(date_time[i]);
//        tv3.setText(dob[i]);
//        tv4.setText(eid[i]);
//        tv5.setText(p[i]);
//        tv6.setText(ph[i]);

//
//        tv8.setTag(nid[i]);

        // Load image using Picasso

//
        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(context);
        // Load image using Picasso

//        String imgUrl = sh.getString("url", "") + photo[i]; //viewing image
//        Picasso.with(context).load(imgUrl).into(tv7);



        // Set OnClickListener for the button
//        tv8.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Retrieve the tid associated with the clicked button
//                String clickedTid = (String) v.getTag();
//
//                // Call your function with the tid
//                handleButtonClick(clickedTid);
//            }
//        });

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