package com.kindhands.app;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.annotations.SerializedName;
import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendOtp;

    public class ApiResponse {
        @SerializedName("message")
        private String message;

        public String getMessage() {
            return message;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_phone);

        etEmail = findViewById(R.id.etEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        btnSendOtp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter valid email");
                etEmail.requestFocus();
                return;
            }

            sendForgotPasswordRequest(email);
        });
    }

    private void sendForgotPasswordRequest(String email) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.sendOtp(email).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(
                            ForgotPasswordActivity.this,
                            response.body().get("message"),
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                     Toast.makeText(
                            ForgotPasswordActivity.this,
                            "Email not registered",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(
                        ForgotPasswordActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
