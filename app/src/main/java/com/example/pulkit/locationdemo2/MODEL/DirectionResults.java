package com.example.pulkit.locationdemo2.MODEL;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Pulkit on 8/13/2017.
 */

public class DirectionResults {
    @SerializedName("routes")
    private List<Route> routes;

    public List<Route> getRoutes() {
        return routes;
    }
}

