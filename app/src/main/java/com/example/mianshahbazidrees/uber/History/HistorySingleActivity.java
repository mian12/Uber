package com.example.mianshahbazidrees.uber.History;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.mianshahbazidrees.uber.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Key;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback,RoutingListener{

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private DatabaseReference historyRideInfoDb;


    private TextView rideLocation,rideDistance,rideDate,userName,userPhone;
    private ImageView userProfileImage;
    private RatingBar mRatingBar;


    private  String rideId,currentUserId;

    private  String customerId,driverId;
    private String userCustomerOrDriver;

    private LatLng destinationLatLng,pickUpLatLng;


    private  String totalDistance;
    private  double ridePrice=0;

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorAccent};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);

        polylines=new ArrayList<>();



        rideLocation=findViewById(R.id.lcation);
        rideDistance=findViewById(R.id.distance);
        rideDate=findViewById(R.id.date);

        mRatingBar=findViewById(R.id.ratingBar);

        userProfileImage=findViewById(R.id.userImageHistorySingleActivity);
        userName=findViewById(R.id.nameHistorySingleActivity);
        userPhone=findViewById(R.id.contactNumberHistorySingleActivity);


        rideId=getIntent().getExtras().getString("rideId");

        currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();

        historyRideInfoDb= FirebaseDatabase.getInstance().getReference().child("History").child(rideId);
        
        gerRideInformation();





        mMapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapSingleHistory);

        mMapFragment.getMapAsync(this);

    }

    private void gerRideInformation() {

        historyRideInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {

                    for (DataSnapshot child:dataSnapshot.getChildren())
                    {
                        if (child.getKey().equals("customer"))
                        {
                            customerId=child.getValue().toString();

                            if (!customerId.equals(currentUserId))
                            {
                                userCustomerOrDriver="Drivers";

                                getUserInformation("Customers",customerId);
                            }
                        }


                        if (child.getKey().equals("driver"))
                        {
                            driverId=child.getValue().toString();

                            if (!driverId.equals(currentUserId))
                            {
                                userCustomerOrDriver="Customers";
                                getUserInformation("Drivers",driverId);

                                displayCustomerRelatedRatingObjects();
                            }
                        }


                        if (child.getKey().equals("timeStamp"))
                        {
                           rideDate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }

                        if (child.getKey().equals("destination"))
                        {
                            rideLocation.setText(child.getValue().toString());
                        }

                        if (child.getKey().equals("rating"))
                        {
                           mRatingBar.setRating(Float.valueOf(child.getValue().toString()));

                        }

                        if (child.getKey().equals("distance"))
                        {
                            totalDistance=child.getValue().toString();

                            rideDistance.setText(totalDistance.substring(0,Math.min(totalDistance.length(),5))+" km");

                            ridePrice=Double.valueOf(totalDistance)*0.5;

                        }

                        if (child.getKey().equals("location"))
                        {
                          pickUpLatLng=new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString() ) , Double.valueOf(child.child("from").child("lng").getValue().toString() ));
                            destinationLatLng=new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString() ) , Double.valueOf(child.child("to").child("lng").getValue().toString() ));

                            if (destinationLatLng!=new LatLng(0,0))
                            {
                                getRouteToMarker();
                            }
                        }

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayCustomerRelatedRatingObjects() {

        mRatingBar.setVisibility(View.VISIBLE);

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean b) {

                historyRideInfoDb.child("rating").setValue(rating);

                DatabaseReference driverRatingDb=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("rating");
                driverRatingDb.child(rideId).setValue(rating);

            }
        });
    }

    private void getUserInformation(String otherUserDriverOrCustomer, String otherUserId) {


        DatabaseReference otherDbRef=FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserDriverOrCustomer).child(otherUserId);
        otherDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    Map<String,Object> map= (Map<String, Object>) dataSnapshot.getValue();
                   if( map.get("name")!=null)
                   {
                       userName.setText(map.get("name").toString());
                   }
                    if( map.get("phone")!=null)
                    {
                        userPhone.setText(map.get("phone").toString());
                    }
                    if( map.get("profileImageUrl")!=null)
                    {
                        Glide.with(getApplicationContext()).load(map.get("profileImageUrl").toString()).into(userProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private String getDate(Long timeStamp)
    {
        Calendar cal=Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timeStamp*1000);
        String date= android.text.format.DateFormat.format("dd-MM-yyyy hh:mm",cal).toString();
        return  date;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
    }


    private  void    getRouteToMarker(){


        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickUpLatLng,destinationLatLng)
                .build();
        routing.execute();


    }


    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {


        LatLngBounds.Builder builder=new LatLngBounds.Builder();
        builder.include(pickUpLatLng);
        builder.include(destinationLatLng);
        LatLngBounds bounds=builder.build();
       int width= getResources().getDisplayMetrics().widthPixels;
       int padding= (int) (width*0.2);

        CameraUpdate cameraUpdate=CameraUpdateFactory.newLatLngBounds(bounds,padding);
        mMap.animateCamera(cameraUpdate);


        mMap.addMarker(new MarkerOptions().position(pickUpLatLng).title("Pickup Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));

        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Destination"));


        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            // Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();

        }
    }
    @Override
    public void onRoutingCancelled() {

    }
    private  void  erasePolyLines()
    {

        for (Polyline polyline: polylines)
        {
            polyline.remove();
        }

        polylines.clear();

    }
}
