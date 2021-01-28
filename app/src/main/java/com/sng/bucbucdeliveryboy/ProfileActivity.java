package com.sng.bucbucdeliveryboy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    //todo TotalEarnings,Ratings

    String post;

    CircleImageView DP;
    TextView Name,Mobile,Address,UserID,BikeModel,BikeNo,Earnings,Pending,Completed,Ratings;

    DatabaseReference reference;

    String name,mob,address,userID,bikemodel,bikeno,earnings,pending,completed,rating,dp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent=getIntent();
        post=intent.getStringExtra("ID");

        if (post==null) {

            SharedPreferences prefs = getApplicationContext().getSharedPreferences("UserSettings", MODE_PRIVATE);
            post = prefs.getString("DB_ID", "");
        }

        DP=(CircleImageView)findViewById(R.id.dp);
        Name=findViewById(R.id.name);
        Mobile=findViewById(R.id.mob);
        Address=findViewById(R.id.address);
        UserID=findViewById(R.id.userId);
        BikeModel=findViewById(R.id.bikemodel);
        BikeNo=findViewById(R.id.bikeNO);
        Earnings=findViewById(R.id.totalEarn);
        Ratings=findViewById(R.id.totalRating);
        Pending=findViewById(R.id.pendingOrder);
        Completed=findViewById(R.id.completedOrder);

        reference= FirebaseDatabase.getInstance().getReference("Drivers").child(post);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                name= String.valueOf(snapshot.child("Name").getValue());
                dp=String.valueOf(snapshot.child("ProfilePic").getValue());
                mob=String.valueOf(snapshot.child("Mobile").getValue());
                address=String.valueOf(snapshot.child("Address").getValue());
                userID=String.valueOf(snapshot.child("UserID").getValue());
                bikemodel=String.valueOf(snapshot.child("BikeModel").getValue());
                bikeno=String.valueOf(snapshot.child("BikeNo").getValue());

                Name.setText(name);
                Mobile.setText(mob);
                Address.setText(address);
                BikeModel.setText(bikemodel);
                BikeNo.setText(bikeno);
                UserID.setText("@"+userID);

                Picasso.get().load(dp).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.ic_buc_buc_logo_final)
                        .into(DP, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(dp).into(DP);
                            }
                        });

                if (snapshot.child("Pending").exists()){
                    Pending.setText("1");
                }else {
                    Pending.setText("No");
                }

                Completed.setText(String.valueOf(snapshot.child("OrderHistory").getChildrenCount()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void Finish(View view) {
        finish();
    }
}