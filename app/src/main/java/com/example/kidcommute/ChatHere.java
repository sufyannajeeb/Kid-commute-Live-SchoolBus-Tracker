package com.example.kidcommute;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

//import org.ksoap2.SoapEnvelope;
//import org.ksoap2.serialization.SoapObject;
//import org.ksoap2.serialization.SoapSerializationEnvelope;
//import org.ksoap2.transport.HttpTransportSE;

import android.R.string;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class ChatHere extends Activity implements JsonResponse
{
	EditText ed1;
	ImageView b1;
	String chat;
	ListView l1;
	ImageView iv10;
	String method1="",method2="",method3="";
	String namespace="http://Dbcon/";
	String url="";
	String[] aid,aname,r_id1,msg1;
	String[] msgid,s_id,r_id,message,date,re;
	SharedPreferences sh;

	String aid1,aname1,msg;
	String soapaction="";
	
	Timer timer;
	TimerTask timerTask;
	final Handler handler = new Handler();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_here);
		 sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		url = sh.getString("url", "");
		ed1=(EditText)findViewById(R.id.editText1);
		l1=(ListView)findViewById(R.id.listView1);
		b1=(ImageView)findViewById(R.id.imageView1);
		//Toast.makeText(getApplicationContext(), "hii1", Toast.LENGTH_SHORT).show();	
		
		startTimer();
		getChats();
		
		b1.setOnClickListener(new View.OnClickListener() 
		{
			public void onClick(View arg0)
			{
				chat=ed1.getText().toString();
				if(chat.equalsIgnoreCase(""))
				{
					ed1.setError("Empty Message ");
					ed1.setFocusable(true);
				}
				else
				{
					JsonReq jr= new JsonReq();
					jr.json_response=(JsonResponse) ChatHere.this;
					String q="chat/"+chat+"/"+sh.getString("sid","")+"/"+sh.getString("rid","");
					q.replace("", "%20");
					jr.execute(q);
//					try
//					{
//						
//						
//						method2="chat";
//						soapaction =namespace+method2;
//						SoapObject sop=new SoapObject(namespace,method2);
//						sop.addProperty("msg",ed1.getText().toString());
//						sop.addProperty("from_id","2"/*Login.logid*/);
//						sop.addProperty("to_id",UsersList.aid1);
//						SoapSerializationEnvelope env=new SoapSerializationEnvelope(SoapEnvelope.VER11);
//						env.setOutputSoapObject(sop);
//						HttpTransportSE http=new HttpTransportSE(url);
//						http.call(soapaction, env);
//						String result=env.getResponse().toString();
//
//						if(result.equalsIgnoreCase("ok"))
//						{
//				//			Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
////							startActivity(new Intent(getApplicationContext(),ChatHere.class));
//							getChats();
//							ed1.setText("");
//						}
////						else
////						{
////							Toast.makeText(getApplicationContext(), "Failed..."+result, Toast.LENGTH_SHORT).show();
//////							startActivity(new Intent(getApplicationContext(),ChatHere.class));
////							getChats();
////						}
//					}
//					catch(Exception e)
//					{
//						Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
//					}	
				} 
			}
		});
	}
	
	void startTimer() {
		timer = new Timer();
		initializeTimerTask();
		timer.schedule(timerTask, 0, 3000);
	}
	
	void initializeTimerTask() {
		timerTask = new TimerTask() {
			
			@Override
			public void run() {
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						getChats();
					}
				});
			}
		};
	}
	
	void getChats() {
		
		JsonReq JR= new JsonReq();
		JR.json_response=(JsonResponse)ChatHere.this;
		String q="get_chat/"+sh.getString("sid","")+"/"+sh.getString("rid","");
		JR.execute(q);
//		try
//		{	
//			method3="get_chat";
//			soapaction=namespace+method3;
//			SoapObject sop=new SoapObject(namespace,method3);
//			sop.addProperty("from_id","2" /*Login.logid*/);
//			sop.addProperty("to_id",UsersList.aid1);
//			//sop.addProperty("message",msg1);
//			SoapSerializationEnvelope env=new SoapSerializationEnvelope(SoapEnvelope.VER11);
//			env.setOutputSoapObject(sop);
//			HttpTransportSE http=new HttpTransportSE(url);
//			http.call(soapaction, env);
//
//			String result=env.getResponse().toString();
////			Toast.makeText(getApplicationContext(), result , Toast.LENGTH_SHORT).show();
//			if(result.equalsIgnoreCase("failed"))
//			{
//				Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
//			}
//			else
//			{
//				String[] temp=result.split("\\$");
//				if(temp.length>0)
//				{
//					s_id=new String[temp.length];
//					r_id=new String[temp.length];
//					message=new String[temp.length];
//					date=new String[temp.length];
//				
//					for(int i=0;i<temp.length;i++)
//					{
//						String[] temp1=temp[i].split("\\#");
//						s_id[i]=temp1[0];
//						r_id[i]=temp1[1];
//						message[i]=temp1[2];
//						date[i]=temp1[3];
//					
//					}
//					l1.setAdapter(new Customchat(this, message, s_id,date));
//					//Toast.makeText(getApplicationContext(),result, Toast.LENGTH_SHORT).show();
//				}
//			}
//		}
//		catch(Exception e)
//		{
//			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
//		}
	}

	@Override
	public void response(JSONObject jo) {
		// TODO Auto-generated method stub
		try{
			String method=jo.getString("method");
			if(method.equalsIgnoreCase("get_chat"))
			{
		
		try{
			String status=jo.getString("status");
			if(status.equalsIgnoreCase("ok"))
			{
				JSONArray ja=(JSONArray)jo.getJSONArray("q");
				s_id=new String[ja.length()];
				r_id=new String[ja.length()];
				message= new String[ja.length()];
				date= new String[ja.length()];
//				details=new String[ja.length()];
				
				
				
				for(int i=0;i<ja.length();i++)
				{
					s_id[i]=ja.getJSONObject(i).getString("sid");
					r_id[i]=ja.getJSONObject(i).getString("rid");
					message[i]=ja.getJSONObject(i).getString("message");
//					date[i]=ja.getJSONObject(i).getString("date_time");
					date[i]="1";
//					details[i]="Complaint : "+complaint[i]+"\nReply : "+reply[i]+"\nDate : "+date[i];
					
				}
				//driver_list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),R.layout.spin,ConfirmRide.name_s));
				l1.setAdapter(new Customchat(this, message, s_id,date));
//				l1.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,details));
			}
			else
			{
				Toast.makeText(getApplicationContext(), "No Data", Toast.LENGTH_LONG).show();
			}
		}
		catch(Exception e){
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "haii"+e, Toast.LENGTH_LONG).show();
		}
		
	}
			if(method.equalsIgnoreCase("chat"))
			{
				try
				{
					String status=jo.getString("status");
					Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG).show();
					if(status.equalsIgnoreCase("ok"))
					{
						Toast.makeText(getApplicationContext(), "Chat Send Successfully", Toast.LENGTH_LONG).show();
						startActivity(new Intent(getApplicationContext(),ChatHere.class));
//						ed1.setText("");
					}
					
					else
					{
						Toast.makeText(getApplicationContext(), "Not Successfull", Toast.LENGTH_LONG).show();
					}
				
				}
				catch(Exception e)
				{
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "Hai"+e, Toast.LENGTH_LONG).show();
				}
			}
}
	catch(Exception e){
		e.printStackTrace();
		Toast.makeText(getApplicationContext(), "haii"+e, Toast.LENGTH_LONG).show();
	}
		
	}

}

	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu)
//	{
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.chat_here, menu);
//		return true;
//	}

