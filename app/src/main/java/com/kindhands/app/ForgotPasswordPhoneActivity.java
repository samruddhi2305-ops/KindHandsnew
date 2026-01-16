package com.kindhands.app;

import android.content.Intent; // <-- ADD THIS LINE
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordPhoneActivity extends AppCompatActivity {

    private EditText etPhoneNumber;
    private Button btnSendOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to your XML layout
        setContentView(R.layout.activity_forgot_password_phone);

        // Initialize the views from your layout
        etPhoneNumber = findViewById(R.id.etPhoneNumber); // Make sure this ID matches your EditText in the XML
        btnSendOtp = findViewById(R.id.btnSendOtp);       // Make sure this ID matches your Button in the XML

        // In ForgotPasswordPhoneActivity.java

        btnSendOtp.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();

            if (phoneNumber.isEmpty() || phoneNumber.length() < 10) {
                etPhoneNumber.setError("A valid phone number is required");
                etPhoneNumber.requestFocus();
                return; // This return statement stops execution if the phone number is invalid
            }

            // --- TODO: Implement actual OTP sending logic here ---
            Toast.makeText(this, "Sending OTP to " + phoneNumber, Toast.LENGTH_SHORT).show();

            // Create the intent to navigate to the OTP screen
            // All the errors on the lines below will disappear after adding the import.
            Intent intent = new Intent(ForgotPasswordPhoneActivity.this, ForgotPasswordOtpActivity.class);
            intent.putExtra("PHONE_NUMBER", phoneNumber); // Pass the phone number

            // ***** THIS IS THE CRUCIAL LINE *****
            startActivity(intent); // Make sure this line exists and is being called
        });
    }
}
