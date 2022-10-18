package com.example.rtttest1;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;

import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.content.pm.PackageManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private boolean LocationPermission = false;

    private ArrayList<ScanResult> AP_list_support_RTT;

    private WifiManager myWifiManager;
    private WifiScanReceiver myWifiReceiver;
    private MainActivityAdapter mainActivityAdapter;

    private int checkedLocation;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Window w = getWindow();
//        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        RecyclerView myRecyclerView = findViewById(R.id.RecyclerViewAPs);
        myRecyclerView.setHasFixedSize(true);

        drawerLayout = findViewById(R.id.activity_main);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        navigationView.setItemIconTintList(null);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawerLayout,toolbar,R.string.NavigationDrawerOpen,R.string.NavigationDrawerClose);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        LayoutManager layoutManager = new LinearLayoutManager(this);
        myRecyclerView.setLayoutManager((layoutManager));

        AP_list_support_RTT = new ArrayList<>();

        mainActivityAdapter = new MainActivityAdapter(AP_list_support_RTT);
        myRecyclerView.setAdapter(mainActivityAdapter);

        myWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        myWifiReceiver = new WifiScanReceiver();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.nav_server){
            DialogServer();
        }
        else if (menuItem.getItemId() == R.id.nav_csv){
            DialogCSV();
        }
        else if (menuItem.getItemId() == R.id.nav_ap){
            Toast.makeText(getApplicationContext(),"AP", Toast.LENGTH_SHORT).show();
        }
        else if (menuItem.getItemId() == R.id.nav_rtt){
            //Check WiFi-RTT availability of the device
            Log.d(TAG,"Checking RTT Availability...");

            boolean RTT_availability = getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_WIFI_RTT);

            if (RTT_availability) {
                Toast.makeText(this,"WiFi RTT is supported on this device :)",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,"WiFi RTT is not supported on this device :(",
                        Toast.LENGTH_LONG).show();
            }
        }
        else if (menuItem.getItemId() == R.id.nav_version){
            Intent intentVersion = new Intent(getApplicationContext(), VersionActivity.class);
            startActivity(intentVersion);
        }
        else if (menuItem.getItemId() == R.id.nav_help){
            Intent intentHelp = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intentHelp);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //TODO make this class a common service
    private class WifiScanReceiver extends BroadcastReceiver {
        private List<ScanResult> findRTTAPs(@NonNull List<ScanResult> WiFiScanResults) {
            List<ScanResult> RTT_APs = new ArrayList<>();
            for (ScanResult scanresult:WiFiScanResults) {
                if (scanresult.is80211mcResponder()) {
                    RTT_APs.add(scanresult);
                }
            }
            //MaxPeer is 10
            return RTT_APs;
        }

        //Add to avoid permission check for each scan
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive() MainActivity");

            List<ScanResult> scanResults = myWifiManager.getScanResults();
            AP_list_support_RTT = (ArrayList<ScanResult>) findRTTAPs(scanResults);
            Log.d(TAG, "All WiFi points"+"("+scanResults.size()+")"+": "+scanResults);
            Log.d(TAG, "RTT APs"+"("+AP_list_support_RTT.size()+")"+": "+AP_list_support_RTT);

            if (!AP_list_support_RTT.isEmpty()){
                mainActivityAdapter.swapData(AP_list_support_RTT);

            } else{
                Log.d(TAG,"No RTT APs available");
            }
        }
    }

    public void onClickScanAPs(View view) {
        if (LocationPermission) {
            Log.d(TAG, "Scanning...");
            myWifiManager.startScan();

            Snackbar.make(view, "Scanning...", Snackbar.LENGTH_SHORT).show();

        } else {
            // request permission
            Intent IntentRequestPermission = new Intent(this,
                    LocationPermissionRequest.class);
            startActivity(IntentRequestPermission);
        }
    }

    //Start ranging in a new screen
    public void onClickRangingAPs(View view) {
        Log.d(TAG,"onClickRangingAPs()");

        Intent IntentRanging = new Intent(getApplicationContext(), RangingActivity.class);

        //Pass AP_list_support_RTT to RangingActivity
        IntentRanging.putParcelableArrayListExtra("SCAN_RESULT", AP_list_support_RTT);
        startActivity(IntentRanging);
    }

    public void onClickStartPositioning(View view){

        String[] location = {"Link Building (J13)","Mechanical Building (J07)"};

        AlertDialog.Builder dialog_location = new AlertDialog.Builder(this);
        dialog_location.setTitle("Choose a location");
        dialog_location.setSingleChoiceItems(location, checkedLocation, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkedLocation = which;
            }
        });
        dialog_location.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (checkedLocation){
                    case 0:
                        Intent intentPositioning_0 = new Intent(getApplicationContext(),
                                LocalisationActivity.class);
                        intentPositioning_0.putParcelableArrayListExtra("SCAN_RESULT",AP_list_support_RTT);
                        startActivity(intentPositioning_0);
                        break;
                    case 1:
                        Intent intentPositioning_1 = new Intent(getApplicationContext(),
                                LocalisationActivity_mechanical.class);
                        intentPositioning_1.putParcelableArrayListExtra("SCAN_RESULT",AP_list_support_RTT);
                        startActivity(intentPositioning_1);
                        break;
                }
            }
        });
        dialog_location.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = dialog_location.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() MainActivity");
        super.onStop();
        unregisterReceiver(myWifiReceiver);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() MainActivity");
        super.onResume();

        // each time resume back in onResume state, check location permission
        LocationPermission = ActivityCompat.checkSelfPermission(
                this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        Log.d(TAG, "Location permission:" + LocationPermission);

        //register a Broadcast receiver to run in the main activity thread
        registerReceiver(
                myWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void DialogServer(){
        final EditText ServerAddress = new EditText(this);
        ServerAddress.setMaxLines(1);
        ServerAddress.setInputType(InputType.TYPE_CLASS_NUMBER);
        ServerAddress.setHint("IP address of the server");
        ServerAddress.setText(getString(R.string.ServerAddressPreset));

        AlertDialog.Builder dialogServer = new AlertDialog.Builder(this);
        dialogServer.setTitle("Enter the server address");
        dialogServer.setView(ServerAddress);
        dialogServer.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (ServerAddress.getText().toString().isEmpty()) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(),"Empty address",Toast.LENGTH_SHORT).show();
                }
                else{
                    String serverAddress = ServerAddress.getText().toString();

                    //Write the server address into internal storage
                    SharedPreferences sharedPreServer = getSharedPreferences("config",
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editorServer = sharedPreServer.edit();
                    editorServer.putString("ServerAddress",serverAddress);
                    editorServer.apply();

                    Toast.makeText(getApplicationContext(),"Server address: "
                            + serverAddress + "is applied",Toast.LENGTH_LONG).show();
                }
            }
        });
        dialogServer.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = dialogServer.create();
        dialog.show();
    }

    public void DialogCSV(){
        final EditText CSVFile = new EditText(this);
        CSVFile.setHint("The csv file name you want");
        CSVFile.setMaxLines(1);

        AlertDialog.Builder dialogCSV = new AlertDialog.Builder(this);
        dialogCSV.setTitle("Enter the csv file name");
        dialogCSV.setView(CSVFile);
        dialogCSV.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (CSVFile.getText().toString().isEmpty()) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(),"Empty name",Toast.LENGTH_SHORT).show();
                }
                else{
                    String csvFile = CSVFile.getText().toString();

                    //Write the csv file name into internal storage
                    SharedPreferences sharedPreCSV = getSharedPreferences("config",
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editorServer = sharedPreCSV.edit();
                    editorServer.putString("csvFile",csvFile);
                    editorServer.apply();

                    Toast.makeText(getApplicationContext(),"The csv file name: "
                            + csvFile + "is applied",Toast.LENGTH_LONG).show();
                }
            }
        });
        dialogCSV.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = dialogCSV.create();
        dialog.show();
    }
}