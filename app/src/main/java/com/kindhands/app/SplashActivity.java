package com.kindhands.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.kindhands.app.utils.SharedPrefManager;

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
        boolean isAppAlreadyInstalled = prefs.getBoolean("is_app_already_installed", false);

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
            if (isAppAlreadyInstalled) {
                // For users who have already used the app once
                if (SharedPrefManager.getInstance(SplashActivity.this).isLoggedIn()) {
                    // Logged in user: Welcome Page then Community Info
                    startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                } else {
                    // Not logged in but used before: Login (Skipping Role/Lang selection)
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
            } else {
                // Completely new user: Language/Role Selection Page
                startActivity(new Intent(SplashActivity.this, LanguageSelectionActivity.class));
            }
            finish();
        }, 1000);
    }
}
