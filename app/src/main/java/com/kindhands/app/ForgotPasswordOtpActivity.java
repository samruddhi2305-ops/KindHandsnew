package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        // Get the email passed from the previous activity
        email = getIntent().getStringExtra("EMAIL");

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();

            if (otp.isEmpty() || otp.length() != 6) {
                etOtp.setError("Enter a valid 6-digit OTP");
                etOtp.requestFocus();
                return;
            }

            verifyOtpFromServer(otp);
        });
    }
    
    private void verifyOtpFromServer(String otp) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.verifyOtp(email, otp).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ForgotPasswordOtpActivity.this, "OTP Verified!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPasswordOtpActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("EMAIL", email); 
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(ForgotPasswordOtpActivity.this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(ForgotPasswordOtpActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
