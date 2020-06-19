package com.example.kassiermanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import java.util.prefs.PreferenceChangeListener;

public class PreferenceActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new PreferenceFragment())
                .commit();

        actionbarDesign();
    }

    private void actionbarDesign(){
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        preferenceChangeListener = ((sharedPrefs, key) -> preferenceChanged(sharedPrefs, key));
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        String backgroundColour = prefs.getString("colour", "#6200EE");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(backgroundColour)));
    }

    private void preferenceChanged(SharedPreferences sharedPreferences, String key) {
        String backgroundColour = prefs.getString("colour", "#6200EE");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(backgroundColour)));
    }
}
