package com.example.laptop.rideuser;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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

import Common.Common;
import Model.Rider;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    //Location
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERBAL = 5000;
    private static int FASTEST_INTERval = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference ref;
    GeoFire geoFire;
    Marker mCurrentUser;
    //Bottomsheet view
    ImageView imgExpandable;
    BottomSheetRiderFragment mBottomSheet;
    Button btnpickup;
    View mapView;


    boolean isDriverfound=false;
    String driverId ="";
    int radius = 1;
    int distance= 1 ;
    private static final int LIMIT = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // Map
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);




        //initialize view
        imgExpandable = (ImageView) findViewById(R.id.imgExpandable);
        mBottomSheet = BottomSheetRiderFragment.newInstance("Rider Bottom Sheet");
        imgExpandable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
            }
        });
        btnpickup = (Button) findViewById(R.id.Pickupbtn);
        btnpickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());

            }
        });

        setUpLocation();
    }

    private void requestPickupHere(String uid) {
        DatabaseReference dbrequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbrequest);
        mGeoFire.setLocation(uid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        if (mCurrentUser.isVisible())
            mCurrentUser.remove();
        //add new marker
        mCurrentUser = mMap.addMarker(new MarkerOptions()
                .title("Pickup Here")
                .snippet("")
                .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mCurrentUser.showInfoWindow();
        btnpickup.setText("Finding Driver...");

        findDriver();

    }

    private void findDriver() {
        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire gfDrivers= new GeoFire(drivers);
        GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),
                radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //if Driver found
                if (!isDriverfound)
                {
                    isDriverfound = true;
                    driverId = key;
                    btnpickup.setText("Call Driver");
                    Toast.makeText(Home.this, ""+key, Toast.LENGTH_SHORT).show();
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

                //if driver still not found,increase distance
                if (!isDriverfound)
                {
                    radius++;
                    findDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    //Press ctrl+o


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {

                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }

                }
                break;
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            //Request Runtime permissions
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);

        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();

                displayLocation();
            }
        }
        ;

    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {

            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();


                    //Add MArker
                    if (mCurrentUser != null)
                        mCurrentUser.remove();
                    mCurrentUser = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .title("Your Location"));

                    //Move camera
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
                    loadAllAvilabeDriver();

            Log.d("Update", String.format("Your location has changed: %f/ %f", latitude, longitude));


        } else {
            Log.d("ERROR", "Cannot get your Location");
        }

    }

    private void loadAllAvilabeDriver() {
        //load all avilabele drivers in distance 5km
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire gf= new GeoFire(driverLocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),distance);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                //use key to get email from table of users as drivers

                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Rider rider = dataSnapshot.getValue(Rider.class);
                                //Adddriver to MAP
                                mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(location.latitude,location.longitude))
                                    .flat(true)
                                    .title(rider.getName())
                                        .snippet("Phone:"+rider.getPhone())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (distance <= LIMIT)
                {
                    distance++;
                    loadAllAvilabeDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERBAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();


    }

    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError((resultCode)))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
            else
                Toast.makeText(this, "This Device is Not Supported", Toast.LENGTH_SHORT).show();
            finish();
            return (false);
        }
        return (true);

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on left bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(70,0,5,5);

        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ){

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, (com.google.android.gms.location.LocationListener) this);



            }



    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }
}
