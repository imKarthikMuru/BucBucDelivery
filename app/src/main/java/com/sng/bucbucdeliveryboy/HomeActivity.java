package com.sng.bucbucdeliveryboy;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sng.bucbucdeliveryboy.modelClass.OrdersModelsClass;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseRecyclerAdapter adapter;
    DatabaseReference driverRef,customerRef,storeRef;
    FirebaseRecyclerOptions<OrdersModelsClass> options;
    LinearLayoutManager manager;
    String uid;
    String userToken;

    SwitchCompat DutySwitch;

    GPSTracker gpsTracker;

    Map<String, Object> DriverStatus=new HashMap<>();

    LoadingDialog loadingDialog=LoadingDialog.getInstance();

    String postID,storeID,orderID,date,time;
    SharedPreferences prefs;

    Toolbar toolbar;

    AlertDialog alertDialog;

    @Override
    protected void onStart() {
        super.onStart();

        uid=FirebaseAuth.getInstance().getCurrentUser().getUid();

        prefs = getApplicationContext().getSharedPreferences("UserSettings", MODE_PRIVATE);
        postID=prefs.getString("DB_ID","");

        String s="delivery";
        FirebaseMessaging.getInstance().subscribeToTopic(s);
        FirebaseMessaging.getInstance().subscribeToTopic("BucBuc");

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        Log.d("TAG", "onComplete: :::::::::::::"+task.getResult().getToken());

                        userToken=task.getResult().getToken();

                        Map<String, Object> token = new HashMap<>();
                        token.put("PushToken",userToken);

                        FirebaseDatabase.getInstance().getReference("Drivers").child(postID)
                                .updateChildren(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("TAG", "onComplete: :::::::::::Token Updated");
//                                Toast.makeText(HomeActivity.this, "Loaded Successfully.!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

        FirebaseDatabase.getInstance().getReference("Drivers").child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //public static final String MY_PREFS_NAME = "MyPrefsFile";
                SharedPreferences.Editor editor = getSharedPreferences("UserSettings", MODE_PRIVATE).edit();
                editor.putString("Name", String.valueOf(snapshot.child("Name").getValue()));
                editor.putString("Address", String.valueOf(snapshot.child("Address").getValue()));
                editor.putString("Mobile", String.valueOf(snapshot.child("Mobile").getValue()));
                editor.putString("DB_ID", postID);
                editor.putString("UserID",uid);
                editor.putString("UID",FirebaseAuth.getInstance().getCurrentUser().getUid());
                editor.putString("BikeNo",String.valueOf(snapshot.child("BikeNo").getValue()));
                editor.putString("BikeModel",String.valueOf(snapshot.child("BikeModel").getValue()));
                editor.apply();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView=(RecyclerView)findViewById(R.id.orderRecycler);
        DutySwitch=(SwitchCompat)findViewById(R.id.dutySwitch);
        toolbar=(Toolbar)findViewById(R.id.toolbarHome);
        toolbar.inflateMenu(R.menu.main2);

        alertDialog=new AlertDialog.Builder(HomeActivity.this)
                .setCancelable(true)
                .setIcon(R.drawable.logotext)
                .setMessage("You want to Logout for now.")
                .setTitle("Are you sure?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadingDialog.ShowProgress(HomeActivity.this,"Logging out..",false);
                        AuthUI.getInstance()
                                .signOut(HomeActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(HomeActivity.this,LoginActivity.class));
                                    Toast.makeText(HomeActivity.this, "Successfully signed out.", Toast.LENGTH_SHORT).show();
                                }
                                loadingDialog.hideProgress();
                                dialog.dismiss();
                            }
                        });
                    }
                }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.rideHistory:
                      startActivity(new Intent(HomeActivity.this,RideHistoryActivity.class));
                      break;
                    case R.id.profile:
                        startActivity(new Intent(HomeActivity.this,ProfileActivity.class).putExtra("ID",postID));
                        break;
                    case R.id.logout:
                        alertDialog.show();
                        break;
                }

                return false;
            }
        });

        manager=new LinearLayoutManager(this,RecyclerView.VERTICAL,true);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);

        loadingDialog.ShowProgress(HomeActivity.this,"Loading",false);

        //Setting Address in Bottom Bar
        prefs = getApplicationContext().getSharedPreferences("UserSettings", MODE_PRIVATE);
        postID=prefs.getString("DB_ID","");

        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

        driverRef= FirebaseDatabase.getInstance().getReference("Drivers").child(postID);

        storeRef=FirebaseDatabase.getInstance().getReference("Stores");
        customerRef=FirebaseDatabase.getInstance().getReference("Users");

        try {
            gpsTracker=new GPSTracker(HomeActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        driverRef.child("Pending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    Toast.makeText(HomeActivity.this, "Cannot find any Pending Orders", Toast.LENGTH_SHORT).show();
                }else {
                    Log.d("TAG", "onDataChange: :::::::::::::Home Page Loaded Successfully.");
                }
                loadingDialog.hideProgress();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        driverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Status").exists()){
                    if (String.valueOf(snapshot.child("Status").getValue()).equals("OnDuty")){
                        DutySwitch.setChecked(true);
                        DutySwitch.setText("On Duty");
                    }else {
                        DutySwitch.setChecked(false);
                        DutySwitch.setText("Off Duty");
                    }
                }else {
                    DutySwitch.setChecked(false);
                    DutySwitch.setText("Off Duty");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DutySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){

                    DriverStatus.put("Status","OnDuty");
                    DriverStatus.put("Lat",gpsTracker.latitude);
                    DriverStatus.put("Lng",gpsTracker.longitude);

                }else {

                    DriverStatus.put("Status","OffDuty");
                    DriverStatus.put("Lat",0);
                    DriverStatus.put("Lng",0);

                }

                driverRef.updateChildren(DriverStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loadingDialog.ShowProgress(HomeActivity.this,"Hold tight!",false);
                        if (task.isSuccessful()){
//                            DutySwitch.setChecked(true);
//                            DutySwitch.setText("On Duty");
//                            Toast.makeText(HomeActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                            loadingDialog.hideProgress();
                        }else {
//                            DutySwitch.setChecked(false);
//                            DutySwitch.setText("Off Duty");
                            Toast.makeText(HomeActivity.this, "Oh snap ! Something went wrong.", Toast.LENGTH_SHORT).show();
                            loadingDialog.hideProgress();
                        }
                    }
                });
            }
        });


        options=new FirebaseRecyclerOptions.Builder<OrdersModelsClass>()
                .setQuery(driverRef.child("Pending"),OrdersModelsClass.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<OrdersModelsClass, OrdersViewHolder>(options) {
            @NonNull
            @Override
            public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new OrdersViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_recycler,parent,false));
            }

            @Override
            protected void onBindViewHolder(@NonNull OrdersViewHolder holder, int position, @NonNull OrdersModelsClass model) {



                final DatabaseReference ref=getRef(position);
                final String post=ref.getKey();

                holder.CustomeName.setText(model.getName());
                holder.AreaNameTV.setText(model.getAddress());
                holder.RateTV.setText("₹"+model.getToPay());
                holder.RateTV.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

                final List<String> list=new ArrayList<>();
                final StringBuilder builder = new StringBuilder();

                FirebaseDatabase.getInstance().getReference("Drivers").child(postID).child("OrderHistory").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(post).exists()){
                            driverRef.child("Pending").child(post).removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                driverRef.child("Pending").child(post).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        storeID= String.valueOf(snapshot.child("StoreID").getValue());
                        orderID=String.valueOf(snapshot.child("OrderID").getValue());

                        //Changed Store Details instead of Customer Details

                        holder.OrderedDateTime.setText(String.format("%s  %s", snapshot.child("Date").getValue(), snapshot.child("Time").getValue()));

                        storeRef.child(storeID).child("StoreDetails").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                holder.StoreName.setText(String.valueOf(snapshot.child("StoreName").getValue()));
                                holder.StoreAddress.setText(String.valueOf(snapshot.child("Address").getValue()));
//                                holder.RateTV.setText("₹"+String.valueOf(snapshot.child("ToPay").getValue()));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        storeRef.child(storeID).child("Orders").child(orderID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                holder.CustomeName.setText(String.valueOf(snapshot.child("Name").getValue()));
                                holder.AreaNameTV.setText(String.valueOf(snapshot.child("Address").getValue()));
//               
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        storeRef.child(storeID).child("Orders").child(orderID).child("Products").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot data:dataSnapshot.getChildren()){

                                    list.add(data.child("ProductName").getValue()+" x "+data.child("Quantity").getValue());
                                    Log.d("TAG", "onBindViewHolder: ::::::::::::"+list.size()+"::::::::;"+list.toString());

                                }

                                for (int i=0;i<list.size();i++){
                                    if (i < list.size() - 1) {
                                        builder.append(list.get(i).toString()+", ");
                                    } else {
                                        builder.append(list.get(i).toString());
                                    }

                                }

                                holder.OrderDetailsTV.setText(builder.toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Log.d("TAG", "onBindViewHolder: ::::::::::::::::::"+storeID+":::::::::::"+orderID);

                holder.Show.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(HomeActivity.this,OrderDetailsActivity.class)
                                .putExtra("PostKey",post));
//                        Toast.makeText(HomeActivity.this, ""+post, Toast.LENGTH_SHORT).show();
                    }
                });

//                holder.OrderedDateTime.setText(model.getOrderedDate()+" "+model.getTime());

            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }


    public class OrdersViewHolder extends RecyclerView.ViewHolder {

        TextView CustomeName,AreaNameTV,RateTV,OrderDetailsTV,OrderedDateTime,StoreName,StoreAddress;
        Button Show;

        public OrdersViewHolder(@NonNull View itemView) {
            super(itemView);

            CustomeName=itemView.findViewById(R.id.customerName);
            AreaNameTV=itemView.findViewById(R.id.areaName);
            RateTV=itemView.findViewById(R.id.price);
            OrderDetailsTV=itemView.findViewById(R.id.orderDetails);
            OrderedDateTime=itemView.findViewById(R.id.orderDate);
            Show=itemView.findViewById(R.id.show);
            StoreName=itemView.findViewById(R.id.storeName);
            StoreAddress=itemView.findViewById(R.id.storeAddress);


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TAG", "onResume: ::::::::::::::::::::");

        if (adapter!=null){

            adapter.startListening();

        }
    }
}