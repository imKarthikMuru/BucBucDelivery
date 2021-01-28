package com.sng.bucbucdeliveryboy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    EditText UserId, Mobile;
    Button LoginBTN;

    String userID, mobile;
    DatabaseReference reference;

    EditText OTP_Dialog_ET;
    Button SubmitBTN;

    AlertDialog.Builder dialogBuilder;

    FirebaseAuth mAuth;
    String verificationID;
    String uid;
    String postID;
    AlertDialog alertDialog;

    LoadingDialog loadingDialog = LoadingDialog.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginBTN = (Button) findViewById(R.id.BTNLogin);
        UserId = (EditText) findViewById(R.id.userId);
        Mobile = (EditText) findViewById(R.id.mobile);

        mAuth=FirebaseAuth.getInstance();

        LoginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userID = UserId.getText().toString().trim();
                mobile = Mobile.getText().toString().trim();

                if (userID.isEmpty() && mobile.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Enter all fields.", Toast.LENGTH_SHORT).show();
                } else if (userID.isEmpty()) {
                    UserId.setError("Required.");
                } else if (mobile.isEmpty()) {
                    Mobile.setError("Required.");
                } else if (mobile.length() < 10) {
                    Mobile.setError("must be 10 digits.");
                } else {
                    CheckUserRegistered(userID, "+91"+mobile);
                }
            }
        });

        dialogBuilder=new AlertDialog.Builder(this);

        LayoutInflater inflater=this.getLayoutInflater();
        View dialogView=inflater.inflate(R.layout.otp_dialogbox,null);

        OTP_Dialog_ET=(EditText)dialogView.findViewById(R.id.otpET);
        SubmitBTN=(Button)dialogView.findViewById(R.id.submit);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);

        alertDialog = dialogBuilder.create();
        alertDialog.setCancelable(false);

        SubmitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code=OTP_Dialog_ET.getText().toString().trim();

                if (code.isEmpty()||code.length()<6){
                    OTP_Dialog_ET.setError("Enter code...");
                    OTP_Dialog_ET.requestFocus();
                    return;
                }
                verifyCode(code);
            }
        });
    }

    private void CheckUserRegistered(String userID, String mobile) {

        loadingDialog.ShowProgress(LoginActivity.this, "Hold up! Checking.", false);

        reference = FirebaseDatabase.getInstance().getReference("Drivers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("UserID").getValue().equals(userID) && dataSnapshot.child("Mobile").getValue().equals(mobile)) {

//                        Toast.makeText(LoginActivity.this, "Exists", Toast.LENGTH_SHORT).show();

                        postID=dataSnapshot.getKey();

                        sendOTPcode(mobile);

                        alertDialog.show();

                    } else if (dataSnapshot.child("UserID").getValue().equals(userID) && !dataSnapshot.child("Mobile").getValue().equals(mobile)) {
                        Mobile.setError("Incorrect Mobile number.");
                    } else {
                        Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }

                    Log.d("TAG", "onDataChange: ::::::::::::::" + dataSnapshot.child("Mobile").getValue());
                    Log.d("TAG", "onComplete::::::::::::::: "+postID);
                    loadingDialog.hideProgress();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void sendOTPcode(String mobile) {

       PhoneAuthOptions options= PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(mobile)
                .setTimeout(60L,TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallback)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallback=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code=phoneAuthCredential.getSmsCode();
            if (code!=null){
                OTP_Dialog_ET.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationID=s;
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyCode(String code) {

        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationID,code);
        signIn(credential);

    }

    private void signIn(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                alertDialog.dismiss();

                if (task.isSuccessful()){

                    Toast.makeText(LoginActivity.this, ""+postID, Toast.LENGTH_SHORT).show();
                    
                    Map<String, Object> USer=new HashMap<>();
                    USer.put("UID",FirebaseAuth.getInstance().getCurrentUser().getUid());
                    
                    FirebaseDatabase.getInstance().getReference("Drivers").child(postID)
                            .updateChildren(USer);

                    //todo SHared preferences

                    FirebaseDatabase.getInstance().getReference("Drivers").child(postID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            //public static final String MY_PREFS_NAME = "MyPrefsFile";
                            SharedPreferences.Editor editor = getSharedPreferences("UserSettings", MODE_PRIVATE).edit();
                            editor.putString("Name", String.valueOf(snapshot.child("Name").getValue()));
                            editor.putString("Address", String.valueOf(snapshot.child("Address").getValue()));
                            editor.putString("Mobile", String.valueOf(snapshot.child("Mobile").getValue()));
                            editor.putString("DB_ID", postID);
                            editor.putString("UserID",userID);
                            editor.putString("UID",FirebaseAuth.getInstance().getCurrentUser().getUid());
                            editor.putString("BikeNo",String.valueOf(snapshot.child("BikeNo").getValue()));
                            editor.putString("BikeModel",String.valueOf(snapshot.child("BikeModel").getValue()));
                            editor.apply();

                            startActivity(new Intent(LoginActivity.this,HomeActivity.class));

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Log.d("TAG", "onComplete::::::::::::::: "+postID);
                }else {
                    Toast.makeText(LoginActivity.this, "Error on login.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            startActivity(new Intent(LoginActivity.this,HomeActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            startActivity(new Intent(LoginActivity.this,HomeActivity.class));
        }

    }
}