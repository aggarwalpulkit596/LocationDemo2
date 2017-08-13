package com.example.pulkit.locationdemo2.REST;

import com.example.pulkit.locationdemo2.MODEL.DirectionResults;
import com.google.android.gms.maps.model.LatLng;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Pulkit on 8/13/2017.
 */

public interface ApiInterface {

    @GET("/maps/api/directions/json")
    Call<DirectionResults> getRoutes(@Query("origin") String origin, @Query("destination") String destination);
}
