package com.sng.bucbucdeliveryboy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RideCompletionActivity extends AppCompatActivity {

    String post;
    DatabaseReference PendingReference;
    DatabaseReference reference;
    String driverID;
    String deliveryDate,deliveryTime;

    ImageView gifIV;
    Button BTNBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_completion);

        Intent intent = getIntent();
        post=intent.getStringExtra("Post");

        Log.d("TAG", "onCreate: :::::::::::"+post);

        PendingReference= FirebaseDatabase.getInstance().getReference("Drivers");
        reference=FirebaseDatabase.getInstance().getReference("Drivers");
        BTNBack=(Button)findViewById(R.id.backBTN);

        gifIV=(ImageView)findViewById(R.id.iv);

//        final DateFormat date = new SimpleDateFormat("dd-MMMM-yyyy");
//         deliveryDate= date.format(Calendar.getInstance().getTime());
//
//        final DateFormat time = new SimpleDateFormat("hh:mm a");
//        deliveryTime  = time.format(Calendar.getInstance().getTime());
//

//        Picasso.get().load(R.drawable.ddone).into(gifIV);

        //Setting Address in Bottom Bar
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("UserSettings", MODE_PRIVATE);
        driverID=prefs.getString("DB_ID","");
//
//        Map<String, Object> orders = new HashMap<>();
//        orders.put("DeliveredDate",deliveryDate);
//        orders.put("DeliveredTime",deliveryTime);
//
//        PendingReference.child(driverID)
//                .child("Pending").child(post).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                    orders.put("Date",snapshot.child("Date").getValue());
//                    orders.put("OrderID",snapshot.child("OrderID").getValue());
//                    orders.put("ShipmentStatus",snapshot.child("ShipmentStatus").getValue());
//                    orders.put("StoreID",snapshot.child("StoreID").getValue());
//                    orders.put("Time",snapshot.child("Time").getValue());
//
////                reference.child(driverID)
////                            .child("OrderHistory").addValueEventListener(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(@NonNull DataSnapshot dsnapshot) {
////                            for (DataSnapshot dataSnapshot:dsnapshot.getChildren()){
////                                if (dataSnapshot.child("OrderID").getValue().equals(post)){
////                                    PendingReference.child(driverID).child("Pending").child(post).removeValue();
////                                }else {
////                                }
////                            }
////                        }
////
////                        @Override
////                        public void onCancelled(@NonNull DatabaseError error) {
////
////                        }
////                    });
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//
//        reference.child(driverID).child("OrderHistory").child(post).setValue(orders).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()){
//                }else {
//                    Toast.makeText(RideCompletionActivity.this, "Oops! Something went wrong.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        BTNBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PendingReference.child(driverID).child("Pending").child(post).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(RideCompletionActivity.this,HomeActivity.class));
                        finish();
                    }
                });


            }
        });

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        PendingReference.child(driverID).child("Pending").child(post).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                startActivity(new Intent(RideCompletionActivity.this,HomeActivity.class));
                finish();
            }
        });
    }
}