package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword, tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Auto-login if already logged in
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            navigateToDashboard();
            return;
        }

        setContentView(R.layout.login);

        // Init views
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvGoToRegister);

        // ✅ Forgot password → Works ONLY for USERS
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordPhoneActivity.class);
            startActivity(intent);
        });

        // Login click
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Valid email required");
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Password required");
                return;
            }

            // 🔐 ADMIN LOGIN (Hardcoded)
            if ("team.kindhands12@gmail.com".equalsIgnoreCase(email)
                    && "#KINDHANDS26".equals(password)) {

                SharedPrefManager.getInstance(this)
                        .saveUser("Admin", email, "ADMIN");

                Toast.makeText(this, "Welcome Admin", Toast.LENGTH_SHORT).show();
                navigateToDashboard();
                return;
            }

            // Try USER/DONOR login first
            loginDonor(email, password);
        });

        // Register
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RoleSelectionActivity.class));
        });
    }

    // ================= USER/DONOR LOGIN =================
    private void loginDonor(String email, String password) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        User user = new User(email, password);

        apiService.loginUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User donor = response.body();
                    SharedPrefManager.getInstance(LoginActivity.this)
                            .saveUser(donor.getName(), donor.getEmail(), "DONOR");

                    Toast.makeText(LoginActivity.this, "Welcome " + donor.getName(), Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                } else {
                    // ❌ User failed → try organization
                    loginOrganization(email, password);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                loginOrganization(email, password);
            }
        });
    }

    // ================= ORGANIZATION LOGIN =================
    private void loginOrganization(String email, String password) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        OrganizationLoginRequest request = new OrganizationLoginRequest(email, password);

        apiService.loginOrganization(request).enqueue(new Callback<Organization>() {
            @Override
            public void onResponse(Call<Organization> call, Response<Organization> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Organization org = response.body();
                    SharedPrefManager.getInstance(LoginActivity.this)
                            .saveUser(org.getName(), org.getEmail(), "ORGANIZATION");

                    Toast.makeText(LoginActivity.this, "Welcome " + org.getName(), Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                } else {
                    String errorMsg = "Invalid email or password";
                    try {
                        if (response.errorBody() != null) {
                            String serverError = response.errorBody().string();
                            if (serverError.contains("Organization not approved")) {
                                errorMsg = "Organization not approved";
                            } else if (serverError.contains("Organization not found")) {
                                errorMsg = "Organization not found";
                            } else if (serverError.contains("Invalid password")) {
                                errorMsg = "Invalid password";
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Organization> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ================= NAVIGATION =================
    private void navigateToDashboard() {
        String role = SharedPrefManager.getInstance(this).getUserType();
        Intent intent;

        if (role == null) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            switch (role) {
                case "ADMIN":
                    intent = new Intent(this, AdminDashboardActivity.class);
                    break;
                case "DONOR":
                    intent = new Intent(this, AddDonationActivity.class);
                    break;
                case "ORGANIZATION":
                    intent = new Intent(this, OrganizationDashboardActivity.class);
                    break;
                default:
                    intent = new Intent(this, LoginActivity.class);
            }
        }
        startActivity(intent);
        finish();
    }
}
