package com.kindhands.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Always enforce light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isLanguageSelected = prefs.getBoolean("is_language_selected", false);

        // Apply the saved language if it exists
        if (isLanguageSelected) {
            String languageCode = prefs.getString("selected_language", "en"); // default to English
            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }

        // Decide where to go next
        new Handler().postDelayed(() -> {
            if (isLanguageSelected) {
                // Go directly to Login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            } else {
                // Show language selection screen
                startActivity(new Intent(SplashActivity.this, LanguageSelectionActivity.class));
            }
            finish(); // Finish this activity
        }, 1000); // 1-second delay
    }
}

