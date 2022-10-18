package com.example.rtttest1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

public class HelpActivity extends AppCompatActivity {

    private static final String TAG = "HelpActivity";
    Toolbar toolbarHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        toolbarHelp = findViewById(R.id.toolbarHelp);
        setSupportActionBar(toolbarHelp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
}