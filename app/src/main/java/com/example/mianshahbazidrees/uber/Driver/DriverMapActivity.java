package com.example.mianshahbazidrees.uber.Driver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.mianshahbazidrees.uber.Customer.CustomerMapActivity;
import com.example.mianshahbazidrees.uber.Customer.CustomerSettingActivity;
import com.example.mianshahbazidrees.uber.History.HistoryActivity;
import com.example.mianshahbazidrees.uber.MainActivity;
import com.example.mianshahbazidrees.uber.R;
import com.example.mianshahbazidrees.uber.Service.OnAppKilled;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        , com.google.android.gms.location.LocationListener,RoutingListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Switch mSwitchWorking;

    SupportMapFragment mapFragment;

    private Button mLogOut,mSetting,mRideStatus,mHistory;;

    private  int status=0;

    private  double rideDistance;


    private  String customerId="",destination;

    private  boolean isLoggingOut=false;
    private  boolean isSettingButtonClick=false;

    private  LatLng destinationLatLng,pickUpLatLng;



    private LinearLayout mCustomerLinearLayout;
    private ImageView  mCustomerProfileImage;
    private TextView mCustomerName,mCustomerPhone,mCustomerDestination;


    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.colorAccent};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        polylines = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(DriverMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DriverMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }else{
            // Write you code here if permission already given.
            mapFragment.getMapAsync(this);
        }


        startService(new Intent(DriverMapActivity.this, OnAppKilled.class));



        mCustomerLinearLayout=findViewById(R.id.customerInfoLinearLayout);

        mCustomerProfileImage=findViewById(R.id.customerProfileImage);
        mCustomerName=findViewById(R.id.customerName);
        mCustomerPhone=findViewById(R.id.customerPhone);

        mCustomerDestination=findViewById(R.id.customerDestination);

        mLogOut=findViewById(R.id.logout);
        mSetting=findViewById(R.id.setting);
        mHistory=findViewById(R.id.history);

        mSwitchWorking=findViewById(R.id.switchWorking);
        mSwitchWorking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked)
                {
                    connetDriver();

                }
                else
                {
                    disconnectDriver();
                }
            }

        });

        mRideStatus=findViewById(R.id.rideStatus);



        mRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (status)
                {
                    case 1:

                        status=2;
                        erasePolyLines();
                        if (destinationLatLng.latitude!=0.0 && destinationLatLng.latitude!=0.0)
                        {
                            getRouteToMarker(destinationLatLng);
                        }
                        mRideStatus.setText("drive Completed");
                        break;

                    case 2:

                        recordHistory();
                        endRide();
                        break;
                }
            }
        });



        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               // isLoggingOut=true;

                Intent intent=new Intent(DriverMapActivity.this,DriverSettingActivity.class);
                startActivity(intent);
                return;
            }
        });



        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DriverMapActivity.this,HistoryActivity.class);
                intent.putExtra("CustomerOrDriver","Drivers");
                startActivity(intent);
                return;
            }
        });


        mLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


//                String user_Id= FirebaseAuth.getInstance().getCurrentUser().getUid();
//                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("DriversAvailable");
//                GeoFire geoFire=new GeoFire(ref);
//                geoFire.removeLocation(user_Id);

                isLoggingOut=true;

                disconnectDriver();

                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(DriverMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
               // return;
            }
        });


       getAssignedCustomer();






    }

//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        // its used for  when user come  from  setting activity
//        isLoggingOut=false;
//    }

