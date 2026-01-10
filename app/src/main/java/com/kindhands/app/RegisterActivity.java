package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.kindhands.app.model.User;
import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etAddress, etPincode, etPassword;
    private Spinner spinnerGender;
    private Button btnRegister;
    private TextView tvGoToLogin, tvGoToOrgRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        etName = findViewById(R.id.etRegisterName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPhone = findViewById(R.id.etRegisterPhone);
        etAddress = findViewById(R.id.etRegisterAddress);
        etPincode = findViewById(R.id.etRegisterPincode);
        etPassword = findViewById(R.id.etRegisterPassword);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToOrgRegister = findViewById(R.id.tvGoToOrgRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders);
        spinnerGender.setAdapter(adapter);

        tvGoToOrgRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterOrganizationActivity.class)));

        btnRegister.setOnClickListener(v -> registerUser());

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        if (!validateInputs()) return;

        User newUser = new User(
                etName.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                etPassword.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                etAddress.getText().toString().trim(),
                etPincode.getText().toString().trim(),
                spinnerGender.getSelectedItem().toString(),
                "DONOR"
        );

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Call<Map<String, String>> call = apiService.registerUser(newUser);

        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call,
                                   Response<Map<String, String>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(
                            RegisterActivity.this,
                            response.body().get("message"),
                            Toast.LENGTH_LONG
                    ).show();

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    Toast.makeText(
                            RegisterActivity.this,
                            "Registration Failed : " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(
                        RegisterActivity.this,
                        "Network Error : " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                Log.e("REGISTER_ERROR", t.getMessage(), t);
            }
        });
    }

    private boolean validateInputs() {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("Name required");
            return false;
        }
        if (etEmail.getText().toString().trim().isEmpty()) {
            etEmail.setError("Email required");
            return false;
        }
        if (etPhone.getText().toString().trim().length() < 10) {
            etPhone.setError("Valid phone required");
            return false;
        }
        if (etPassword.getText().toString().trim().length() < 6) {
            etPassword.setError("Min 6 chars password");
            return false;
        }
        return true;
    }
}
