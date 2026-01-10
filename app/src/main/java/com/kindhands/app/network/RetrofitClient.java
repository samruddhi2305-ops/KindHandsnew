package com.kindhands.app.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class RetrofitClient {

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.73.80.94:8080/")
                    // ❌ GsonConverterFactory ONLY nahi
                    .addConverterFactory(ScalarsConverterFactory.create()) // 👈 ADD THIS
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