//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        // if driver is out from app and currently not using this app then  remove the "user id" of that driver form driver available list
//
//        if (!isLoggingOut)
//        {
//            disconnectDriver();
//        }
//
//
//    }



    private void recordHistory() {

        String user_id=FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id).child("history");
        DatabaseReference customerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId).child("history");


        DatabaseReference historyRef=FirebaseDatabase.getInstance().getReference().child("History");

        /// its a primery key which generated in History child

        String requestId=historyRef.push().getKey();

        driverRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);

        HashMap<String,Object> map=new HashMap<>();
        map.put("driver",user_id);
        map.put("customer",customerId);
        map.put("rating",0);
        map.put("timeStamp",getCurrentTimeStamp());
        map.put("destination",destination);
        map.put("location/from/lat",pickUpLatLng.latitude);
        map.put("location/from/lng",pickUpLatLng.longitude);
        map.put("location/to/lat",destinationLatLng.latitude);
        map.put("location/to/lng",destinationLatLng.longitude);
        map.put("distance",rideDistance);
        historyRef.child(requestId).updateChildren(map);




    }

    private Long getCurrentTimeStamp() {

        Long timeStamp=System.currentTimeMillis()/1000;
        return  timeStamp;
    }


    private  void  endRide()
    {

    mRideStatus.setText("Picked Customer.......DriverEnd");

        erasePolyLines();

        String user_id=FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(user_id).child("customerRequest");
            driverRef.removeValue();


        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("customerRequest");

        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(customerId);
        customerId="";

        rideDistance=0;


        if (markerPickUpLoction!=null)
        {
            markerPickUpLoction.remove();
        }



        if (assignedCustomerPickUpLocationRefListenere!=null) {
            assignedCustomerPickUpLocationRef.removeEventListener(assignedCustomerPickUpLocationRefListenere);
        }


        // now clear the  customer information on driver map activity

        mCustomerLinearLayout.setVisibility(View.GONE);
        mCustomerName.setText("");
        mCustomerDestination.setText("");
        mCustomerPhone.setText("");
        mCustomerProfileImage.setImageResource(R.mipmap.ic_profile_image);



    }



    private  void getAssignedCustomer(){

        String driverId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference assignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("customerRideId");

        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() )
                {

                        status=1;

                       customerId= dataSnapshot.getValue().toString();


                       getAssignedCustomerPickUpLocation();

                        getAssignedCustomerDestination();

                       getAssignedCustomerInfo();

                }
                else
                {

                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private  DatabaseReference assignedCustomerPickUpLocationRef;
    private  ValueEventListener assignedCustomerPickUpLocationRefListenere;
    private Marker markerPickUpLoction;

    private  void  getAssignedCustomerPickUpLocation(){


        assignedCustomerPickUpLocationRef=FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");

        assignedCustomerPickUpLocationRefListenere= assignedCustomerPickUpLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && !customerId.equals(""))
                {

                    double latitude=0;
                    double longitude=0;

                    List<Object> map= (List<Object>) dataSnapshot.getValue();

                    if (map.get(0)!=null)
                    {
                        latitude=Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1)!=null)
                    {
                        longitude=Double.parseDouble(map.get(1).toString());
                    }
                   pickUpLatLng=new LatLng(latitude,longitude);

                    markerPickUpLoction=  mMap.addMarker(new MarkerOptions().position(pickUpLatLng).title("Pick Up Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));

                    getRouteToMarker(pickUpLatLng);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }







    private  void getAssignedCustomerDestination(){

        String driverId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference assignedCustomerDestinationRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");
        assignedCustomerDestinationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    //List<Object> map = (List<Object>) dataSnapshot.getValue();
                    Map<String,Object> map= (Map<String, Object>) dataSnapshot.getValue();
                    //String destination = dataSnapshot.getValue().toString();
                    if (map.get("destination") !=null ) {
                        destination= map.get("destination").toString();
                        mCustomerDestination.setText("Destination : " + destination);
                    } else {
                        mCustomerDestination.setText("Destination : ----");
                    }


                    double destinationLatitude = 0.0;
                    double destinationLongitude =0.0;



                    if (map.get("destinationLat") != null) {
                        destinationLatitude = Double.parseDouble(map.get("destinationLat").toString());
                    }
                    if (map.get("destinationLng") != null) {
                        destinationLongitude = Double.parseDouble(map.get("destinationLng").toString());

                        destinationLatLng=new LatLng(destinationLatitude,destinationLongitude);
                    }


                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    private  void  getAssignedCustomerInfo()
    {
        mCustomerLinearLayout.setVisibility(View.VISIBLE);


       DatabaseReference  mCustomerDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    Map<String,Object> map= (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name")!=null)
                    {
                       mCustomerName.setText(map.get("name").toString());
                    }
                    if (map.get("phone")!=null)
                    {

                        mCustomerPhone.setText(map.get("phone").toString());
                    }

                    if (map.get("destination")!=null)
                    {

                        mCustomerDestination.setText(map.get("destination").toString());
                    }

                    if (map.get("profileImageUrl")!=null)
                    {
                        // now set image url  into image view throgh glide liberary
                        // Glide
                        Glide.with(getApplicationContext())
                                .load(map.get("profileImageUrl").toString())
                                .into(mCustomerProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }

    protected  synchronized  void  buildGoogleApiClient()
    {

        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {

        if (getApplicationContext()!=null) {


            if (!customerId.equals(""))
            {
                /// divide by 1000 to convert metere distance into Km's;
                rideDistance+=mLastLocation.distanceTo(location)/1000;
            }

            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

            String user_Id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference availabeRef = FirebaseDatabase.getInstance().getReference("DriversAvailable");
             DatabaseReference workingRef= FirebaseDatabase.getInstance().getReference("DriversWorking");

            GeoFire geoFireAvailable = new GeoFire(availabeRef);
             GeoFire geoFireWorking=new GeoFire(workingRef);
          //   customerId="cWi4nLaQb8ZFPzTMRg1BvmehuXD2";

        switch (customerId)
        {
            case "":
                geoFireWorking.removeLocation(user_Id);
                geoFireAvailable.setLocation(user_Id,new GeoLocation(location.getLatitude(),location.getLongitude()));
                break;
                default:
                    geoFireAvailable.removeLocation(user_Id);
                    geoFireWorking.setLocation(user_Id,new GeoLocation(location.getLatitude(),location.getLongitude()));
                    break;
        }


          //  geoFireAvailable.setLocation(user_Id, new GeoLocation(location.getLatitude(), location.getLongitude()));

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    private  void connetDriver()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }


    private  void disconnectDriver()
    {

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);

        String user_Id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("DriversAvailable");
        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(user_Id);
    }





    final int LOCATION_REQUEST_CODE=1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:

                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    mapFragment.getMapAsync(this);
                }
                else
                {
                    Toast.makeText(this, "Please provide Permissions", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }



    private  void    getRouteToMarker( LatLng pickUpLatLng){


        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()), pickUpLatLng)
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
