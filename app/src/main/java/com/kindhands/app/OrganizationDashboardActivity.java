package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kindhands.app.model.DonationRequest;
import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;
import com.kindhands.app.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrganizationDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DonationAdapter adapter;
    private List<DonationRequest> donationList = new ArrayList<>();
    private Button btnLogout, btnPostReq;
    private EditText etReqDescription;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_dashboard);

        apiService = RetrofitClient.getClient().create(ApiService.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        recyclerView = findViewById(R.id.rvDonationRequests);
        btnLogout = findViewById(R.id.btnLogoutOrg);
        etReqDescription = findViewById(R.id.etReqDescription);
        btnPostReq = findViewById(R.id.btnPostReq);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DonationAdapter(donationList);
        recyclerView.setAdapter(adapter);

        fetchOpenDonations();

        btnLogout.setOnClickListener(v -> {
            SharedPrefManager.getInstance(this).logout();
            Intent intent = new Intent(OrganizationDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        
        btnPostReq.setOnClickListener(v -> postRequirement());
    }

    private void postRequirement() {
        String description = etReqDescription.getText().toString().trim();
        
        if (description.isEmpty()) {
            Toast.makeText(this, "Please describe your requirement", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPostReq.setEnabled(false);
        btnPostReq.setText("Posting...");

        // Create request with current Organization's ID and Name
        Long orgId = SharedPrefManager.getInstance(this).getUserId();
        String orgName = SharedPrefManager.getInstance(this).getUserName();

        DonationRequest request = new DonationRequest("REQUIREMENT", description, 1, orgName);
        request.setOrganizationId(orgId);
        request.setStatus("OPEN");

        apiService.createRequest(request).enqueue(new Callback<DonationRequest>() {
            @Override
            public void onResponse(Call<DonationRequest> call, Response<DonationRequest> response) {
                btnPostReq.setEnabled(true);
                btnPostReq.setText("Submit Requirement");
                
                if (response.isSuccessful()) {
                    Toast.makeText(OrganizationDashboardActivity.this, "Requirement Posted Successfully!", Toast.LENGTH_LONG).show();
                    etReqDescription.setText(""); 
                    fetchOpenDonations(); // Refresh list to show new post
                } else {
                    Toast.makeText(OrganizationDashboardActivity.this, "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DonationRequest> call, Throwable t) {
                btnPostReq.setEnabled(true);
                btnPostReq.setText("Submit Requirement");
                Toast.makeText(OrganizationDashboardActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOpenDonations() {
        apiService.getOpenRequests().enqueue(new Callback<List<DonationRequest>>() {
            @Override
            public void onResponse(Call<List<DonationRequest>> call, Response<List<DonationRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    donationList.clear();
                    donationList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<DonationRequest>> call, Throwable t) {
                Log.e("ORG_DASHBOARD", "Fetch error", t);
            }
        });
    }

    private class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.VH> {
        private List<DonationRequest> list;
        public DonationAdapter(List<DonationRequest> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(getLayoutInflater().inflate(R.layout.item_donation_offer, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            DonationRequest d = list.get(pos);
            h.tvCategory.setText(d.getCategory());
            h.tvDetails.setText(d.getDetails());
            h.tvDonorName.setText("Role: " + (d.getOtherDetails() != null ? d.getOtherDetails() : "Unknown"));

            h.btnAccept.setOnClickListener(v -> Toast.makeText(OrganizationDashboardActivity.this, "Accepting...", Toast.LENGTH_SHORT).show());
            h.btnReject.setOnClickListener(v -> Toast.makeText(OrganizationDashboardActivity.this, "Declining...", Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            ImageView imgIcon;
            TextView tvCategory, tvDonorName, tvDetails;
            Button btnAccept, btnReject;
            VH(View v) {
                super(v);
                imgIcon = v.findViewById(R.id.imgDonationCategory);
                tvCategory = v.findViewById(R.id.tvCategory);
                tvDonorName = v.findViewById(R.id.tvDonorName);
                tvDetails = v.findViewById(R.id.tvDetails);
                btnAccept = v.findViewById(R.id.btnAccept);
                btnReject = v.findViewById(R.id.btnReject);
            }
        }
    }
}
