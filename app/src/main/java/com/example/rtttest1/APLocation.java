package com.example.rtttest1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Objects;

public class APLocation extends AppCompatActivity {

    private static final String TAG = "APLocationActivity";

    Toolbar toolbarAPLocation;
    RadioGroup radioGroup;

    EditText AP1X_EditText, AP1Y_EditText, AP2X_EditText, AP2Y_EditText,
            AP3X_EditText, AP3Y_EditText, AP4X_EditText, AP4Y_EditText,
            AP5X_EditText, AP5Y_EditText, AP6X_EditText, AP6Y_EditText,
            AP7X_EditText, AP7Y_EditText, AP8X_EditText, AP8Y_EditText;

    String CoordinateAP1_X, CoordinateAP1_Y, CoordinateAP2_X, CoordinateAP2_Y,
            CoordinateAP3_X, CoordinateAP3_Y, CoordinateAP4_X, CoordinateAP4_Y,
            CoordinateAP5_X, CoordinateAP5_Y, CoordinateAP6_X, CoordinateAP6_Y,
            CoordinateAP7_X, CoordinateAP7_Y, CoordinateAP8_X, CoordinateAP8_Y;

    Boolean isStoredLink, isStoredMechanical;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aplocation);

        toolbarAPLocation = findViewById(R.id.toolbarAPLocation);
        setSupportActionBar(toolbarAPLocation);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        AP1X_EditText = findViewById(R.id.editTextAP1X);
        AP1Y_EditText = findViewById(R.id.editTextAP1Y);
        AP2X_EditText = findViewById(R.id.editTextAP2X);
        AP2Y_EditText = findViewById(R.id.editTextAP2Y);
        AP3X_EditText = findViewById(R.id.editTextAP3X);
        AP3Y_EditText = findViewById(R.id.editTextAP3Y);
        AP4X_EditText = findViewById(R.id.editTextAP4X);
        AP4Y_EditText = findViewById(R.id.editTextAP4Y);
        AP5X_EditText = findViewById(R.id.editTextAP5X);
        AP5Y_EditText = findViewById(R.id.editTextAP5Y);
        AP6X_EditText = findViewById(R.id.editTextAP6X);
        AP6Y_EditText = findViewById(R.id.editTextAP6Y);
        AP7X_EditText = findViewById(R.id.editTextAP7X);
        AP7Y_EditText = findViewById(R.id.editTextAP7Y);
        AP8X_EditText = findViewById(R.id.editTextAP8X);
        AP8Y_EditText = findViewById(R.id.editTextAP8Y);

        radioGroup = findViewById(R.id.radioGroupLocation);

        sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);

        isStoredLink = sharedPreferences.getBoolean("AP_Storage_Status_Link", false);
        isStoredMechanical = sharedPreferences.getBoolean("AP_Storage_Status_Mechanical", false);

        Log.d(TAG, "J13的AP位置数据存储状态：" + isStoredLink);
        Log.d(TAG, "J07的AP位置数据存储状态：" + isStoredMechanical);

        DisplayForLink();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                //DO NOT USE SWITCH CASE
                if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonLink) {
                    DisplayForLink();
                } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonMechanical) {
                        DisplayForMechanical();
                }
            }
        });
    }

    public void onClickSetDefault(View view) {
        Log.d(TAG,"onClickSetDefault");

        // DO NOT USE SWITCH CASE
        if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonLink) {
            DefaultLink();
        } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonMechanical) {
            DefaultMechanical();
        }
    }

    public void onClickApply(View view) {
        Log.d(TAG,"onClickApply");

        //DO NOT USE SWITCH CASE
        if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonLink) {
            CoordinateAP1_X = AP1X_EditText.getText().toString();
            CoordinateAP1_Y = AP1Y_EditText.getText().toString();
            CoordinateAP2_X = AP2X_EditText.getText().toString();
            CoordinateAP2_Y = AP2Y_EditText.getText().toString();
            CoordinateAP3_X = AP3X_EditText.getText().toString();
            CoordinateAP3_Y = AP3Y_EditText.getText().toString();
            CoordinateAP4_X = AP4X_EditText.getText().toString();
            CoordinateAP4_Y = AP4Y_EditText.getText().toString();
            CoordinateAP5_X = AP5X_EditText.getText().toString();
            CoordinateAP5_Y = AP5Y_EditText.getText().toString();
            CoordinateAP6_X = AP6X_EditText.getText().toString();
            CoordinateAP6_Y = AP6Y_EditText.getText().toString();
            CoordinateAP7_X = AP7X_EditText.getText().toString();
            CoordinateAP7_Y = AP7Y_EditText.getText().toString();
            CoordinateAP8_X = AP8X_EditText.getText().toString();
            CoordinateAP8_Y = AP8Y_EditText.getText().toString();

            SharedPreferences.Editor editorLink = sharedPreferences.edit();

            editorLink.putBoolean("AP_Storage_Status_Link", true);
            editorLink.putString("AP1_X_Link", CoordinateAP1_X);
            editorLink.putString("AP1_Y_Link", CoordinateAP1_Y);
            editorLink.putString("AP2_X_Link", CoordinateAP2_X);
            editorLink.putString("AP2_Y_Link", CoordinateAP2_Y);
            editorLink.putString("AP3_X_Link", CoordinateAP3_X);
            editorLink.putString("AP3_Y_Link", CoordinateAP3_Y);
            editorLink.putString("AP4_X_Link", CoordinateAP4_X);
            editorLink.putString("AP4_Y_Link", CoordinateAP4_Y);
            editorLink.putString("AP5_X_Link", CoordinateAP5_X);
            editorLink.putString("AP5_Y_Link", CoordinateAP5_Y);
            editorLink.putString("AP6_X_Link", CoordinateAP6_X);
            editorLink.putString("AP6_Y_Link", CoordinateAP6_Y);
            editorLink.putString("AP7_X_Link", CoordinateAP7_X);
            editorLink.putString("AP7_Y_Link", CoordinateAP7_Y);
            editorLink.putString("AP8_X_Link", CoordinateAP8_X);
            editorLink.putString("AP8_Y_Link", CoordinateAP8_Y);
            editorLink.apply();

            Toast.makeText(this, "New AP locations are applied to link building (J13)",
                    Toast.LENGTH_LONG).show();
        } else if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonMechanical) {
            CoordinateAP1_X = AP1X_EditText.getText().toString();
            CoordinateAP1_Y = AP1Y_EditText.getText().toString();
            CoordinateAP2_X = AP2X_EditText.getText().toString();
            CoordinateAP2_Y = AP2Y_EditText.getText().toString();
            CoordinateAP3_X = AP3X_EditText.getText().toString();
            CoordinateAP3_Y = AP3Y_EditText.getText().toString();
            CoordinateAP4_X = AP4X_EditText.getText().toString();
            CoordinateAP4_Y = AP4Y_EditText.getText().toString();
            CoordinateAP5_X = AP5X_EditText.getText().toString();
            CoordinateAP5_Y = AP5Y_EditText.getText().toString();
            CoordinateAP6_X = AP6X_EditText.getText().toString();
            CoordinateAP6_Y = AP6Y_EditText.getText().toString();
            CoordinateAP7_X = AP7X_EditText.getText().toString();
            CoordinateAP7_Y = AP7Y_EditText.getText().toString();
            CoordinateAP8_X = AP8X_EditText.getText().toString();
            CoordinateAP8_Y = AP8Y_EditText.getText().toString();

            SharedPreferences.Editor editorMechanical = sharedPreferences.edit();

            editorMechanical.putBoolean("AP_Storage_Status_Mechanical", true);
            editorMechanical.putString("AP1_X_Mechanical", CoordinateAP1_X);
            editorMechanical.putString("AP1_Y_Mechanical", CoordinateAP1_Y);
            editorMechanical.putString("AP2_X_Mechanical", CoordinateAP2_X);
            editorMechanical.putString("AP2_Y_Mechanical", CoordinateAP2_Y);
            editorMechanical.putString("AP3_X_Mechanical", CoordinateAP3_X);
            editorMechanical.putString("AP3_Y_Mechanical", CoordinateAP3_Y);
            editorMechanical.putString("AP4_X_Mechanical", CoordinateAP4_X);
            editorMechanical.putString("AP4_Y_Mechanical", CoordinateAP4_Y);
            editorMechanical.putString("AP5_X_Mechanical", CoordinateAP5_X);
            editorMechanical.putString("AP5_Y_Mechanical", CoordinateAP5_Y);
            editorMechanical.putString("AP6_X_Mechanical", CoordinateAP6_X);
            editorMechanical.putString("AP6_Y_Mechanical", CoordinateAP6_Y);
            editorMechanical.putString("AP7_X_Mechanical", CoordinateAP7_X);
            editorMechanical.putString("AP7_Y_Mechanical", CoordinateAP7_Y);
            editorMechanical.putString("AP8_X_Mechanical", CoordinateAP8_X);
            editorMechanical.putString("AP8_Y_Mechanical", CoordinateAP8_Y);
            editorMechanical.apply();

            Toast.makeText(this, "New AP locations are applied to mechanical building (J07)",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void DisplayForLink() {
        if (!isStoredLink) {
            DefaultLink();
        } else {
            CoordinateAP1_X = sharedPreferences.getString("AP1_X_Link", "");
            CoordinateAP1_Y = sharedPreferences.getString("AP1_Y_Link", "");
            CoordinateAP2_X = sharedPreferences.getString("AP2_X_Link", "");
            CoordinateAP2_Y = sharedPreferences.getString("AP2_Y_Link", "");
            CoordinateAP3_X = sharedPreferences.getString("AP3_X_Link", "");
            CoordinateAP3_Y = sharedPreferences.getString("AP3_Y_Link", "");
            CoordinateAP4_X = sharedPreferences.getString("AP4_X_Link", "");
            CoordinateAP4_Y = sharedPreferences.getString("AP4_Y_Link", "");
            CoordinateAP5_X = sharedPreferences.getString("AP5_X_Link", "");
            CoordinateAP5_Y = sharedPreferences.getString("AP5_Y_Link", "");
            CoordinateAP6_X = sharedPreferences.getString("AP6_X_Link", "");
            CoordinateAP6_Y = sharedPreferences.getString("AP6_Y_Link", "");
            CoordinateAP7_X = sharedPreferences.getString("AP7_X_Link", "");
            CoordinateAP7_Y = sharedPreferences.getString("AP7_Y_Link", "");
            CoordinateAP8_X = sharedPreferences.getString("AP8_X_Link", "");
            CoordinateAP8_Y = sharedPreferences.getString("AP8_Y_Link", "");

            AP1X_EditText.setText(CoordinateAP1_X);
            AP1Y_EditText.setText(CoordinateAP1_Y);
            AP2X_EditText.setText(CoordinateAP2_X);
            AP2Y_EditText.setText(CoordinateAP2_Y);
            AP3X_EditText.setText(CoordinateAP3_X);
            AP3Y_EditText.setText(CoordinateAP3_Y);
            AP4X_EditText.setText(CoordinateAP4_X);
            AP4Y_EditText.setText(CoordinateAP4_Y);
            AP5X_EditText.setText(CoordinateAP5_X);
            AP5Y_EditText.setText(CoordinateAP5_Y);
            AP6X_EditText.setText(CoordinateAP6_X);
            AP6Y_EditText.setText(CoordinateAP6_Y);
            AP7X_EditText.setText(CoordinateAP7_X);
            AP7Y_EditText.setText(CoordinateAP7_Y);
            AP8X_EditText.setText(CoordinateAP8_X);
            AP8Y_EditText.setText(CoordinateAP8_Y);
        }
    }

    public void DefaultLink(){
        AP1X_EditText.setText("35.45");
        AP1Y_EditText.setText("14.07");
        AP2X_EditText.setText("49");
        AP2Y_EditText.setText("15.11");
        AP3X_EditText.setText("27.69");
        AP3Y_EditText.setText("14.72");
        AP4X_EditText.setText("15.68");
        AP4Y_EditText.setText("13.17");
        AP5X_EditText.setText("12.08");
        AP5Y_EditText.setText("5.6");
        AP6X_EditText.setText("29.1");
        AP6Y_EditText.setText("5.6");
        AP7X_EditText.setText("39.31");
        AP7Y_EditText.setText("5.6");
        AP8X_EditText.setText("50.43");
        AP8Y_EditText.setText("5.6");
    }

    public void DisplayForMechanical(){
        if (!isStoredMechanical){
            DefaultMechanical();
        } else {
            CoordinateAP1_X = sharedPreferences.getString("AP1_X_Mechanical", "");
            CoordinateAP1_Y = sharedPreferences.getString("AP1_Y_Mechanical", "");
            CoordinateAP2_X = sharedPreferences.getString("AP2_X_Mechanical", "");
            CoordinateAP2_Y = sharedPreferences.getString("AP2_Y_Mechanical", "");
            CoordinateAP3_X = sharedPreferences.getString("AP3_X_Mechanical", "");
            CoordinateAP3_Y = sharedPreferences.getString("AP3_Y_Mechanical", "");
            CoordinateAP4_X = sharedPreferences.getString("AP4_X_Mechanical", "");
            CoordinateAP4_Y = sharedPreferences.getString("AP4_Y_Mechanical", "");
            CoordinateAP5_X = sharedPreferences.getString("AP5_X_Mechanical", "");
            CoordinateAP5_Y = sharedPreferences.getString("AP5_Y_Mechanical", "");
            CoordinateAP6_X = sharedPreferences.getString("AP6_X_Mechanical", "");
            CoordinateAP6_Y = sharedPreferences.getString("AP6_Y_Mechanical", "");
            CoordinateAP7_X = sharedPreferences.getString("AP7_X_Mechanical", "");
            CoordinateAP7_Y = sharedPreferences.getString("AP7_Y_Mechanical", "");
            CoordinateAP8_X = sharedPreferences.getString("AP8_X_Mechanical", "");
            CoordinateAP8_Y = sharedPreferences.getString("AP8_Y_Mechanical", "");

            AP1X_EditText.setText(CoordinateAP1_X);
            AP1Y_EditText.setText(CoordinateAP1_Y);
            AP2X_EditText.setText(CoordinateAP2_X);
            AP2Y_EditText.setText(CoordinateAP2_Y);
            AP3X_EditText.setText(CoordinateAP3_X);
            AP3Y_EditText.setText(CoordinateAP3_Y);
            AP4X_EditText.setText(CoordinateAP4_X);
            AP4Y_EditText.setText(CoordinateAP4_Y);
            AP5X_EditText.setText(CoordinateAP5_X);
            AP5Y_EditText.setText(CoordinateAP5_Y);
            AP6X_EditText.setText(CoordinateAP6_X);
            AP6Y_EditText.setText(CoordinateAP6_Y);
            AP7X_EditText.setText(CoordinateAP7_X);
            AP7Y_EditText.setText(CoordinateAP7_Y);
            AP8X_EditText.setText(CoordinateAP8_X);
            AP8Y_EditText.setText(CoordinateAP8_Y);
        }
    }

    public void DefaultMechanical(){
        AP1X_EditText.setText("10.3");
        AP1Y_EditText.setText("21.67");
        AP2X_EditText.setText("0.4");
        AP2Y_EditText.setText("20");
        AP3X_EditText.setText("17.25");
        AP3Y_EditText.setText("14.53");
        AP4X_EditText.setText("17.4");
        AP4Y_EditText.setText("20.6");
        AP5X_EditText.setText("0.4");
        AP5Y_EditText.setText("9.5");
        AP6X_EditText.setText("0.4");
        AP6Y_EditText.setText("16.24");
        AP7X_EditText.setText("25.34");
        AP7Y_EditText.setText("18.5");
        AP8X_EditText.setText("10.28");
        AP8Y_EditText.setText("12.4");
    }
}