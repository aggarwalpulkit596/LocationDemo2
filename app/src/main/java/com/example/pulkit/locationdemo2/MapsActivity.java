package com.example.pulkit.locationdemo2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.pulkit.locationdemo2.MODEL.DirectionResults;
import com.example.pulkit.locationdemo2.MODEL.Distance;
import com.example.pulkit.locationdemo2.MODEL.Duration;
import com.example.pulkit.locationdemo2.MODEL.Location;
import com.example.pulkit.locationdemo2.MODEL.Route;
import com.example.pulkit.locationdemo2.MODEL.Steps;
import com.example.pulkit.locationdemo2.REST.ApiClients;
import com.example.pulkit.locationdemo2.REST.ApiInterface;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    FusedLocationProviderClient mFusedLocationProviderClient;
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    android.location.Location location;
    List<LatLng> points = new ArrayList<>();
    MarkerOptions markerOptions = new MarkerOptions();
    private FloatingActionMenu menuRed;
    private com.github.clans.fab.FloatingActionButton fab1;
    private com.github.clans.fab.FloatingActionButton fab2;
    private com.github.clans.fab.FloatingActionButton fab3;
    private com.github.clans.fab.FloatingActionButton fab4;
    private List<FloatingActionMenu> menus = new ArrayList<>();
    BottomSheetLayout bottomSheet;
    Polyline polylineFinal,polyline1;
    String time,distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        if (savedInstanceState != null) {
//            location = savedInstanceState.getParcelable(KEY_LOCATION);
//            CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
//        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        points.add(new LatLng(28.6127, 77.2773));//Akshardham
        points.add(new LatLng(28.6129, 77.2295));//India Gate
        points.add(new LatLng(28.6881, 77.2069));//Faculty Of Maths
        points.add(new LatLng(28.5244, 77.1855));//Qutub Minar
        bottomSheet = findViewById(R.id.bottomsheet);
        menuRed = findViewById(R.id.fab);
        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);
        fab3 = findViewById(R.id.fab3);
        fab4 = findViewById(R.id.fab4);
        menuRed.setClosedOnTouchOutside(true);
        menus.add(menuRed);
        menuRed.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuRed.toggle(true);
            }
        });
        fab1.setOnClickListener(clickListener);
        fab2.setOnClickListener(clickListener);
        fab3.setOnClickListener(clickListener);
        fab4.setOnClickListener(clickListener);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }

        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        mFusedLocationProviderClient
                .requestLocationUpdates(mLocationRequest,
                        mLocationCallback, null);
        mRequestingLocationUpdates = true;

    }

    private void getDeviceLocation(final LatLng latLng) {

        Log.i("Functionworking", "onClick: " + latLng);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                location = locationResult.getLastLocation();
                fetchroutes(location, latLng);

            }

        };
    }

    private void fetchroutes(final android.location.Location lctn, final LatLng latLng) {
        Log.i("Functionworking", "onClick: " + lctn + latLng);
        ApiInterface apiService = ApiClients.getClient().create(ApiInterface.class);
        Call<DirectionResults> call = apiService.getRoutes(lctn.getLatitude() + "," + lctn.getLongitude(), latLng.latitude + "," + latLng.longitude);
        call.enqueue(new Callback<DirectionResults>() {
            @Override
            public void onResponse(Call<DirectionResults> call, Response<DirectionResults> response) {
                ArrayList<LatLng> routelist = new ArrayList<>();
                if (response.body().getRoutes().size() > 0) {
                    ArrayList<LatLng> decodelist;
                    Route routeA = response.body().getRoutes().get(0);
                    if (routeA.getLegs().size() > 0) {
                        List<Steps> steps = routeA.getLegs().get(0).getSteps();
                        Steps step;
                        Distance dist = routeA.getLegs().get(0).getDistance();
                        Duration durt = routeA.getLegs().get(0).getDuration();
                        time = durt.getText();
                        distance = dist.getText();
                        Location location;
                        String polyline;
                        for (int i = 0; i < steps.size(); i++) {
                            step = steps.get(i);
                            location = step.getStart_location();
                            routelist.add(new LatLng(location.getLat(), location.getLng()));
                            polyline = step.getPolyline().getPoints();
                            decodelist = RouteDecode.decodePoly(polyline);
                            routelist.addAll(decodelist);
                            location = step.getEnd_location();
                            routelist.add(new LatLng(location.getLat(), location.getLng()));
                        }
                    }
                }
                if (routelist.size() > 0) {
                    PolylineOptions rectLine = new PolylineOptions().width(10).color(
                            Color.BLUE);

                    for (int i = 0; i < routelist.size(); i++) {
                        rectLine.add(routelist.get(i));
                    }
                    // Adding route on the map
//                    mMap.clear();
                    polylineFinal = mMap.addPolyline(rectLine);
                    markerOptions.position(latLng);
                    markerOptions.draggable(true);
                    markerOptions.title("Euclidean Distance " + computeDistanceBetween(new LatLng(lctn.getLatitude(), lctn.getLongitude()), latLng) + "m");
                    markerOptions.snippet("Duration "+time+" Distance "+distance);
                    mMap.addMarker(markerOptions);
                }

            }

            @Override
            public void onFailure(Call<DirectionResults> call, Throwable t) {

            }
        });
        polyline1 = mMap.addPolyline(new PolylineOptions()
                .add(
                        new LatLng(lctn.getLatitude(), lctn.getLongitude()),
                        latLng));
        polyline1.setColor(Color.RED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRequestingLocationUpdates) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            mRequestingLocationUpdates = false;
        }
    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        if (mMap != null) {
//            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
//            outState.putParcelable(KEY_LOCATION, location);
//            super.onSaveInstanceState(outState);
//        }
//    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fab1:
                    getDeviceLocation(points.get(0));
                    mFusedLocationProviderClient
                            .requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, null);
                    mRequestingLocationUpdates = true;
                    break;
                case R.id.fab2:
                    getDeviceLocation(points.get(1));
                    mFusedLocationProviderClient
                            .requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, null);
                    mRequestingLocationUpdates = true;

                    break;
                case R.id.fab3:
                    getDeviceLocation(points.get(2));
                    mFusedLocationProviderClient
                            .requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, null);
                    mRequestingLocationUpdates = true;
                    break;
                case R.id.fab4:
                    getDeviceLocation(points.get(3));
                    mFusedLocationProviderClient
                            .requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, null);
                    mRequestingLocationUpdates = true;
                    break;
            }
        }
    };

}
