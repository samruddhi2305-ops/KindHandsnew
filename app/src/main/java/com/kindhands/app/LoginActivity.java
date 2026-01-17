package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kindhands.app.model.Organization;
import com.kindhands.app.model.OrganizationLoginRequest;
import com.kindhands.app.model.User;
import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;
import com.kindhands.app.utils.SharedPrefManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CHECK IF ALREADY LOGGED IN
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            navigateToDashboard();
            return;
        }

        setContentView(R.layout.login);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvGoToRegister);

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordPhoneActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Password is required");
                etPassword.requestFocus();
                return;
            }

            // ADMIN LOGIN
            if ("admin@kindhands.com".equalsIgnoreCase(email) && "admin123".equals(password)) {
                Toast.makeText(LoginActivity.this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
                SharedPrefManager.getInstance(LoginActivity.this).saveUser("Admin", "admin@kindhands.com", "ADMIN");
                navigateToDashboard();
                return;
            }

            // TRY DONOR LOGIN FIRST
            performDonorLogin(email, password);
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
        });
    }

    private void performDonorLogin(String email, String password) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        // Fix: Use OrganizationLoginRequest instead of User to match the ApiService interface
        OrganizationLoginRequest loginRequest = new OrganizationLoginRequest(email, password);
        
        Log.d("LOGIN_DEBUG", "Attempting Donor Login for: " + email);
        
        Call<User> callDonor = apiService.loginDonor(loginRequest);
        callDonor.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Toast.makeText(LoginActivity.this, "Welcome Donor " + user.getName(), Toast.LENGTH_SHORT).show();
                    SharedPrefManager.getInstance(LoginActivity.this).saveUser(user.getId(), user.getName(), user.getEmail(), "DONOR");
                    navigateToDashboard();
                } else {
                    // If not a donor, try Organization Login
                    Log.d("LOGIN_DEBUG", "Donor login failed (Code: " + response.code() + "), trying Organization login...");
                    performOrganizationLogin(email, password);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("LOGIN_ERROR", "Donor Login Network Failure: " + t.getMessage());
                performOrganizationLogin(email, password);
            }
        });
    }

    private void performOrganizationLogin(String email, String password) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        OrganizationLoginRequest loginRequest = new OrganizationLoginRequest(email, password);
        
        Call<Organization> callOrg = apiService.loginOrganization(loginRequest);
        callOrg.enqueue(new Callback<Organization>() {
            @Override
            public void onResponse(Call<Organization> call, Response<Organization> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Organization org = response.body();
                    
                    // Check if Organization is approved
                    if ("REJECTED".equalsIgnoreCase(org.getStatus())) {
                        Toast.makeText(LoginActivity.this, "Login Failed: Your organization has been rejected.", Toast.LENGTH_LONG).show();
                    } else if (!"APPROVED".equalsIgnoreCase(org.getStatus())) {
                        Toast.makeText(LoginActivity.this, "Login Failed: Your account is pending approval.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Welcome " + org.getName(), Toast.LENGTH_SHORT).show();
                        SharedPrefManager.getInstance(LoginActivity.this).saveUser(org.getId(), org.getName(), org.getEmail(), "ORGANIZATION");
                        navigateToDashboard();
                    }
                } else {
                    String error = "Invalid Credentials";
                    try {
                        if (response.errorBody() != null) {
                            String serverError = response.errorBody().string();
                            Log.e("LOGIN_SERVER_ERROR", serverError);
                            if (serverError.toLowerCase().contains("not approved")) {
                                error = "Account pending admin approval";
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, "Login Failed: " + error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Organization> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDashboard() {
        String userType = SharedPrefManager.getInstance(this).getUserType();
        Intent intent;

        if (userType != null) {
            switch (userType) {
                case "ADMIN":
                    intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                    break;
                case "DONOR":
                    intent = new Intent(LoginActivity.this, AddDonationActivity.class);
                    break;
                case "ORGANIZATION":
                    intent = new Intent(LoginActivity.this, OrganizationDashboardActivity.class);
                    break;
                default:
                    intent = new Intent(LoginActivity.this, LoginActivity.class);
                    break;
            }
        } else {
            intent = new Intent(LoginActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
