package com.example.pulkit.locationdemo2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.pulkit.locationdemo2.MODEL.DirectionResults;
import com.example.pulkit.locationdemo2.MODEL.Location;
import com.example.pulkit.locationdemo2.MODEL.Route;
import com.example.pulkit.locationdemo2.MODEL.Steps;
import com.example.pulkit.locationdemo2.REST.ApiClients;
import com.example.pulkit.locationdemo2.REST.ApiInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
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
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    android.location.Location location;
    List<LatLng> points = new ArrayList<>();
    MarkerOptions markerOptions = new MarkerOptions();
    private CameraPosition mCameraPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (savedInstanceState != null) {
            location = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        points.add(new LatLng(28.6127, 77.2773));
        points.add(new LatLng(28.6129, 77.2295));
        points.add(new LatLng(28.6881, 77.2069));
        points.add(new LatLng(28.5244, 77.1855));
//        getDeviceLocation();
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

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                location = locationResult.getLastLocation();
                points.set(0, new LatLng(location.getLatitude(), location.getLongitude()));
                fetchroutes(location);
            }

        };
    }

    private void fetchroutes(final android.location.Location lctn) {
        ApiInterface apiService = ApiClients.getClient().create(ApiInterface.class);
        Call<DirectionResults> call = apiService.getRoutes(lctn.getLatitude() + "," + lctn.getLongitude(), "28.6881, 77.2069");
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
                    mMap.clear();
                    mMap.addPolyline(rectLine);
                    markerOptions.position(points.get(2));
                    markerOptions.draggable(true);
                    markerOptions.title("Euclidean Distance " + computeDistanceBetween(new LatLng(lctn.getLatitude(), lctn.getLongitude()), points.get(2)) + "m");
                    mMap.addMarker(markerOptions);
                }

            }

            @Override
            public void onFailure(Call<DirectionResults> call, Throwable t) {

            }
        });
        Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                .add(
                        new LatLng(lctn.getLatitude(), lctn.getLongitude()),
                        points.get(2)));
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, location);
            super.onSaveInstanceState(outState);
        }
    }
}
