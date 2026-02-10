package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.kindhands.app.model.DonationRequest;
import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;
import com.kindhands.app.utils.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDonationActivity extends AppCompatActivity {

    private TextView tvRequirements;
    private SwitchMaterial switchPublicHistory;
    private Button btnViewPublicHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_donation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvRequirements = findViewById(R.id.tvOrgRequirements);
        switchPublicHistory = findViewById(R.id.switchPublicHistory);
        btnViewPublicHistory = findViewById(R.id.btnViewPublicHistory);

        // Logout Logic
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
        
        // Privacy Toggle
        if (switchPublicHistory != null) {
            switchPublicHistory.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updatePrivacy(isChecked);
            });
        }

        // View Public History
        if (btnViewPublicHistory != null) {
            btnViewPublicHistory.setOnClickListener(v -> {
                startActivity(new Intent(this, PublicHistoryActivity.class));
            });
        }

        fetchRequirements();
    }

    private void updatePrivacy(boolean isPublic) {
        String email = SharedPrefManager.getInstance(this).getUserEmail();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.updateDonorPrivacy(email, isPublic).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddDonationActivity.this, "Privacy updated", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddDonationActivity.this, "Failed to update privacy", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRequirements() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<DonationRequest>> call = apiService.getOpenRequests();
        
        call.enqueue(new Callback<List<DonationRequest>>() {
            @Override
            public void onResponse(Call<List<DonationRequest>> call, Response<List<DonationRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StringBuilder reqText = new StringBuilder();
                    boolean found = false;
                    
                    for (DonationRequest req : response.body()) {
                        if ("REQUIREMENT".equalsIgnoreCase(req.getCategory())) {
                            found = true;
                            String orgName = req.getOtherDetails() != null ? req.getOtherDetails() : "Organization";
                            reqText.append("• ").append(orgName).append(": ").append(req.getDetails()).append("\n");
                        }
                    }
                    
                    if (found && tvRequirements != null) {
                        tvRequirements.setText(reqText.toString());
                    } else if (tvRequirements != null) {
                        tvRequirements.setText("No current needs from organizations.");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<DonationRequest>> call, Throwable t) {
                 if (tvRequirements != null) {
                        tvRequirements.setText("Could not load organization needs.");
                 }
            }
        });
    }
}
