package com.kindhands.app.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    // ⚠️ PC / Laptop IP (Backend running on this IP)
    // WiFi बदलला तर हा IP update करावा लागतो
    private static final String BASE_URL = "http://192.168.31.148:8081/";

    private RetrofitClient() {
        // ❌ prevent object creation
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            // For plain string responses (like "OTP sent")
                            .addConverterFactory(ScalarsConverterFactory.create())
                            // For JSON responses
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }
}
