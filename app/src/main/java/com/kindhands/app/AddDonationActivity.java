package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.kindhands.app.model.DonationRequest;
import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;
import com.kindhands.app.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDonationActivity extends AppCompatActivity {

    private RecyclerView rvRequirements;
    private RequirementAdapter adapter;
    private List<DonationRequest> requirementList = new ArrayList<>();
    
    private SwitchMaterial switchPublicHistory;
    private Button btnViewPublicHistory;
    private TextView tvEmptyMessage;

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
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        rvRequirements = findViewById(R.id.rvRequirements);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        switchPublicHistory = findViewById(R.id.switchPublicHistory);
        btnViewPublicHistory = findViewById(R.id.btnViewPublicHistory);

        rvRequirements.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequirementAdapter(requirementList);
        rvRequirements.setAdapter(adapter);

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
        // Using getOpenRequests which maps to api/requests/pending in backend
        apiService.getOpenRequests().enqueue(new Callback<List<DonationRequest>>() {
            @Override
            public void onResponse(Call<List<DonationRequest>> call, Response<List<DonationRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    requirementList.clear();
                    for (DonationRequest req : response.body()) {
                        if ("REQUIREMENT".equalsIgnoreCase(req.getCategory())) {
                            requirementList.add(req);
                        }
                    }
                    
                    if (requirementList.isEmpty()) {
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                        rvRequirements.setVisibility(View.GONE);
                    } else {
                        tvEmptyMessage.setVisibility(View.GONE);
                        rvRequirements.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<DonationRequest>> call, Throwable t) {
                Toast.makeText(AddDonationActivity.this, "Could not load needs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter for requirements
    private class RequirementAdapter extends RecyclerView.Adapter<RequirementAdapter.VH> {
        private List<DonationRequest> list;
        public RequirementAdapter(List<DonationRequest> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_org_requirement, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            DonationRequest req = list.get(pos);
            h.details.setText(req.getDetails());
            
            // Display organization info stored in 'otherDetails' (as set in OrganizationDashboardActivity)
            String orgName = req.getOtherDetails() != null ? req.getOtherDetails() : "Organization";
            h.orgInfo.setText("Posted by: " + orgName);

            h.btnDonate.setOnClickListener(v -> {
                // Future: Navigate to a donation form for this specific requirement
                Toast.makeText(AddDonationActivity.this, "Thank you! Processing donation for " + orgName, Toast.LENGTH_LONG).show();
            });
            
            // Hide admin buttons if they exist in the layout but aren't needed for donor
            if (h.btnDisapprove != null) h.btnDisapprove.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView details, orgInfo;
            Button btnDonate, btnDisapprove;
            VH(View v) {
                super(v);
                details = v.findViewById(R.id.tvReqDetails);
                orgInfo = v.findViewById(R.id.tvOrgInfo);
                btnDonate = v.findViewById(R.id.btnApproveReq); // Repurposing as 'Donate' button
                btnDisapprove = v.findViewById(R.id.btnDisapproveReq);
                
                if (btnDonate != null) btnDonate.setText("I want to Donate");
            }
        }
    }
}
