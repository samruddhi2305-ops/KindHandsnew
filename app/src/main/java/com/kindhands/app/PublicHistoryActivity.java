package com.kindhands.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kindhands.app.model.DonationRequest;
import com.kindhands.app.model.Organization;
import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicHistoryActivity extends AppCompatActivity {

    private RecyclerView rvPublicHistory, rvOrganizations;
    private HistoryAdapter historyAdapter;
    private OrgAdapter orgAdapter;
    private List<DonationRequest> historyList = new ArrayList<>();
    private List<Organization> orgList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_history);

        rvPublicHistory = findViewById(R.id.rvPublicHistory);
        rvOrganizations = findViewById(R.id.rvOrganizations);

        rvPublicHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(historyList);
        rvPublicHistory.setAdapter(historyAdapter);

        rvOrganizations.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        orgAdapter = new OrgAdapter(orgList);
        rvOrganizations.setAdapter(orgAdapter);

        fetchData();
    }

    private void fetchData() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        // Fetch Public Donation History
        apiService.getPublicDonationHistory().enqueue(new Callback<List<DonationRequest>>() {
            @Override
            public void onResponse(Call<List<DonationRequest>> call, Response<List<DonationRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    historyList.clear();
                    historyList.addAll(response.body());
                    historyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<DonationRequest>> call, Throwable t) {
                Toast.makeText(PublicHistoryActivity.this, "Error loading history", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch Public Organizations
        apiService.getPublicOrganizations().enqueue(new Callback<List<Organization>>() {
            @Override
            public void onResponse(Call<List<Organization>> call, Response<List<Organization>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orgList.clear();
                    orgList.addAll(response.body());
                    orgAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Organization>> call, Throwable t) {
                Toast.makeText(PublicHistoryActivity.this, "Error loading organizations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- ADAPTER FOR DONATION HISTORY ---
    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<DonationRequest> list;
        public HistoryAdapter(List<DonationRequest> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_donor_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DonationRequest req = list.get(position);
            holder.tvDonationDetails.setText("Donated: " + req.getQuantity() + " " + req.getCategory());
            holder.tvStatus.setText("Status: " + req.getStatus());
            // Note: In a real app, these names would come from the backend response
            holder.tvDonorName.setText("Donor: Anonymous"); 
            holder.tvOrgName.setText("To: Registered Org");
            holder.tvDate.setText("Date: " + (req.getOtherDetails() != null ? req.getOtherDetails() : "N/A"));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDonorName, tvDonationDetails, tvOrgName, tvDate, tvStatus;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDonorName = itemView.findViewById(R.id.tvDonorName);
                tvDonationDetails = itemView.findViewById(R.id.tvDonationDetails);
                tvOrgName = itemView.findViewById(R.id.tvOrgName);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }

    // --- ADAPTER FOR ORGANIZATION INFO ---
    private class OrgAdapter extends RecyclerView.Adapter<OrgAdapter.ViewHolder> {
        private List<Organization> list;
        public OrgAdapter(List<Organization> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_org_info, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Organization org = list.get(position);
            holder.tvOrgName.setText(org.getName());
            holder.tvOrgType.setText("Type: " + org.getType());
            holder.tvOrgAddress.setText("Address: " + org.getAddress());
            holder.tvOrgContact.setText("Contact: " + org.getContact());
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvOrgName, tvOrgType, tvOrgAddress, tvOrgContact;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvOrgName = itemView.findViewById(R.id.tvOrgName);
                tvOrgType = itemView.findViewById(R.id.tvOrgType);
                tvOrgAddress = itemView.findViewById(R.id.tvOrgAddress);
                tvOrgContact = itemView.findViewById(R.id.tvOrgContact);
            }
        }
    }
}
