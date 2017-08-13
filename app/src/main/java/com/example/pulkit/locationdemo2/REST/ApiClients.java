package com.example.pulkit.locationdemo2.REST;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Pulkit on 8/13/2017.
 */

public class ApiClients {

    public static final String BASE_URL = "http://maps.googleapis.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(){
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
