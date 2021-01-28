package com.sng.bucbucdeliveryboy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sng.bucbucdeliveryboy.modelClass.OrdersModelsClass;

public class RideHistoryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseRecyclerOptions<OrdersModelsClass> options;
    DatabaseReference reference;
    String uid,postID;
    LinearLayoutManager manager;
    OrderListAdapter adapter;
    String userID;


    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;
    private Parcelable mListState = null;

    @Override
    protected void onPause() {
        super.onPause();

        mBundleRecyclerViewState = new Bundle();
        mListState = recyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, mListState);

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mBundleRecyclerViewState != null) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    mListState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
                    recyclerView.getLayoutManager().onRestoreInstanceState(mListState);

                }
            }, 50);
        }

        recyclerView.setLayoutManager(manager);
    }


    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_history);

        recyclerView=(RecyclerView)findViewById(R.id.orderhistoryRV);

        manager=new LinearLayoutManager(RideHistoryActivity.this,RecyclerView.VERTICAL,true);
        manager.setStackFromEnd(true);

        recyclerView.setLayoutManager(manager);

        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("UserSettings", MODE_PRIVATE);
        postID=prefs.getString("DB_ID","");

        reference= FirebaseDatabase.getInstance().getReference("Drivers").child(postID).child("OrderHistory");

        options=new FirebaseRecyclerOptions.Builder<OrdersModelsClass>()
                .setQuery(reference,OrdersModelsClass.class)
                .build();

        adapter=new OrderListAdapter(options,RideHistoryActivity.this);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public void back(View view) {
        finish();
    }

    public class OrderListAdapter extends FirebaseRecyclerAdapter<OrdersModelsClass,OrdersViewHolder>{

        Context context;
        LoadingDialog loadingDialog=LoadingDialog.getInstance();

        public OrderListAdapter(@NonNull FirebaseRecyclerOptions<OrdersModelsClass> options, Context context) {
            super(options);
            this.context = context;
            loadingDialog.ShowProgress(context,"Loading stuffs..!",false);
        }

        @Override
        protected void onBindViewHolder(@NonNull OrdersViewHolder holder, int position, @NonNull OrdersModelsClass model) {
            loadingDialog.hideProgress();

            holder.OrderDetailsTV.setVisibility(View.GONE);

            final DatabaseReference ref=getRef(position);
            final String post=ref.getKey();

            FirebaseDatabase.getInstance().getReference("Stores")
                    .child(model.getStoreID()).child("StoreDetails").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    holder.StoreName.setText(String.valueOf(snapshot.child("StoreName").getValue()));
                    holder.StoreAddress.setText(String.valueOf(snapshot.child("Address").getValue()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            FirebaseDatabase.getInstance().getReference("Stores")
                    .child(model.getStoreID()).child("Orders").child(model.getOrderID()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    holder.CustomeName.setText(String.valueOf(snapshot.child("Name").getValue()));
                    holder.AreaNameTV.setText(String.valueOf(snapshot.child("Address").getValue()));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            SharedPreferences prefs = getApplicationContext().getSharedPreferences("UserSettings", MODE_PRIVATE);
            postID=prefs.getString("DB_ID","");

            reference= FirebaseDatabase.getInstance().getReference("Drivers").child(postID).child("OrderHistory");

            reference.child(model.getOrderID()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("DeliveredDate").exists()) {
                            holder.OrderedDateTime.setVisibility(View.VISIBLE);
                            holder.OrderedDateTime.setText(String.valueOf(snapshot.child("DeliveredDate").getValue())
                                    + "  " +
                                    String.valueOf(snapshot.child("DeliveredTime").getValue()));
                        }else {
                            holder.OrderedDateTime.setVisibility(View.GONE);
                        }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            holder.Show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(RideHistoryActivity.this,DetailRideHistoryActivity.class)
                            .putExtra("PostKey",post));
                }
            });


        }

        @NonNull
        @Override
        public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_recycler,parent,false);
            return  new OrdersViewHolder(view);
        }
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

}