package com.sng.bucbucdeliveryboy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sng.bucbucdeliveryboy.modelClass.OrdersModelsClass;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class DetailRideHistoryActivity extends AppCompatActivity {

    TextView OrderedDate,CusName,CusAddress,CusMobile,StoreName,StoreAddress,StoreMobile,PaymentType,CallCustomer,
            Note,ItemTotal,DeliveryFee,Taxes,ToPay;
    RecyclerView ProductRecyclerView;

    DatabaseReference reference,StoreRef,CusRef;
    FirebaseRecyclerAdapter adapter;
    FirebaseRecyclerOptions<OrdersModelsClass> options;

    String postKey,driverID,storeID,orderID,cusID,dateTime;
    int total=0,deliveryFee=0,toPay=0;

    LoadingDialog loadingDialog=LoadingDialog.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_ride_history);

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

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("UserSettings", MODE_PRIVATE);
        driverID=prefs.getString("DB_ID","");

        postKey=getIntent().getStringExtra("PostKey");

        reference= FirebaseDatabase.getInstance().getReference("Drivers");
        StoreRef = FirebaseDatabase.getInstance().getReference("Stores");
        CusRef = FirebaseDatabase.getInstance().getReference("Users");

        reference.child(driverID)
                .child("OrderHistory")
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
                                        }
                                        catch (NumberFormatException e) {
                                            Log.d("TAG", "onDataChange: ::::::::::::::::Exception NUmber Format TOPAY");
                                        }
                                        cusID=String.valueOf(dsnapshot.child("UserID").getValue());

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                        options=new FirebaseRecyclerOptions.Builder<OrdersModelsClass>()
                                .setQuery(StoreRef.child(storeID)
                                        .child("Orders")
                                        .child(orderID).child("Products"),OrdersModelsClass.class).build();

                        adapter=new FirebaseRecyclerAdapter<OrdersModelsClass, OrderListViewHolder>(options) {

                            @NonNull
                            @Override
                            public OrderListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_recycler_list, parent, false);
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

                        ProductRecyclerView.setLayoutManager(new LinearLayoutManager(DetailRideHistoryActivity.this,RecyclerView.VERTICAL,false));
                        ProductRecyclerView.setAdapter(adapter);
                        adapter.startListening();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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