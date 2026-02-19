package com.kindhands.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class LanguageSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        // Language Buttons (Small at the top)
        Button englishButton = findViewById(R.id.button_english);
        Button marathiButton = findViewById(R.id.button_marathi);

        // Registration Buttons
        Button btnDonor = findViewById(R.id.btnRoleDonor);
        Button btnOrphanage = findViewById(R.id.btnRoleOrphanage);
        Button btnOldAge = findViewById(R.id.btnRoleOldAge);

        // Language Listeners
        englishButton.setOnClickListener(v -> setLocale("en"));
        marathiButton.setOnClickListener(v -> setLocale("mr"));

        // Registration Navigation
        btnDonor.setOnClickListener(v -> {
            Intent intent = new Intent(LanguageSelectionActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnOrphanage.setOnClickListener(v -> {
            Intent intent = new Intent(LanguageSelectionActivity.this, RegisterOrganizationActivity.class);
            intent.putExtra("ORG_TYPE", "ORPHANAGE");
            startActivity(intent);
        });

        btnOldAge.setOnClickListener(v -> {
            Intent intent = new Intent(LanguageSelectionActivity.this, RegisterOrganizationActivity.class);
            intent.putExtra("ORG_TYPE", "OLD_AGE_HOME");
            startActivity(intent);
        });
    }

    private void setLocale(String languageCode) {
        // Save the selected language
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("selected_language", languageCode);
        editor.putBoolean("is_language_selected", true);
        editor.apply();

        // Update the app's configuration
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Refresh the current activity to show changes
        recreate();
    }
}
