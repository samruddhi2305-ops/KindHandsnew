package com.kindhands.app.network;

import com.kindhands.app.model.*;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ================= AUTH =================
    @POST("api/auth/register")
    Call<Object> registerUser(@Body User user);

    @POST("api/auth/login")
    Call<User> loginUser(@Body User user);

    @POST("api/auth/forgot-password")
    Call<String> forgotPassword(@Body Map<String, String> mobile);

    @POST("api/auth/verify-otp")
    Call<String> verifyOtp(@Body Map<String, String> otpData);

    @POST("api/auth/reset-password")
    Call<String> resetPassword(@Body Map<String, String> passwordData);


    // ================= DONOR =================
    @POST("api/donors/register")
    Call<User> registerDonor(@Body User user);

    @POST("api/donors/login")
    Call<User> loginDonor(@Body User user);


    // ================= ORGANIZATION =================
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
            @Part MultipartBody.Part document
    );


    @POST("api/organizations/login")
    Call<Organization> loginOrganization(@Body OrganizationLoginRequest loginRequest);


    // ================= ADMIN =================
    @GET("api/organizations/admin/pending")
    Call<List<Organization>> getPendingOrganizations();

    @PUT("api/organizations/admin/{id}/approve")
    Call<Void> approveOrg(@Path("id") Long id);

    @PUT("api/organizations/admin/{id}/reject")
    Call<Void> rejectOrg(@Path("id") Long id);


    // ================= DOCUMENT VIEW =================
    @GET("api/organizations/admin/document/{id}")
    Call<Void> viewDocument(@Path("id") Long id);


    // ================= REQUESTS =================
    @POST("requests/create")
    Call<DonationRequest> createRequest(@Body DonationRequest requestBody);

    @GET("requests/open")
    Call<List<DonationRequest>> getOpenRequests();

    @GET("requests/organization/{orgId}")
    Call<List<DonationRequest>> getOrgRequests(@Path("orgId") Long orgId);

    @PUT("requests/{id}/accept/{donorId}")
    Call<DonationRequest> acceptRequest(
            @Path("id") Long id,
            @Path("donorId") Long donorId
    );

    @PUT("requests/{id}/reject")
    Call<DonationRequest> rejectRequest(@Path("id") Long id);

    @PUT("requests/{id}/delivered")
    Call<DonationRequest> markDelivered(@Path("id") Long id);

    @PUT("requests/{id}/complete")
    Call<DonationRequest> completeRequest(@Path("id") Long id);
}
