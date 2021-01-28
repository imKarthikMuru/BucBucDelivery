package com.sng.bucbucdeliveryboy;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class SplashScreen extends AppCompatActivity {
    ImageView SplashIV;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SplashTheme);
//        setContentView(R.layout.activity_splash_screen);

//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//        SplashIV=(ImageView)findViewById(R.id.splashIV);

        builder=new AlertDialog.Builder(SplashScreen.this);
        builder.setTitle("Permission Denied")
                .setMessage("Permission to access the location is denied. You need to go to Settings to enable the permission")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });

        if (ContextCompat.checkSelfPermission(SplashScreen.this,
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Intent intent=new Intent(SplashScreen.this,MainActivity.class);
//                    startActivity(intent);
//                    finish();
//                }
//            },150);

            if (FirebaseAuth.getInstance().getCurrentUser()!=null){
                Intent intent=new Intent(SplashScreen.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }else {
                Intent intent=new Intent(SplashScreen.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }


            return;
        }else {

            CheckPermisiion();
        }



    }

    private void CheckPermisiion() {
        Dexter.withActivity(SplashScreen.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Intent intent=new Intent(SplashScreen.this,MainActivity.class);
//                                    startActivity(intent);
//                                    finish();
//                                }
//                            },150);
                        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
                            Intent intent=new Intent(SplashScreen.this,HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Intent intent=new Intent(SplashScreen.this,LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if (permissionDeniedResponse.isPermanentlyDenied()){
                            builder.show();

                        }else {

                            AlertDialog.Builder builder=new AlertDialog.Builder(SplashScreen.this);
                            builder.setTitle("Permission Denied")
                                    .setMessage("Permission Denied. You must allow this permission to use this App")
                                    .setNegativeButton("Cancel",null)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            CheckPermisiion();
                                        }
                                    }).show();
                            Toast.makeText(getApplicationContext(), "Permission Denied. You must allow this permission to use this App", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(SplashScreen.this,
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if (FirebaseAuth.getInstance().getCurrentUser()!=null){
                Intent intent=new Intent(SplashScreen.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }else {
                Intent intent=new Intent(SplashScreen.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }


}
