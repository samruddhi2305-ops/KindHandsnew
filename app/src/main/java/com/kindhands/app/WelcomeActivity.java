package com.kindhands.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.kindhands.app.utils.SharedPrefManager;

import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply language settings immediately before layout inflation
        applyLanguageSettings();
        
        setContentView(R.layout.activity_welcome);

        // Always enforce light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Stay for 3 seconds then decide where to go
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            boolean isAppAlreadyInstalled = prefs.getBoolean("is_app_already_installed", false);

            Intent intent;
            if (!isAppAlreadyInstalled) {
                // Completely new user: Show Language/Role Selection
                intent = new Intent(WelcomeActivity.this, LanguageSelectionActivity.class);
            } else if (SharedPrefManager.getInstance(WelcomeActivity.this).isLoggedIn()) {
                // Old user, already logged in: Go to Community Info
                intent = new Intent(WelcomeActivity.this, CommunityInfoActivity.class);
            } else {
                // Old user, not logged in: Go to Login
                intent = new Intent(WelcomeActivity.this, LoginActivity.class);
            }
            
            startActivity(intent);
            finish();
        }, 3000);
    }

    private void applyLanguageSettings() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isLanguageSelected = prefs.getBoolean("is_language_selected", false);

        if (isLanguageSelected) {
            String languageCode = prefs.getString("selected_language", "en");
            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);
            Resources resources = getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(locale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }
}
