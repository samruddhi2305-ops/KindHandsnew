package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordPhoneActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_phone);

        etEmail = findViewById(R.id.etEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        btnSendOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Valid email required");
                etEmail.requestFocus();
                return;
            }

            sendOtpToEmail(email);
        });
    }

    private void sendOtpToEmail(String email) {

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.sendOtp(email).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call,
                                   Response<Map<String, String>> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(
                            ForgotPasswordPhoneActivity.this,
                            "OTP sent to " + email,
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent =
                            new Intent(ForgotPasswordPhoneActivity.this,
                                    ForgotPasswordOtpActivity.class);
                    intent.putExtra("EMAIL", email);
                    startActivity(intent);

                } else {
                    Toast.makeText(
                            ForgotPasswordPhoneActivity.this,
                            "Email not found",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(
                        ForgotPasswordPhoneActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
