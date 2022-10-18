package com.example.rtttest1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

public class VersionActivity extends AppCompatActivity {

    private static final String TAG = "VersionActivity";
    Toolbar toolbarVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version);

        toolbarVersion = findViewById(R.id.toolbarVersion);
        setSupportActionBar(toolbarVersion);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
}