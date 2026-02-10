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

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Valid email required");
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Password required");
                return;
            }

            if ("team.kindhands12@gmail.com".equalsIgnoreCase(email) && "#KINDHANDS26".equals(password)) {
                SharedPrefManager.getInstance(this).saveUser(0L, "Admin", email, "ADMIN");
                Toast.makeText(this, "Welcome Admin", Toast.LENGTH_SHORT).show();
                navigateToDashboard();
                return;
            }

            loginUser(email, password);
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RoleSelectionActivity.class));
        });
    }

    private void loginUser(String email, String password) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        User user = new User(email, password);

        apiService.loginUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User loggedInUser = response.body();
                    SharedPrefManager.getInstance(LoginActivity.this)
                            .saveUser(loggedInUser.getId(), loggedInUser.getName(), loggedInUser.getEmail(), "DONOR");

                    Toast.makeText(LoginActivity.this, "Welcome " + loggedInUser.getName(), Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                } else {
                    // User login failed, now try organization login
                    Long userId = SharedPrefManager.getInstance(LoginActivity.this).getUserId();
                    if(userId != -1L) { // Check if a user is logged in to get the userId
                        loginOrganization(userId, password);
                    } else {
                         Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginOrganization(Long userId, String password) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        OrganizationLoginRequest request = new OrganizationLoginRequest(userId, password);

        apiService.loginOrganization(request).enqueue(new Callback<Organization>() {
            @Override
            public void onResponse(Call<Organization> call, Response<Organization> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Organization org = response.body();
                    SharedPrefManager.getInstance(LoginActivity.this)
                            .saveUser(org.getUserId(), org.getName(), org.getEmail(), "ORGANIZATION");

                    Toast.makeText(LoginActivity.this, "Welcome " + org.getName(), Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                } else {
                    String errorMsg = "Invalid credentials";
                    if (response.code() == 401 || response.code() == 403) {
                        try {
                            if (response.errorBody() != null) {
                                String serverError = response.errorBody().string();
                                if (serverError.contains("not approved")) {
                                    errorMsg = "Organization not approved";
                                } else {
                                    errorMsg = "Invalid User ID or Password";
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
