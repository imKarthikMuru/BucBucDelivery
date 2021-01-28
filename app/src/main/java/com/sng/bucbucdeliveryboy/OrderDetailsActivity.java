package com.sng.bucbucdeliveryboy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sng.bucbucdeliveryboy.Notification.NotificationInterface;
import com.sng.bucbucdeliveryboy.Notification.NotificationRequest;
import com.sng.bucbucdeliveryboy.Notification.NotificationResponse;
import com.sng.bucbucdeliveryboy.Notification.RetrofitClient;
import com.sng.bucbucdeliveryboy.modelClass.OrdersModelsClass;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class OrderDetailsActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/";

    TextView OrderedDate,CusName,CusAddress,CusMobile,StoreName,StoreAddress,StoreMobile,PaymentType,CallCustomer,
    Note,ItemTotal,DeliveryFee,Taxes,ToPay;
    RecyclerView ProductRecyclerView;
    Button GetDirection,StartRide;

    String storeName,DriverName,userToken;

    DatabaseReference reference,StoreRef,CusRef;
    FirebaseRecyclerAdapter adapter;
    FirebaseRecyclerOptions<OrdersModelsClass> options;

    String postKey,driverID,storeID,orderID,cusID,dateTime;
    int total=0,deliveryFee=0,toPay=0;

    Map<String, Object> UpdateRide=new HashMap<>();

    LoadingDialog loadingDialog=LoadingDialog.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        OrderedDate=(TextView)findViewById(R.id.date);
        CusName=(TextView)findViewById(R.id.cusName);
        CusAddress=findViewById(R.id.address);
        CusMobile=findViewById(R.id.mobile);
        StoreName=findViewById(R.id.storeName);
        StoreAddress=findViewById(R.id.Storeaddress);
        StoreMobile=findViewById(R.id.Storemobile);
        PaymentType=findViewById(R.id.paymentType);
        CallCustomer=findViewById(R.id.callTV);
        Note=findViewById(R.id.noteTv);
        ItemTotal=findViewById(R.id.itemTotal);
        DeliveryFee=findViewById(R.id.deliveryFee);
        Taxes=findViewById(R.id.taxes);
        ToPay=findViewById(R.id.toPay);

        ProductRecyclerView=findViewById(R.id.orderRecycler);

        GetDirection=findViewById(R.id.getDirection);
        StartRide=findViewById(R.id.startRide);

        //Setting Address in Bottom Bar
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("UserSettings", MODE_PRIVATE);
        driverID=prefs.getString("DB_ID","");
        DriverName=prefs.getString("Name","");

        postKey=getIntent().getStringExtra("PostKey");

        Log.d("TAG", "onCreate: :::::::::::::"+postKey);

        reference= FirebaseDatabase.getInstance().getReference("Drivers");
        StoreRef = FirebaseDatabase.getInstance().getReference("Stores");
        CusRef = FirebaseDatabase.getInstance().getReference("Users");

        final DateFormat date = new SimpleDateFormat("dd-MMMM-yyyy");
        String deliveryDate = date.format(Calendar.getInstance().getTime());

        final DateFormat time = new SimpleDateFormat("hh:mm a");
        String deliveryTime = time.format(Calendar.getInstance().getTime());

        Map<String, Object> orders = new HashMap<>();
        orders.put("DeliveredDate",deliveryDate);
        orders.put("DeliveredTime",deliveryTime);

        reference.child(driverID)
                .child("Pending").child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                orders.put("Date",snapshot.child("Date").getValue());
                orders.put("OrderID",snapshot.child("OrderID").getValue());
                orders.put("ShipmentStatus","Delivered");
                orders.put("StoreID",snapshot.child("StoreID").getValue());
                orders.put("Time",snapshot.child("Time").getValue());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        StartRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadingDialog.ShowProgress(OrderDetailsActivity.this,"Updating.. Hold tight!",false);

                Log.d("TAG", "onClick: :::::::::::"+UpdateRide.get("ShipmentStatus"));

                if (UpdateRide.get("ShipmentStatus").equals("Delivered")){

                    StoreRef.child(storeID)
                            .child("Orders")
                            .child(orderID).
                            updateChildren(UpdateRide).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){


                            }else{
                                Toast.makeText(OrderDetailsActivity.this, "oh snap! please try again later.", Toast.LENGTH_SHORT).show();
                            }

                            loadingDialog.hideProgress();
                        }

                    });

                    CusRef.child(cusID).child("MyOrders").child(orderID).updateChildren(UpdateRide);

                    reference.child(driverID)
                            .child("Pending")
                            .child(postKey).updateChildren(UpdateRide);


                    reference.child(driverID).child("OrderHistory").child(postKey).setValue(orders).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                sendNotification("Order Delivered!","You Order on "+storeName+" package has delivered successfully by "+DriverName,"MyOrders",userToken);

                                Intent intent=new Intent(OrderDetailsActivity.this,RideCompletionActivity.class).putExtra("Post",postKey);
                                startActivity(intent);
                                finish();

                            }else {
                                Toast.makeText(OrderDetailsActivity.this, "Oops! Something went wrong.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }else{
                    StoreRef.child(storeID)
                            .child("Orders")
                            .child(orderID).
                            updateChildren(UpdateRide).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                sendNotification("Order Status","You Order on "+storeName+" package has delivery by "+DriverName,"MyOrders",userToken);

                            }else{
                                Toast.makeText(OrderDetailsActivity.this, "oh snap! please try again later.", Toast.LENGTH_SHORT).show();
                            }

                            loadingDialog.hideProgress();
                        }

                    });
                    CusRef.child(cusID).child("MyOrders").child(orderID).updateChildren(UpdateRide);
                    reference.child(driverID)
                            .child("Pending")
                            .child(postKey).updateChildren(UpdateRide);
                }



            }
        });

                reference.child(driverID)
                .child("Pending")
                .child(postKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        dateTime=String.valueOf(snapshot.child("Date").getValue()+"  "+snapshot.child("Time").getValue());

                        orderID=String.valueOf(snapshot.child("OrderID").getValue());
                        storeID=String.valueOf(snapshot.child("StoreID").getValue());

                        OrderedDate.setText(dateTime);

                        StoreRef.child(storeID)
                                .child("StoreDetails")
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        StoreName.setText(String.valueOf(snapshot.child("StoreName").getValue()));
                                        StoreAddress.setText(String.valueOf(snapshot.child("Address").getValue()));
                                        StoreMobile.setText(String.valueOf(snapshot.child("ContactNo").getValue()));

                                        storeName=String.valueOf(snapshot.child("StoreName").getValue());

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                        StoreRef.child(storeID)
                                .child("Orders")
                                .child(orderID)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dsnapshot) {

                                        CusName.setText(String.valueOf(dsnapshot.child("Name").getValue()));
                                        CusAddress.setText(String.valueOf(dsnapshot.child("Address").getValue()));
                                        CusMobile.setText(String.valueOf(dsnapshot.child("Mobile").getValue()));

                                        PaymentType.setText(String.valueOf(dsnapshot.child("PaymentMethod").getValue()));

                                        Note.setText(String.valueOf(dsnapshot.child("Suggestion").getValue()));

                                        ToPay.setText("₹"+String.valueOf(dsnapshot.child("ToPay").getValue()));
                                        try {
                                            toPay=Integer.parseInt(String.valueOf(dsnapshot.child("ToPay").getValue()));

                                            if (String.valueOf(dsnapshot.child("OrderStatus").getValue()).equals("Shipped")){
                                                StartRide.setEnabled(true);
                                            }else {
                                                StartRide.setEnabled(false);
                                            }
                                        }
                                        catch (NumberFormatException e) {
                                            Log.d("TAG", "onDataChange: ::::::::::::::::Exception NUmber Format TOPAY");
                                        }
                                        cusID=String.valueOf(dsnapshot.child("UserID").getValue());

                                        FirebaseDatabase.getInstance().getReference("Users").child(cusID)
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        userToken=String.valueOf(snapshot.child("PushToken").getValue());
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });

                                        if (!dsnapshot.child("ShipmentStatus").exists()){
                                            StartRide.setEnabled(true);
                                            UpdateRide.put("ShipmentStatus","Transit");
                                            UpdateRide.put("OrderStatus","Shipped");
                                            StartRide.setText("Start Ride");
                                        }else if(dsnapshot.child("ShipmentStatus").getValue().equals("Transit")){
                                                StartRide.setText("Mark us Delivered");
                                                StartRide.setEnabled(true);
                                            UpdateRide.put("OrderStatus","Delivered");
                                                UpdateRide.put("ShipmentStatus","Delivered");
                                            }else {
                                                StartRide.setText("Delivered");
                                                StartRide.setEnabled(false);
                                            }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                        options=new FirebaseRecyclerOptions.Builder<OrdersModelsClass>()
                                .setQuery(StoreRef.child(storeID)
                                        .child("Orders")
                                        .child(orderID).child("Products"),OrdersModelsClass.class).build();

                        adapter=new FirebaseRecyclerAdapter<OrdersModelsClass,OrderListViewHolder>(options) {

                            @NonNull
                            @Override
                            public OrderListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_recycler_list, parent, false);
                                return new OrderListViewHolder(view);

                            }

                            @Override
                            protected void onBindViewHolder(@NonNull OrderListViewHolder holder, int position, @NonNull OrdersModelsClass model) {
                                holder.ProductName.setText(model.getProductName());
                                holder.SubTotalTV.setText("₹"+model.getPrice());
                                holder.Quantity.setText(""+model.getQuantity());


                                total+=model.getPrice();

                                ItemTotal.setText("₹"+total);

                                deliveryFee=toPay-total;
                                DeliveryFee.setText("₹"+deliveryFee);
                                ToPay.setText("₹"+toPay);

                                StoreRef.child(storeID).child("Products").child(model.getCategory()).child(model.getProductID()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        holder.PriceTv.setText("₹ "+snapshot.child("Price").getValue());

                                        final String img=String.valueOf(snapshot.child("ProductImage").getValue());

                                        if (!img.isEmpty()){

                                            Picasso.get().load(img).networkPolicy(NetworkPolicy.OFFLINE).into(holder.ProductBanner, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Picasso.get().load(img).into(holder.ProductBanner);
                                                }
                                            });

                                        }else{
                                            Picasso.get().load(R.drawable.logotext).into(holder.ProductBanner);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        };

                        ProductRecyclerView.setLayoutManager(new LinearLayoutManager(OrderDetailsActivity.this,RecyclerView.VERTICAL,false));
                        ProductRecyclerView.setAdapter(adapter);
                        adapter.startListening();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                GetDirection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startActivity(new Intent(OrderDetailsActivity.this,DirectionActivity.class)
                        .putExtra("StoreID",storeID)
                        .putExtra("OrderID",orderID));
                    }
                });


    }

    private void sendNotification(String order_status, String s, String myOrders, String userToken) {

        NotificationRequest request=new NotificationRequest(userToken,new NotificationRequest.Notification(order_status,s,myOrders));

        Log.d("TAG", "onComplete: :::::::::::::::::"+userToken);

        RetrofitClient.getRetrofit(BASE_URL)
                .create(NotificationInterface.class)
                .sent(request)
                .enqueue(new retrofit2.Callback<NotificationResponse>() {
                    @Override
                    public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                        if (response.code() == 200){
                            Toast.makeText(OrderDetailsActivity.this, "message send", Toast.LENGTH_SHORT).show();
                        }else if (response.code()==400){
                            Toast.makeText(OrderDetailsActivity.this, "Bad request", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(OrderDetailsActivity.this, "404", Toast.LENGTH_SHORT).show();
                        }
                        Log.d("TAG", "onResponse: :::::::::::Error"+response.code()+":::::::::"+call.request());
                    }

                    @Override
                    public void onFailure(Call<NotificationResponse> call, Throwable t) {

                        Log.d("TAG", "onFailure: ::::::::::::::"+t.getCause()+"::::::"+t.getMessage()+":::::::::");
                    }
                });
    }

    public class OrderListViewHolder extends RecyclerView.ViewHolder {

        TextView ProductName,PriceTv,SubTotalTV,Quantity;
        ImageView ProductBanner;

        public OrderListViewHolder(@NonNull View itemView) {
            super(itemView);

            ProductName=itemView.findViewById(R.id.productName);
            PriceTv=itemView.findViewById(R.id.price);
            SubTotalTV=itemView.findViewById(R.id.subTotal);
            Quantity=itemView.findViewById(R.id.qty);
            ProductBanner=itemView.findViewById(R.id.product_image);

        }
    }

    public void Back(View view) {
        finish();
    }
}