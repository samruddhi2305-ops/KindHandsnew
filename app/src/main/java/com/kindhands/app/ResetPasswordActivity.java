package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
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

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        // ✅ EMAIL from previous screen
        email = getIntent().getStringExtra("EMAIL");

        btnResetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (newPassword.isEmpty()) {
                etNewPassword.setError("Password cannot be empty");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError("Passwords do not match");
                return;
            }

            resetPasswordApi(email, newPassword);
        });
    }

    private void resetPasswordApi(String email, String newPassword) {

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Map<String, String> data = new HashMap<>();
        data.put("email", email);
        data.put("newPassword", newPassword);

        apiService.resetPassword(data).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call,
                                   Response<Map<String, String>> response) {

                if (response.isSuccessful()) {
                    Toast.makeText(ResetPasswordActivity.this,
                            "Password reset successful!",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(
                            ResetPasswordActivity.this,
                            LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(ResetPasswordActivity.this,
                            "Failed to reset password",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(ResetPasswordActivity.this,
                        "Network Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
