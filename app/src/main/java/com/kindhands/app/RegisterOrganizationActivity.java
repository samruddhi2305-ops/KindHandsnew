package com.kindhands.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.*;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.kindhands.app.network.ApiService;
import com.kindhands.app.network.RetrofitClient;

import java.io.*;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterOrganizationActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etContact, etAddress, etPincode;
    private Spinner spinnerType;
    private TextView tvFile;
    private Button btnUpload, btnRegister;

    private String selectedFilePath = null;
    private ActivityResultLauncher<Intent> launcher;

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
        
        // Modern way to handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

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
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            if (in == null) return null;
            File file = new File(getCacheDir(), "doc_" + System.currentTimeMillis() + ".pdf");
            try (FileOutputStream out = new FileOutputStream(file)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            return file;
        } catch (Exception e) {
            Log.e("FILE_ERROR", "Error copying file", e);
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
        String type = "ORPHANAGE"; // Always ORPHANAGE as requested

        ApiService api = RetrofitClient.getClient().create(ApiService.class);

        MultipartBody.Part pName = MultipartBody.Part.createFormData("name", name);
        MultipartBody.Part pEmail = MultipartBody.Part.createFormData("email", email);
        MultipartBody.Part pPassword = MultipartBody.Part.createFormData("password", password);
        MultipartBody.Part pContact = MultipartBody.Part.createFormData("contact", contact);
        MultipartBody.Part pType = MultipartBody.Part.createFormData("type", type);
        MultipartBody.Part pAddress = MultipartBody.Part.createFormData("address", address);
        MultipartBody.Part pPincode = MultipartBody.Part.createFormData("pincode", pincode);

        RequestBody requestFile = RequestBody.create(MediaType.parse("application/pdf"), file);
        MultipartBody.Part pDocument = MultipartBody.Part.createFormData("document", file.getName(), requestFile);

        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        Call<ResponseBody> call = api.registerOrganization(
                pName, pEmail, pPassword, pContact, pType, pAddress, pPincode, pDocument
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register Organization");
                
                try (ResponseBody responseBody = response.isSuccessful() ? response.body() : response.errorBody()) {
                    String msg = responseBody != null ? responseBody.string() : "Error " + response.code();
                    Toast.makeText(RegisterOrganizationActivity.this, msg, Toast.LENGTH_LONG).show();
                    if (response.isSuccessful()) {
                        finish();
                    }
                } catch (IOException e) {
                    Log.e("ORG_REGISTER_ERROR", "Error reading response", e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Register Organization");
                Log.e("ORG_REGISTER_FAIL", "Network failure", t);
                // Show the ACTUAL error message to help identify connection issues
                Toast.makeText(RegisterOrganizationActivity.this, "Connection Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
