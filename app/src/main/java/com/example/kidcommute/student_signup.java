package com.example.kidcommute;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class student_signup extends AppCompatActivity implements View.OnClickListener {
    ImageView img;
    EditText sname, pickup, classes, division, place, post,pincode,district, uname, psw;
    Button bt;


    final int CAMERA_PIC_REQUEST = 0, GALLERY_CODE = 201;
    private Uri mImageCaptureUri;
    public static String encodedImage = "", path = "";
    public static byte[] byteArray;
    Bitmap bitmap = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_signup);

        img = findViewById(R.id.imageView);

        sname = findViewById(R.id.editTextTextPersonName);
        pickup = findViewById(R.id.editTextTextPersonName2);
        classes = findViewById(R.id.editTextDate);
        division = findViewById(R.id.editTextTextEmailAddress);
        place = findViewById(R.id.editTextTextPersonName4);
        post = findViewById(R.id.editTextTextPersonName5);
        pincode = findViewById(R.id.editTextTextPersonName14);
        district = findViewById(R.id.editTextTextPersonName16);
        uname = findViewById(R.id.editTextTextPersonName6);
        psw = findViewById(R.id.editTextTextPassword);

        bt = findViewById(R.id.button);
        bt.setOnClickListener(this);
        img.setOnClickListener(this);

        // code for external storage permission
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
//                Manifest.permission.READ_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                    Uri.parse("package:" + getPackageName()));
//            finish();
//            startActivity(intent);
//            return;
//        }
    }

    @Override
    public void onClick(View view) {
        if (view == img) {
            selectImageOption();
        } else {
            boolean isValid = true;

            String sname1 = sname.getText().toString().trim();
            String pickup1 = pickup.getText().toString().trim();
            String classes1 = classes.getText().toString().trim();
            String division1 = division.getText().toString().trim();
            String place1 = place.getText().toString().trim();
            String post1 = post.getText().toString().trim();
            String pincode1 = pincode.getText().toString().trim();
            String district1 = district.getText().toString().trim();
            String uname1 = uname.getText().toString().trim();
            String psw1 = psw.getText().toString().trim();


            if (sname1.isEmpty()) {
                sname.setError("Please enter your first name");
                isValid = false;
            } else if (pickup1.isEmpty()) {
                pickup.setError("Please enter your last name");
                isValid = false;

            } else if (classes1.isEmpty()) {
                classes.setError("Please enter your date of birth");
                isValid = false;

            } else if (division1.isEmpty()) {
                division.setError("Please enter your email");
                isValid = false;
            } else if (place1.isEmpty()) {
                place.setError("Please enter your place ");
                isValid = false;

            } else if (place1.isEmpty()) {
                post.setError("Please enter post ");
                isValid = false;
            } else if (pincode1.isEmpty()) {
                pincode.setError("Please enter pincode");
                isValid = false;
            } else if (district1.isEmpty()) {
                district.setError("Please enter district");
                isValid = false;
            } else if (uname1.isEmpty()) {
                uname.setError("Please enter a username");
                isValid = false;

            } else if (psw1.isEmpty()) {
                psw.setError("Please enter a password");
                isValid = false;

            } else if (encodedImage.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                isValid = false;
            }


            if (isValid) {

                SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                String url = sh.getString("url", "") + "usersignup";
                Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT).show();
                VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                        new Response.Listener<NetworkResponse>() {
                            @Override
                            public void onResponse(NetworkResponse response) {
                                try {


                                    JSONObject obj = new JSONObject(new String(response.data));
                                    if (obj.getString("status").equals("ok")) {
                                        Toast.makeText(getApplicationContext(), "Registration success", Toast.LENGTH_SHORT).show();
                                        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                                SharedPreferences.Editor ed = sh.edit();
//                                ed.putString("lid",obj.getString("lid"));
//                                ed.putString("uid",obj.getString("uid"));
//                                ed.commit();
                                        Intent i = new Intent(getApplicationContext(), login.class);
                                        startActivity(i);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Invalid registration", Toast.LENGTH_SHORT).show();
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
                        params.put("student_name", sname1);//passing to python
                        params.put("pickup_point", pickup1);//passing to python
                        params.put("student_class", classes1);//passing to python
                        params.put("student_division", division1);//passing to python
                        params.put("place", place1);//passing to python
                        params.put("post", post1);//passing to python
                        params.put("pincode", pincode1);//passing to python
                        params.put("district", district1);//passing to python
                        params.put("username", uname1);//passing to python
                        params.put("password", psw1);//passing to python


                        return params;
                    }


                    @Override
                    protected Map<String, DataPart> getByteData() {
                        Map<String, DataPart> params = new HashMap<>();
                        long imagename = System.currentTimeMillis();
                        params.put("photo", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                        return params;
                    }
                };

                Volley.newRequestQueue(this).add(volleyMultipartRequest);


                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
            } else {

                Toast.makeText(getApplicationContext(), "Please fill in all fields correctly", Toast.LENGTH_SHORT).show();

            }

        }
    }



    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && null != data) {

            mImageCaptureUri = data.getData();
            System.out.println("Gallery Image URI : " + mImageCaptureUri);
            //   CropingIMG();

            Uri uri = data.getData();
            Log.d("File Uri", "File Uri: " + uri.toString());
            // Get the path
            //String path = null;
            try {
                path = FileUtils.getPath(this, uri);
                Toast.makeText(getApplicationContext(), "path : " + path, Toast.LENGTH_LONG).show();

                File fl = new File(path);
                int ln = (int) fl.length();

                InputStream inputStream = new FileInputStream(fl);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] b = new byte[ln];
                int bytesRead = 0;

                while ((bytesRead = inputStream.read(b)) != -1) {
                    bos.write(b, 0, bytesRead);
                }
                inputStream.close();
                byteArray = bos.toByteArray();

                bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                img.setImageBitmap(bitmap);

                String str = Base64.encodeToString(byteArray, Base64.DEFAULT);
                encodedImage = str;
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == CAMERA_PIC_REQUEST && resultCode == Activity.RESULT_OK) {

            try {
                bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Bitmap thumbs=bitmap;
                thumbs.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                img.setImageBitmap(thumbs);
                byteArray = baos.toByteArray();

                String str = Base64.encodeToString(byteArray, Base64.DEFAULT);
                encodedImage = str;
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    //converting to bitarray
    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void selectImageOption() {

		/*Android 10+ gallery code
        android:requestLegacyExternalStorage="true"*/

        final CharSequence[] items = {"Capture Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(student_signup.this);
        builder.setTitle("Take Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Capture Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, CAMERA_PIC_REQUEST);

                } else if (items[item].equals("Choose from Gallery")) {
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, GALLERY_CODE);

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


}