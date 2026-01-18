package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kindhands.app.model.DonationRequest;
import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;
import com.kindhands.app.utils.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDonationActivity extends AppCompatActivity {

    private LinearLayout layoutRequirements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_donation);

        layoutRequirements = findViewById(R.id.containerRequirements);

        // Find CardViews
        View clothes = findViewById(R.id.cardClothes);
        View food = findViewById(R.id.cardFood);
        View books = findViewById(R.id.cardBooks);
        View medical = findViewById(R.id.cardMedical);
        View toys = findViewById(R.id.cardToys);
        View stationery = findViewById(R.id.cardStationery);

        if (clothes != null) clothes.setOnClickListener(v -> openForm("clothes"));
        if (food != null) food.setOnClickListener(v -> openForm("food"));
        if (books != null) books.setOnClickListener(v -> openForm("books"));
        if (medical != null) medical.setOnClickListener(v -> openForm("medical"));
        if (toys != null) toys.setOnClickListener(v -> openForm("toys"));
        if (stationery != null) stationery.setOnClickListener(v -> openForm("stationery"));

        Button btnLogout = findViewById(R.id.btnLogout); 
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPrefManager.getInstance(this).logout();
                Intent intent = new Intent(AddDonationActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
        
        fetchRequirements();
    }

    private void fetchRequirements() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<DonationRequest>> call = apiService.getOpenRequests();
        
        Log.d("FETCH_DEBUG", "Requesting requirements from server...");

        call.enqueue(new Callback<List<DonationRequest>>() {
            @Override
            public void onResponse(Call<List<DonationRequest>> call, Response<List<DonationRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    layoutRequirements.removeAllViews();
                    boolean found = false;
                    
                    Log.d("FETCH_DEBUG", "Total items received: " + response.body().size());

                    for (DonationRequest req : response.body()) {
                        // LOG THE OTHERDETAILS FIELD TO VERIFY BACKEND DATA
                        Log.d("FETCH_DEBUG", "Item: Category=" + req.getCategory() + 
                                ", Status=" + req.getStatus() + 
                                ", OtherDetails=" + req.getOtherDetails());
                        
                        if ("REQUIREMENT".equalsIgnoreCase(req.getCategory()) || 
                           (req.getDonorId() == null && "OPEN".equalsIgnoreCase(req.getStatus()))) {
                            found = true;
                            addRequirementItem(req);
                        }
                    }
                    
                    if (!found) {
                        TextView tvEmpty = new TextView(AddDonationActivity.this);
                        tvEmpty.setText("No current needs from organizations.");
                        tvEmpty.setPadding(20, 40, 20, 40);
                        tvEmpty.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        layoutRequirements.addView(tvEmpty);
                    }
                } else {
                    Log.e("FETCH_DEBUG", "Server returned error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<DonationRequest>> call, Throwable t) {
                Log.e("FETCH_DEBUG", "Network failure: " + t.getMessage());
                Toast.makeText(AddDonationActivity.this, "Failed to load needs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addRequirementItem(DonationRequest req) {
        View itemView = getLayoutInflater().inflate(R.layout.item_org_requirement, layoutRequirements, false);
        
        TextView tvDetails = itemView.findViewById(R.id.tvReqDetails);
        TextView tvOrgInfo = itemView.findViewById(R.id.tvOrgInfo);
        Button btnApprove = itemView.findViewById(R.id.btnApproveReq);
        Button btnDisapprove = itemView.findViewById(R.id.btnDisapproveReq);

        String desc = req.getDetails() != null ? req.getDetails() : (req.getDescription() != null ? req.getDescription() : "No description");
        tvDetails.setText("Need: " + desc);
        
        // If this still shows N/A, check the Logcat log for 'FETCH_DEBUG'
        tvOrgInfo.setText(req.getOtherDetails() != null ? req.getOtherDetails() : "Organization Info N/A");

        btnApprove.setOnClickListener(v -> {
            Toast.makeText(this, "Thank you! We will notify the organization.", Toast.LENGTH_LONG).show();
            layoutRequirements.removeView(itemView);
        });

        btnDisapprove.setOnClickListener(v -> {
            layoutRequirements.removeView(itemView);
        });

        layoutRequirements.addView(itemView);
    }

    private void openForm(String category) {
        Intent intent = new Intent(this, DonationDetailsActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }
}
