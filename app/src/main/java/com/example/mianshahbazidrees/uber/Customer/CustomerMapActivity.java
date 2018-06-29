package com.example.mianshahbazidrees.uber.Customer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mianshahbazidrees.uber.History.HistoryActivity;
import com.example.mianshahbazidrees.uber.MainActivity;
import com.example.mianshahbazidrees.uber.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        , com.google.android.gms.location.LocationListener {



    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    SupportMapFragment mapFragment;

    private Button mLogOut,mRequest,mSetting,mHistory;
    private  LatLng mPickUpLocation;

    private RatingBar mRatingBar;

    private  boolean requestBol=false;
    private  Marker pickUpMarker;
    private  String destination="";


    private LinearLayout mDriverLinearLayout;
    private ImageView mDriverProfileImage;
    private TextView mDriverName,mDriverPhone,mDriverCar;


    PlaceAutocompleteFragment autocompleteFragment;

    private  String  mRequestService="";
    private RadioGroup mRadioGroup;


    private  LatLng destinationLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(CustomerMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CustomerMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }else{
            // Write you code here if permission already given.
            mapFragment.getMapAsync(this);
        }


        destinationLatLng=new LatLng(0.0,0.0);


        // by default seeting of uber type
        mRadioGroup=findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.UberX);

        mRatingBar=findViewById(R.id.ratingBar);

        mLogOut=findViewById(R.id.logout);
        mRequest=findViewById(R.id.request);
        mSetting=findViewById(R.id.setting);
        mHistory=findViewById(R.id.history);


        mDriverLinearLayout=findViewById(R.id.driverInfoLinearLayout);

        mDriverProfileImage=findViewById(R.id.driverProfileImage);
        mDriverName=findViewById(R.id.driverName);
        mDriverPhone=findViewById(R.id.driverPhone);
        mDriverCar=findViewById(R.id.driverCar);


        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(CustomerMapActivity.this,HistoryActivity.class);
                intent.putExtra("CustomerOrDriver","Customers");
                startActivity(intent);
                return;
            }
        });


        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(CustomerMapActivity.this,CustomerSettingActivity.class);
                startActivity(intent);
                return;
            }
        });


        /// google  autoComplete places  code here
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination=place.getName().toString();

                destinationLatLng=place.getLatLng();

                //Log.i(TAG, "Place: " + place.getName());

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                ///Log.i(TAG, "An error occurred: " + status);
            }
        });


        mLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent=new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // its the id of current login customer bcz in  customerRequest  object we will save customer id and his  position that is  latitude and longitue


                if (requestBol)
                {
                        endRide();

                }
                else
                {

                    int selectId=mRadioGroup.getCheckedRadioButtonId();
                    final RadioButton radioButton=findViewById(selectId);

                    if (radioButton.getText()==null)
                    {
                        return;
                    }

                    mRequestService=radioButton.getText().toString();


                    requestBol=true;

                    String user_id=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref= FirebaseDatabase.getInstance().getReference("customerRequest");

                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.setLocation(user_id,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                    mPickUpLocation=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

                    pickUpMarker=mMap.addMarker(new MarkerOptions().position(mPickUpLocation).title("Pick Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                    mRequest.setText("Getting your  Driver.....");

                    getClosestDriver();

                }



            }
        });


    }
    private  int radius=1;
    private  boolean driverFound=false;
    private String driverFoundId=null;

    private  GeoQuery geoQuery;
    private  void getClosestDriver()
    {

        DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference("DriversAvailable");
        GeoFire geoFire=new GeoFire(driverLocation);
         geoQuery= geoFire.queryAtLocation(new GeoLocation(mPickUpLocation.latitude,mPickUpLocation.longitude),radius);

        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!driverFound && requestBol)
                {

                    DatabaseReference mCustomerDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                            if (dataSnapshot.exists())
                            {
                                Map<String,Object> driverMap=(Map<String, Object>) dataSnapshot.getValue();

                                if (driverFound)
                                {
                                    return;
                                }

                                  if ( driverMap.get("service").equals(mRequestService))
                                  {

                                      driverFound=true;
                                      driverFoundId=dataSnapshot.getKey();


                                      DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");

                                      String customerId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                                      HashMap map=new HashMap();
                                      map.put("customerRideId",customerId);
                                      map.put("destination",destination);
                                      map.put("destinationLat",destinationLatLng.latitude);
                                      map.put("destinationLng",destinationLatLng.longitude);
                                      driverRef.updateChildren(map);

                                      getDriverLocation();

                                      getDriverInfo();

                                      getRideHasEnded();

                                      mRequest.setText("Looking for Driver Location........");

                                  }

                            }




                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });




                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if (!driverFound)
                {
                    radius++;
                    Log.e("Radius",radius+"");
                    getClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private  ValueEventListener driverLocationRefListener;
  private  void  getDriverLocation()
  {

      driverLocationRef=FirebaseDatabase.getInstance().getReference().child("DriversWorking").child(driverFoundId).child("l");
      driverLocationRefListener=driverLocationRef.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {

              if (dataSnapshot.exists() &&  requestBol)
              {
                  double latitude=0;
                  double longitude=0;

                  mRequest.setText("Driver Found");

                  List<Object> map= (List<Object>) dataSnapshot.getValue();
                  if (map.get(0)!=null)
                  {
                      latitude=Double.parseDouble(map.get(0).toString());
                  }
                  if (map.get(1)!=null)
                  {
                      longitude=Double.parseDouble(map.get(1).toString());
                  }
                  LatLng driverLtgLng=new LatLng(latitude,longitude);

                  if (mDriverMarker!=null)
                  {
                      mDriverMarker.remove();
                  }

                  // for calculate the distance  for driver to checking where is it now
                  // i use it Location object because it has  special method which  calculate the distance between two points

                  Location location1=new Location("");

                  location1.setLatitude(mPickUpLocation.latitude);
                  location1.setLongitude(mPickUpLocation.longitude);

                  Location location2=new Location("");

                  location2.setLatitude(driverLtgLng.latitude);
                  location2.setLongitude(driverLtgLng.longitude);

                  double distance=location1.distanceTo(location2);

                  if (distance<100)
                  {
                      mRequest.setText("Driver is arrived");
                  }
                  else
                  {
                      mRequest.setText("Driver distance is ..."+distance);
                  }





                 mDriverMarker= mMap.addMarker(new MarkerOptions().position(driverLtgLng).title("Your Driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));

              }

          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
      });

  }




    private  void  getDriverInfo()
    {
        mDriverLinearLayout.setVisibility(View.VISIBLE);


        DatabaseReference  mDriverDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
        mDriverDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    Map<String,Object> map= (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name")!=null)
                    {
                        mDriverName.setText(map.get("name").toString());
                    }
                    if (map.get("phone")!=null)
                    {

                        mDriverPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("car")!=null)
                    {

                        mDriverCar.setText(map.get("car").toString());
                    }


                    if (map.get("profileImageUrl")!=null)
                    {
                        // now set image url  into image view throgh glide liberary
                        // Glide
                        Glide.with(getApplicationContext())
                                .load(map.get("profileImageUrl").toString())
                                .into(mDriverProfileImage);
                    }

                    float ratingSum=0;
                    int ratingTotal=0;
                    float ratingAvg=0.0f;
                   for (DataSnapshot child: dataSnapshot.child("rating").getChildren())
                   {
                       ratingSum+=Integer.parseInt(child.getValue().toString());

                       ratingTotal++;



                   }
                   if (ratingTotal!=0)
                   {
                       ratingAvg=ratingSum/ratingTotal;
                       mRatingBar.setRating(ratingAvg);
                   }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




private  DatabaseReference driveHasEndedRef;
  private  ValueEventListener driveHasEndedRefListener;
    private  void getRideHasEnded(){

        driveHasEndedRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest").child("customerRideId");

        driveHasEndedRefListener=driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() )
                {

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

    private  void  endRide()
    {

        requestBol=false;


        //Step 1 /// first remove the all liteneres then  remove the database values fron database
        geoQuery.removeAllListeners();

        driverLocationRef.removeEventListener(driverLocationRefListener);

        driveHasEndedRef.removeEventListener(driverLocationRefListener);




        // now removing the value from drivers inside child which contains customerRequest Id
        // for that purpose  we just set value true then it automatically clear all values inside that child

        if (driverFoundId!=null)
        {
            DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");
            driverRef.removeValue();
            driverFoundId=null;

        }

        driverFound=false;
        radius=1;

        //Step 2
        // now removing the values from database
        String user_id=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("customerRequest");

        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(user_id);


        // step 3
        // now remove the pickup marker and driver marker
        // for this purpose first we need  declare marker varibale at the top then  we weill assign it


        if (pickUpMarker!=null)
        {
            pickUpMarker.remove();
        }


        if (mDriverMarker!=null)
        {
            mDriverMarker.remove();
        }

        mRequest.setText("Call Uber");
        destination=null;

        autocompleteFragment.setText("");


        // now clear the  driver information on customer map activity

        mDriverLinearLayout.setVisibility(View.GONE);
        mDriverName.setText("");
        mDriverCar.setText("");
        mDriverPhone.setText("");
        mDriverProfileImage.setImageResource(R.mipmap.ic_profile_image);



    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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
            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        }



    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    protected void onStop() {
        super.onStop();


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
}

