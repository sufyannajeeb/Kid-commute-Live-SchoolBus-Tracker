package com.example.kidcommute;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
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

public class AndroidBarcodeQrExample extends Activity
{
	public static String vals;
	Button b1;
	/** Called when the activity is first created. */
	String method="getslotidandlocid";
	String soapaction="http://tempuri.org/getslotidandlocid";
	SharedPreferences sh;
	static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
	  public static TextToSpeech t1;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mains);
		sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//		b1=(Button)findViewById(R.id.upload);
//		b1.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				startActivity(new Intent(getApplicationContext(),Uploadimage.class));
//			}
//		});
	}

	public void scanBar(View v) {
		try {
			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException anfe) {
			showDialog(AndroidBarcodeQrExample.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
	}

	public void scanQR(View v) {
		try {
			Intent intent = new Intent(ACTION_SCAN);
			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
			startActivityForResult(intent, 0);
		} catch (ActivityNotFoundException anfe) {
			showDialog(AndroidBarcodeQrExample.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
		}
	}
//	public void Uploadfiles(View v) {
//		startActivity(new Intent(getApplicationContext(),Uploadimage.class));
//	}

	private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
		AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
		downloadDialog.setTitle(title);
		downloadDialog.setMessage(message);
		downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				try {
					act.startActivity(intent);
				} catch (ActivityNotFoundException anfe) {

				}
			}
		});
		downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) 
			{
			}
		});
		return downloadDialog.show();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

//				Toast toast = Toast.makeText(this, "Content:" + contents + " Format:" + format, Toast.LENGTH_LONG);
//				toast.show();

				vals=contents;
				SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				String url = sh.getString("url", "") + "checkin";
				VolleyMultipartRequest volleyMultipartRequest;
				volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
						new Response.Listener<NetworkResponse>() {
							@Override
							public void onResponse(NetworkResponse response) {
								try {


									JSONObject obj = new JSONObject(new String(response.data));

									if (obj.getString("status").equals("ok")) {
										Toast.makeText(getApplicationContext(), " Payment Success", Toast.LENGTH_SHORT).show();
										Intent i = new Intent(getApplicationContext(), ayaah_home.class);
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
						params.put("sid", contents);//passing to python
						return params;
					}
				};

				Volley.newRequestQueue(this).add(volleyMultipartRequest);



//				SharedPreferences.Editor e=sh.edit();
//				e.putString("valsss",vals);
//				e.commit();
//				startActivity(new Intent(getApplicationContext(), User_view_purchased_item_details.class));
//				JsonReq JR=new JsonReq();
//				JR.json_response=(JsonResponse) AndroidBarcodeQrExample.this;
//				String q = "/verifynewfiles?fid="+sh.getString("fid","")+"&content="+vals;
//				q=q.replace(" ","%20");
//				JR.execute(q);


//				if(sh.getString("ft","").equalsIgnoreCase("Type3")) {
//					startActivity(new Intent(getApplicationContext(), ViewImage.class));
//				}
//				if(sh.getString("ft","").equalsIgnoreCase("Type4")) {
//					startActivity(new Intent(getApplicationContext(), .class));
//				}


			}
		}
	}

//	@Override
//	public void response(JSONObject jo) {
//		try {
//
//			String method=jo.getString("method");
//			if(method.equalsIgnoreCase("verifynewfiles")) {
//
//				String status = jo.getString("status");
//				Log.d("pearl", status);
//
//
//				if (status.equalsIgnoreCase("success")) {
//					Toast.makeText(getApplicationContext(), "Verified Successfully", Toast.LENGTH_LONG).show();
//
//					if(sh.getString("ft","").equalsIgnoreCase("Type3")) {
//						startActivity(new Intent(getApplicationContext(), ViewImage.class));
//					}
//					if(sh.getString("ft","").equalsIgnoreCase("Type4")) {
//						Random random = new Random();
//						String generatedPassword = String.format("%04d", random.nextInt(10000));
//
//						Log.d("MyApp", "Generated Password : " + generatedPassword);
//
//						//Getting intent and PendingIntent instance
//						Intent intent=new Intent(getApplicationContext(),MainActivity.class);
//						PendingIntent pi=PendingIntent.getActivity(getApplicationContext(), 0, intent,0);
//
////Get the SmsManager instance and call the sendTextMessage method to send message
//						SmsManager sms=SmsManager.getDefault();
//						sms.sendTextMessage("8281940635", null, "Your OTP is "+generatedPassword+"", pi,null);
//						SharedPreferences.Editor e=sh.edit();
//						e.putString("otp",generatedPassword+"");
//						e.commit();
//						startActivity(new Intent(getApplicationContext(), VerifyOTP.class));
//					}
//				} else {
//
//					Toast.makeText(getApplicationContext(), " Enterd key is not correct!!", Toast.LENGTH_LONG).show();
//					startActivity(new Intent(getApplicationContext(), Employee_view_uploaded_files.class));
//				}
//			}
//
//
//		}
//
//		catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
//		}
//	}
	public void onBackPressed()
	{
		// TODO Auto-generated method stub
		super.onBackPressed();
		startActivity(new Intent(getApplicationContext(),ayaah_home.class));
	}
}