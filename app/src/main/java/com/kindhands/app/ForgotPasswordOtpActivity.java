package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordOtpActivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerifyOtp;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_otp);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        // ✅ Email from previous screen
        email = getIntent().getStringExtra("EMAIL");

        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this, "Email not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();

            if (otp.isEmpty()) {
                etOtp.setError("OTP is required");
                etOtp.requestFocus();
                return;
            }

            if (otp.length() != 6) {
                etOtp.setError("Enter valid 6-digit OTP");
                etOtp.requestFocus();
                return;
            }

            verifyOtp(email, otp);
        });
    }

    private void verifyOtp(String email, String otp) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.verifyOtp(email, otp).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call,
                                   Response<Map<String, String>> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(
                            ForgotPasswordOtpActivity.this,
                            "OTP Verified Successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    // ✅ IMPORTANT: Send EMAIL + OTP to ResetPasswordActivity
                    Intent intent = new Intent(
                            ForgotPasswordOtpActivity.this,
                            ResetPasswordActivity.class
                    );
                    intent.putExtra("EMAIL", email);
                    intent.putExtra("OTP", otp);
                    startActivity(intent);
                    finish();

                } else {
                    Toast.makeText(
                            ForgotPasswordOtpActivity.this,
                            "Invalid OTP. Please try again.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(
                        ForgotPasswordOtpActivity.this,
                        "Network Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
