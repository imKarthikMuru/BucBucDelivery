package com.sng.bucbucdeliveryboy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectionActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener {

    String storeID,OrderID;
    double storeLat=0,
            storeLng=0,
            DriverLat=0,
            DriverLng=0,
            CusLat=0,
            CusLng=0;
    SupportMapFragment mapFragment;
    GoogleMap mMap;
    ArrayList<LatLng> mMarkerPoints;
    List<Polyline> polylines=null;

    LatLng DriverLatLng,StoreLatLng,CustomerLatLng;

    String db_id,cus_id,addressID;

    DatabaseReference reference,storeRef,CusRef;

//    Button StartRideBTN;
    MarkerOptions options = new MarkerOptions();

    GPSTracker gpsTracker;

    TextView PickUpAddressTV,DeliveryAddressTV;

    Map<String, Object> latLng=new HashMap<>();

    Button PickupDirection,DeliveryDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

        Intent intent=getIntent();
        storeID=intent.getStringExtra("StoreID");
        OrderID=intent.getStringExtra("OrderID");

        //Setting Address in Bottom Bar
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("UserSettings", MODE_PRIVATE);
        db_id=prefs.getString("DB_ID","");

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        PickUpAddressTV=(TextView)findViewById(R.id.pickup_address);
        DeliveryAddressTV=(TextView)findViewById(R.id.delivery_address) ;

        PickupDirection=(Button)findViewById(R.id.pickupDirection);
        DeliveryDirection=(Button)findViewById(R.id.deliveryDirection);

        reference= FirebaseDatabase.getInstance().getReference("Drivers");
        storeRef=FirebaseDatabase.getInstance().getReference("Stores");
        CusRef=FirebaseDatabase.getInstance().getReference("Users");

        if (mapFragment!=null){
            mapFragment.getMapAsync(this);
        }

        gpsTracker=new GPSTracker();

        mMarkerPoints=new ArrayList<>();

        PickupDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IntentToMaps(DriverLatLng,StoreLatLng);

//                Log.d("TAG", "onClick: ::::::::::::::::"+StoreLatLng.toString());

            }
        });

        DeliveryDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentToMaps(DriverLatLng,CustomerLatLng);
//                Log.d("TAG", "onClick: ::::::::::::::::"+CustomerLatLng.toString());
            }
        });

        if (DriverLat==0||DriverLng==0||CusLat==0||CusLng==0||storeLat==0||storeLng==0) {

            reference.child(db_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    DriverLat = Double.parseDouble(String.valueOf(snapshot.child("Lat").getValue()));
                    DriverLng = Double.parseDouble(String.valueOf(snapshot.child("Lng").getValue()));

                    DriverLatLng = new LatLng(DriverLat, DriverLng);

                    storeRef.child(storeID).child("StoreDetails").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            storeLat = Double.parseDouble(String.valueOf(snapshot.child("Lat").getValue()));
                            storeLng = Double.parseDouble(String.valueOf(snapshot.child("Lng").getValue()));

                            StoreLatLng = new LatLng(storeLat, storeLng);

                            PickUpAddressTV.setText(String.valueOf(snapshot.child("StoreName").getValue())+"\n"+String.valueOf(snapshot.child("Address").getValue()));

                            storeRef.child(storeID)
                                    .child("Orders")
                                    .child(OrderID)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            cus_id = String.valueOf(snapshot.child("UserID").getValue());

                                            addressID = String.valueOf(snapshot.child("AddressID").getValue());

                                            CusRef.child(cus_id).child("SavedAddresses").child(addressID).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot ssnapshot) {
                                                    CusLat = Double.parseDouble(String.valueOf(ssnapshot.child("Lat").getValue()));
                                                    CusLng = Double.parseDouble(String.valueOf(ssnapshot.child("Long").getValue()));

                                                    DeliveryAddressTV.setText(String.valueOf(ssnapshot.child("Address").getValue()));

                                                    CustomerLatLng = new LatLng(CusLat, CusLng);

                                                    List<LatLng> latLngList=new ArrayList<>();
                                                    latLngList.add(CustomerLatLng);
                                                    latLngList.add(DriverLatLng);
                                                    latLngList.add(StoreLatLng);

                                                    Log.d("TAG", "onDataChange: :::::::::"+latLngList.size());

//                                                    FindRoute(DriverLatLng,StoreLatLng,CustomerLatLng);
                                                    FindRoute(latLngList);
                                                    Log.d("TAG", "onDataChange: :::::::::::::"+DriverLatLng.toString()+":::::::::"+StoreLatLng.toString()+"::::::::::"+CustomerLatLng.toString());
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void IntentToMaps(LatLng driverLatLng, LatLng destLatLng) {

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="+driverLatLng.latitude+","+driverLatLng.longitude+"&daddr="+destLatLng.latitude+","+destLatLng.longitude));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);

    }

    private void FindRoute(List<LatLng> latLngList) {

        if(latLngList.size()>0){
            Routing routing=new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .waypoints(latLngList)
                    .key(getString(R.string.Google_Api_Key))
                    .build();
            routing.execute();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap=googleMap;

    }


    @Override
    public void onRoutingFailure(RouteException e) {
        Toast.makeText(DirectionActivity.this, "Cannot find possible routes.!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {
        mMap.clear();

        //Driver Marker
        options.position(DriverLatLng).title("You're here");
        options.icon(bitmapDescriptorFromVector(this, R.drawable.ic_driver_pin));
        mMap.addMarker(options).showInfoWindow();

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {

        CameraUpdate center = CameraUpdateFactory.newLatLng(DriverLatLng);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

        polylines=new ArrayList<>();

        for (int j=0;j<arrayList.size();j++){

            PolylineOptions polylineOptions=new PolylineOptions();
            polylineOptions.color(getResources().getColor(android.R.color.holo_blue_dark));
            polylineOptions.width(10+j*3);
            polylineOptions.addAll(arrayList.get(j).getPoints());
            Polyline polyline=mMap.addPolyline(polylineOptions);
            polylines.add(polyline);

        }

        //Pickup Address
         options = new MarkerOptions();
        options.position(StoreLatLng).title("Pickup Point");
        options.icon(bitmapDescriptorFromVector(this, R.drawable.ic_shop_pin));
        mMap.addMarker(options).showInfoWindow();

        // Delivery Address
        options = new MarkerOptions();
        options.position(CustomerLatLng).title("Delivery Point");
        options.icon(bitmapDescriptorFromVector(this, R.drawable.ic_user_pin));
        mMap.addMarker(options).showInfoWindow();

        ValueAnimator animation = ValueAnimator.ofFloat(0f, 100f);
        final float[] previousStep = {0f};
        double deltaLatitude = DriverLatLng.latitude - options.getPosition().latitude;
        double deltaLongitude = DriverLatLng.longitude - options.getPosition().latitude;
        animation.setDuration(1500);
        animation.addUpdateListener(animation1 -> {
            float deltaStep = (Float) animation1.getAnimatedValue() - previousStep[0];
            previousStep[0] = (Float) animation1.getAnimatedValue();
            mMap.addMarker(options).setPosition(new LatLng(options.getPosition().latitude + deltaLatitude * deltaStep * 1 / 100, options.getPosition().latitude + deltaStep * deltaLongitude * 1 / 100));
        });
        animation.start();

    }

    @Override
    public void onRoutingCancelled() {

    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}