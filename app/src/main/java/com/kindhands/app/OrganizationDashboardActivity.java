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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_dashboard);

        recyclerView = findViewById(R.id.rvDonationRequests);
        btnLogout = findViewById(R.id.btnLogoutOrg);
        
        etReqDescription = findViewById(R.id.etReqDescription);
        btnPostReq = findViewById(R.id.btnPostReq);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DonationAdapter(donationList);
        recyclerView.setAdapter(adapter);

        fetchDonorDonations();

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

        Long orgId = SharedPrefManager.getInstance(this).getUserId();
        String orgName = SharedPrefManager.getInstance(this).getUserName();
        String orgContact = SharedPrefManager.getInstance(this).getUserContact();
        String orgAddress = SharedPrefManager.getInstance(this).getUserAddress();

        DonationRequest request = new DonationRequest();
        request.setCategory("REQUIREMENT");
        request.setDetails(description); 
        request.setQuantity(1);
        
        // Pack all info into otherDetails
        String fullInfo = orgName + " | Contact: " + orgContact + " | Address: " + orgAddress;
        request.setOtherDetails(fullInfo);
        
        request.setStatus("OPEN");
        request.setOrganizationId(orgId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<DonationRequest> call = apiService.createRequest(request);

        btnPostReq.setEnabled(false);
        call.enqueue(new Callback<DonationRequest>() {
            @Override
            public void onResponse(Call<DonationRequest> call, Response<DonationRequest> response) {
                btnPostReq.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(OrganizationDashboardActivity.this, "Requirement Posted Successfully!", Toast.LENGTH_LONG).show();
                    etReqDescription.setText(""); 
                } else {
                    Toast.makeText(OrganizationDashboardActivity.this, "Failed to post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DonationRequest> call, Throwable t) {
                btnPostReq.setEnabled(true);
                Toast.makeText(OrganizationDashboardActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDonorDonations() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<List<DonationRequest>> call = apiService.getOpenRequests();

        call.enqueue(new Callback<List<DonationRequest>>() {
            @Override
            public void onResponse(Call<List<DonationRequest>> call, Response<List<DonationRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    donationList.clear();
                    for (DonationRequest req : response.body()) {
                        if (!"REQUIREMENT".equalsIgnoreCase(req.getCategory())) {
                            donationList.add(req);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<DonationRequest>> call, Throwable t) {
                Log.e("FETCH_ERROR", t.getMessage());
            }
        });
    }

    private class DonationAdapter extends RecyclerView.Adapter<DonationAdapter.DonationViewHolder> {
        private List<DonationRequest> list;
        public DonationAdapter(List<DonationRequest> list) { this.list = list; }

        @NonNull
        @Override
        public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_donation_offer, parent, false);
            return new DonationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DonationViewHolder holder, int position) {
            DonationRequest donation = list.get(position);
            holder.tvCategory.setText(donation.getCategory() != null ? donation.getCategory() : "Donation");
            holder.tvDetails.setText(donation.getDetails());
            holder.tvDonorName.setText("Status: " + donation.getStatus()); 

            if ("clothes".equalsIgnoreCase(donation.getCategory())) {
                holder.imgIcon.setImageResource(R.drawable.img_cloths);
            } else if ("food".equalsIgnoreCase(donation.getCategory())) {
                holder.imgIcon.setImageResource(R.drawable.img_food);
            } else if ("books".equalsIgnoreCase(donation.getCategory())) {
                holder.imgIcon.setImageResource(R.drawable.img_books);
            } else {
                holder.imgIcon.setImageResource(R.drawable.ic_launcher_foreground);
            }
            
            holder.btnAccept.setOnClickListener(v -> Toast.makeText(OrganizationDashboardActivity.this, "Accept Clicked", Toast.LENGTH_SHORT).show());
            holder.btnReject.setOnClickListener(v -> Toast.makeText(OrganizationDashboardActivity.this, "Reject Clicked", Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() { return list.size(); }

        class DonationViewHolder extends RecyclerView.ViewHolder {
            ImageView imgIcon;
            TextView tvCategory, tvDonorName, tvDetails;
            Button btnAccept, btnReject;
            public DonationViewHolder(@NonNull View itemView) {
                super(itemView);
                imgIcon = itemView.findViewById(R.id.imgDonationCategory);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvDonorName = itemView.findViewById(R.id.tvDonorName);
                tvDetails = itemView.findViewById(R.id.tvDetails);
                btnAccept = itemView.findViewById(R.id.btnAccept);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }
}
