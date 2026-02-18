package com.kindhands.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;

import org.json.JSONObject;

import java.io.*;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterOrganizationActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etContact, etAddress, etPincode;
    Spinner spinnerType;
    TextView tvFile;
    Button btnUpload, btnRegister;

    String selectedFilePath = null;
    ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_organization);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etName = findViewById(R.id.etOrgName);
        etEmail = findViewById(R.id.etOrgEmail);
        etPassword = findViewById(R.id.etOrgPassword);
        etContact = findViewById(R.id.etOrgContact);
        etAddress = findViewById(R.id.etOrgAddress);
        etPincode = findViewById(R.id.etOrgPincode);

        spinnerType = findViewById(R.id.spinnerOrgType);
        tvFile = findViewById(R.id.tvSelectedFileName);

        btnUpload = findViewById(R.id.btnUploadDocument);
        btnRegister = findViewById(R.id.btnOrgRegister);

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        File file = copyUriToFile(uri);
                        if (file != null) {
                            selectedFilePath = file.getAbsolutePath();
                            tvFile.setText(file.getName());
                        }
                    }
                }
        );

        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            launcher.launch(intent);
        });

        btnRegister.setOnClickListener(v -> register());
    }

    private File copyUriToFile(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            File file = new File(getCacheDir(), "doc_" + System.currentTimeMillis() + ".pdf");
            FileOutputStream out = new FileOutputStream(file);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || contact.isEmpty() || address.isEmpty() || pincode.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (selectedFilePath == null) {
            Toast.makeText(this, "Please upload certificate in PDF format only", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(selectedFilePath);
        String rawType = spinnerType.getSelectedItem().toString();
        String type = rawType.equalsIgnoreCase("Orphanage") ? "ORPHANAGE" : "OLD_AGE_HOME";

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        // Convert all fields to MultipartBody.Part for explicit sending
        MultipartBody.Part pName = createPart("name", name);
        MultipartBody.Part pEmail = createPart("email", email);
        MultipartBody.Part pPassword = createPart("password", password);
        MultipartBody.Part pContact = createPart("contact", contact);
        MultipartBody.Part pType = createPart("type", type);
        MultipartBody.Part pAddress = createPart("address", address);
        MultipartBody.Part pPincode = createPart("pincode", pincode);
        MultipartBody.Part pUserId = createPart("userId", "0");

        // Document part
        RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), file);
        MultipartBody.Part pDocument = MultipartBody.Part.createFormData("document", file.getName(), requestFile);

        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        Call<String> call = api.registerOrganization(
                pName, pEmail, pPassword, pContact, pType, pAddress, pPincode, pUserId, pDocument
        );

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register Organization");
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterOrganizationActivity.this, "Registration Successful! Pending approval.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "{}";
                        Log.e("ORG_REGISTER_ERROR", "Error: " + errorBody);
                        
                        JSONObject errorJson = new JSONObject(errorBody);
                        String msg = errorJson.optString("message", "Error " + response.code());
                        Toast.makeText(RegisterOrganizationActivity.this, msg, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(RegisterOrganizationActivity.this, "Registration Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register Organization");
                Log.e("ORG_REGISTER_FAIL", t.getMessage(), t);
                Toast.makeText(RegisterOrganizationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MultipartBody.Part createPart(String partName, String value) {
        return MultipartBody.Part.createFormData(partName, value);
    }
}
