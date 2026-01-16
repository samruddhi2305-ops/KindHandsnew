package com.kindhands.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        Button btnDonor = findViewById(R.id.btnRoleDonor);
        Button btnOrphanage = findViewById(R.id.btnRoleOrphanage);
        Button btnOldAge = findViewById(R.id.btnRoleOldAge);

        btnDonor.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnOrphanage.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, RegisterOrganizationActivity.class);
            startActivity(intent);
        });

        btnOldAge.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, RegisterOrganizationActivity.class);
            startActivity(intent);
        });
    }
}
