package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;import android.widget.Button;
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
            navigateToDashboard(); // Redirect based on stored user type
            return;
        }

        setContentView(R.layout.login);

        // Initialize views
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvGoToRegister);

        // Forgot Password Click Listener
        tvForgotPassword.setOnClickListener(v -> {
            // Navigate to the activity for entering a phone number
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordPhoneActivity.class);
            startActivity(intent);
        });

        // Login Button Click
        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Validation
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

            // CHECK FOR ADMIN LOGIN FIRST (Case Insensitive Email)
            if ("admin@kindhands.com".equalsIgnoreCase(email) && "admin123".equals(password)) {
                Toast.makeText(LoginActivity.this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
                SharedPrefManager.getInstance(LoginActivity.this).saveUser("Admin", "admin@kindhands.com", "ADMIN");
                navigateToDashboard();
                return; // Stop further execution
            }

            // IF NOT ADMIN, TRY DONOR LOGIN
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            User loginUser = new User(email, password);
            Call<User> callDonor = apiService.loginDonor(loginUser);

            callDonor.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        Toast.makeText(LoginActivity.this, "Welcome Donor " + user.getName(), Toast.LENGTH_SHORT).show();

                        SharedPrefManager.getInstance(LoginActivity.this).saveUser(user.getName(), user.getEmail(), "DONOR");
                        navigateToDashboard();
                    } else {
                        // IF DONOR LOGIN FAILS, TRY ORGANIZATION LOGIN
                        tryOrganizationLogin(email, password);
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    // IF NETWORK ERROR OR OTHER ISSUE, TRY ORGANIZATION LOGIN ANYWAY (OR SHOW ERROR)
                    tryOrganizationLogin(email, password);
                }
            });
        });

        // Register Click
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
        });
    }

    private void tryOrganizationLogin(String email, String password) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        OrganizationLoginRequest loginRequest = new OrganizationLoginRequest(email, password);
        Call<Organization> callOrg = apiService.loginOrganization(loginRequest);

        callOrg.enqueue(new Callback<Organization>() {
            @Override
            public void onResponse(Call<Organization> call, Response<Organization> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Organization org = response.body();
                    Toast.makeText(LoginActivity.this, "Welcome " + org.getName(), Toast.LENGTH_SHORT).show();

                    SharedPrefManager.getInstance(LoginActivity.this).saveUser(org.getName(), org.getEmail(), "ORGANIZATION");
                    navigateToDashboard();
                } else {
                    Toast.makeText(LoginActivity.this, "Login Failed: Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Organization> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                    // Replace with your actual Organization Dashboard activity
                    intent = new Intent(LoginActivity.this, OrganizationDashboardActivity.class); 
                    break;
                default:
                    // Fallback, maybe to a generic home screen or back to login
                    intent = new Intent(LoginActivity.this, LoginActivity.class);
                    break;
            }
        } else {
            // If userType is null, default to login
            intent = new Intent(LoginActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
