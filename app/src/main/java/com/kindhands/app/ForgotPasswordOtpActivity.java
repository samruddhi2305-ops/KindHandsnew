package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordOtpActivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerifyOtp;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_otp);

        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);

        // Get the phone number passed from the previous activity
        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = etOtp.getText().toString().trim();

            if (otp.isEmpty() || otp.length() != 6) {
                etOtp.setError("Enter a valid 6-digit OTP");
                etOtp.requestFocus();
                return;
            }

            // --- TODO: Implement OTP verification logic here ---
            // For now, we'll assume the OTP is correct and navigate to reset password screen.
            Toast.makeText(this, "OTP Verified!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ForgotPasswordOtpActivity.this, ResetPasswordActivity.class);
            intent.putExtra("PHONE_NUMBER", phoneNumber); // Pass the phone number along
            startActivity(intent);
        });
    }
}
