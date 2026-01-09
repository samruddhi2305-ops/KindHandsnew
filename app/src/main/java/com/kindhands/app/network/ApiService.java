package com.kindhands.app.network;

import com.kindhands.app.model.*;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // DONOR
    @POST("api/donors/register")
    Call<User> registerDonor(@Body User user);

    @POST("api/donors/login")
    Call<User> loginDonor(@Body User user);

    // ORGANIZATION
    @Multipart
    @POST("api/organizations/register")
    Call<String> registerOrganization(
            @Part("name") RequestBody name,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password,
            @Part("contact") RequestBody contact,
            @Part("type") RequestBody type,
            @Part("address") RequestBody address,
            @Part("pincode") RequestBody pincode,
            @Part("userId") RequestBody userId,
            @Part MultipartBody.Part document
    );

    // ADMIN
    @GET("api/organizations/admin/pending")
    Call<List<Organization>> getPendingOrganizations();

    @PUT("api/organizations/admin/{id}/approve")
    Call<Void> approve(@Path("id") Long id);

    @PUT("api/organizations/admin/{id}/reject")
    Call<Void> reject(@Path("id") Long id);

    @GET("api/organizations/admin/document/{id}")
    Call<ResponseBody> viewDoc(@Path("id") Long id);

    @GET("/api/organizations/admin/pending")
    Call<List<DonationRequest>> getOpenRequests();


    @PUT("api/organizations/admin/{id}/approve")
            Call<Void> approveOrg(Long id);
    @PUT("api/organizations/admin/{id}/reject")
    Call<Void> rejectOrg(Long id);

    Call<DonationRequest> createRequest(DonationRequest request);

    Call<Organization> loginOrganization(OrganizationLoginRequest loginRequest);

    Call<Object> registerUser(User newUser);
}
