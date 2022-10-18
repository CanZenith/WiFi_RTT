package com.example.rtttest1;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocalisationActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "LocalisationActivity";

    //TODO public WifiManager/WifiRTTManager/RTTRangingResultCallback for all activities?
    //TODO fix layout in all orientations
    //TODO fix locationX/Y textview

    /**
     * For RTT service
     */
    private WifiRttManager myWifiRTTManager;
    private WifiManager myWifiManager;
    private RTTRangingResultCallback myRTTRangingResultCallback;

    private List<ScanResult> RTT_APs = new ArrayList<>();
    private final List<RangingResult> Ranging_Results = new ArrayList<>();
    private List<RangingResult> Synchronised_RTT = new ArrayList<>();
    private final List<String> APs_MacAddress = new ArrayList<>();

    private long RTT_timestamp;

    final Handler RangingRequestDelayHandler = new Handler();

    private int buttonState = 0;
    private Button visualiseButton;
    private EditText url_text;

    private float meter2pixel, dip2pixel;
    private int screenHeight, screenWidth, bitmapHeight, bitmapWidth, Bias_Y;

    /**
     * For IMU service
     */
    private SensorManager sensorManager;
    private final HashMap<String, Sensor> sensors = new HashMap<>();
    private long IMU_timestamp;
    private long Closest_IMU_timestamp;

    private final float[] rotationMatrix = new float[9];
    private final float[] inclinationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    private float[] Synchronised_orientationAngles = new float[3];

    private final float[] LastAccReading = new float[3];
    private final float[] LastMagReading = new float[3];
    private final float[] LastGyroReading = new float[3];
    private float[] Synchronised_LastAccReading = new float[3];
    private float[] Synchronised_LastMagReading = new float[3];
    private float[] Synchronised_LastGyroReading = new float[3];

    /**
     * For Localisation service
     */
    private Paint paint;
    private Path path;
    private Bitmap temp_bitmap;
    private Canvas temp_canvas;

    private ImageView floor_plan, location_pin, AP1_ImageView, AP2_ImageView, AP3_ImageView,
            AP4_ImageView, AP5_ImageView, AP6_ImageView, AP7_ImageView, AP8_ImageView;
    private final ImageView[] Circles = new ImageView[3];

    private TextView LocationX, LocationY;

    int start_localisation = 0;
    int color_blue = Color.parseColor("#0000FF");
    int color_green = Color.parseColor("#00FF00");
    int APList_start, APList_end, DisList_start, DisList_end;

    private String Location_from_server;
    private String APList, DisList;
    private String[] Calculated_coordinates = new String[8];
    private String[] Previous_location_for_line_drawing = new String[8];

    private final AccessPoint AP1 = new AccessPoint("b0:e4:d5:39:26:89", 35.45, 14.07);
    private final AccessPoint AP2 = new AccessPoint("cc:f4:11:8b:29:4d", 49, 15.11);
    private final AccessPoint AP3 = new AccessPoint("b0:e4:d5:01:26:f5", 27.69, 14.72);
    private final AccessPoint AP4 = new AccessPoint("b0:e4:d5:91:ba:5d", 15.68, 13.17);
    private final AccessPoint AP5 = new AccessPoint("b0:e4:d5:96:3b:95", 12.08, 5.6);
    private final AccessPoint AP6 = new AccessPoint("f8:1a:2b:06:3c:0b", 29.1, 5.6);
    private final AccessPoint AP7 = new AccessPoint("14:22:3b:2a:86:f5", 39.31, 5.6);
    private final AccessPoint AP8 = new AccessPoint("14:22:3b:16:5a:bd", 50.43, 5.6);

    //flag for leaving the activity
    private Boolean Running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() LocalizationActivity");

        //receive RTT_APs from main activity
        Intent intent = getIntent();
        RTT_APs = intent.getParcelableArrayListExtra("SCAN_RESULT");

        //TODO edit Toast
        if (RTT_APs == null || RTT_APs.isEmpty()) {
            Log.d(TAG, "RTT_APs null");
            Toast.makeText(getApplicationContext(),
                    "Please scan for available APs first",
                    Toast.LENGTH_SHORT).show();
            finish();
        } else {
            setContentView(R.layout.activity_localisation);

            //Change to landscape and full screen immersive mode
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            visualiseButton = findViewById(R.id.btnVisualise);
            url_text = findViewById(R.id.editText_server);

            //Server address preset
            SharedPreferences sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);
            String serverAddress = sharedPreferences.getString("ServerAddress", "NULL");
            if (serverAddress.equals("NULL")) {
                url_text.setText(getString(R.string.ServerAddressPreset));
            } else {
                url_text.setText(serverAddress);
            }

            //RTT Initiation
            myWifiRTTManager = (WifiRttManager) getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
            myWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            myRTTRangingResultCallback = new RTTRangingResultCallback();

            WifiScanReceiver myWifiScanReceiver = new WifiScanReceiver();
            registerReceiver(myWifiScanReceiver,
                    new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            for (ScanResult AP : RTT_APs) {
                APs_MacAddress.add(AP.BSSID);
            }

            //IMU Initiation
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensors.put("Accelerometer", sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            sensors.put("Gyroscope", sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
            sensors.put("Magnetic", sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));

            //Localization Initiation
            floor_plan = findViewById(R.id.imageViewFloorplan);
            location_pin = findViewById(R.id.imageViewLocationPin);
            AP1_ImageView = findViewById(R.id.imageViewAP1);
            AP2_ImageView = findViewById(R.id.imageViewAP2);
            AP3_ImageView = findViewById(R.id.imageViewAP3);
            AP4_ImageView = findViewById(R.id.imageViewAP4);
            AP5_ImageView = findViewById(R.id.imageViewAP5);
            AP6_ImageView = findViewById(R.id.imageViewAP6);
            AP7_ImageView = findViewById(R.id.imageViewAP7);
            AP8_ImageView = findViewById(R.id.imageViewAP8);
            Circles[0] = findViewById(R.id.imageViewCircle1);
            Circles[1] = findViewById(R.id.imageViewCircle2);
            Circles[2] = findViewById(R.id.imageViewCircle3);

            LocationX = findViewById(R.id.textViewLocationX);
            LocationY = findViewById(R.id.textViewLocationY);

            WindowManager windowManager = getWindow().getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            screenWidth = point.x;
            screenHeight = point.y;
            Log.d("window", "屏幕宽度：" + screenWidth);
            Log.d("window", "屏幕高度：" + screenHeight);

            dip2pixel = this.getResources().getDisplayMetrics().density;
            meter2pixel = screenWidth / 75.01f;
            Bias_Y = Math.round((screenHeight - screenWidth / 3.6f) / 2);

            Bitmap bitmap_floor_plan = BitmapFactory.decodeResource(getResources(),
                    R.drawable.floor_plan_landscape);
            temp_bitmap = Bitmap.createBitmap(bitmap_floor_plan.getWidth(),
                    bitmap_floor_plan.getHeight(), Bitmap.Config.RGB_565);

            bitmapWidth = bitmap_floor_plan.getWidth();
            bitmapHeight = bitmap_floor_plan.getHeight();
            Log.d("window", "Bitmap宽度：" + bitmapWidth);
            Log.d("window", "Bitmap高度：" + bitmapHeight);

            temp_canvas = new Canvas(temp_bitmap);
            temp_canvas.drawBitmap(bitmap_floor_plan, 0, 0, null);

            paint = new Paint();
            path = new Path();

            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);
            paint.setPathEffect(new DashPathEffect(new float[]{20, 10, 10, 10}, 1));

            //Start Localisation
            setup_pin_location();
            registerSensors();
            startRangingRequest();
            //startLoggingData();
            ScanInBackground();
            update_location_pin();
        }
    }

    /*
      The following method is used to determine the dimension of floor plan
      in aid of constructing an coordinate plane.
     */
/*
      public void onWindowFocusChanged(boolean hasFocus) {
          super.onWindowFocusChanged(hasFocus);
          if (hasFocus) {
              //left top coordinate
              floor_plan.getLocationOnScreen(floor_plan_location);
              location_pin.getLocationOnScreen(pin_location);
              AP6_ImageView.getLocationOnScreen(AP_location);

              //floor_plan.getLayoutParams();
              Log.i(TAG, "Floorplan" + floor_plan_location[0] + ", " + floor_plan_location[1]);
              Log.i(TAG, "Pin " + pin_location[0] + ", " + pin_location[1]);
              Log.i(TAG, "AP6 " + AP_location[0] + ", " + AP_location[1]);
              Log.i(TAG, "Image Width: " + floor_plan.getWidth());
              Log.i(TAG, "Image Height: " + floor_plan.getHeight());
          }
      }

 */

    /** ============================================================================================
     * -----新坐标系统-----
     * 横屏模式下，坐标原点为手机右上角，垂直方向为x轴，水平方向为y轴
     * 屏幕可用像素：宽（长边） = window_width, 高（短边） = window_height
     * 像素：Floorplan的总宽 = window_width, 宽高比 = 2341/650 = 3.6, Floorplan的总高 = window_width/3.6
     * 现实：Floorplan的尺寸 = 75.01m * 20.79m, 现实转换为像素：meter2pixel = window_width/75.01
     * x轴的偏置 = 0, y轴的偏置 = (window_height - Floorplan的总宽)/2
     * Bitmap的宽高：宽（长边） = bitmapWidth, 高（短边） = bitmapHeight
     *
     * -----转换公式-----
     * Pin Location：
     * setX = x * meter2pixel, setY = window_height - (y * meter2pixel + y轴偏置)
     *
     * Path Effect:
     * setX = x/75.01 * bitmapWidth, setY = y/20.79 * bitmapHeight
     * =============================================================================================
     */

    private float coordinate_X_to_Pixel(double X) {
        return (float) (X * meter2pixel);
    }

    private float coordinate_Y_to_Pixel(double Y) {
        return (float) (screenHeight - (Y * meter2pixel + Bias_Y));
    }

    private float coordinate_X_to_bitmap(double X) {
        return (float) (X * bitmapWidth / 75.01);
    }

    private float coordinate_Y_to_bitmap(double Y) {
        return (float) (bitmapHeight - (Y * bitmapHeight / 20.79));
    }

    private void setup_pin_location() {
        AP1_ImageView.setX(coordinate_X_to_Pixel(AP1.getX()) - (10 * dip2pixel + 0.5f));
        AP1_ImageView.setY(coordinate_Y_to_Pixel(AP1.getY()) - (20 * dip2pixel + 0.5f));
        AP2_ImageView.setX(coordinate_X_to_Pixel(AP2.getX()) - (10 * dip2pixel + 0.5f));
        AP2_ImageView.setY(coordinate_Y_to_Pixel(AP2.getY()) - (20 * dip2pixel + 0.5f));
        AP3_ImageView.setX(coordinate_X_to_Pixel(AP3.getX()) - (10 * dip2pixel + 0.5f));
        AP3_ImageView.setY(coordinate_Y_to_Pixel(AP3.getY()) - (20 * dip2pixel + 0.5f));
        AP4_ImageView.setX(coordinate_X_to_Pixel(AP4.getX()) - (10 * dip2pixel + 0.5f));
        AP4_ImageView.setY(coordinate_Y_to_Pixel(AP4.getY()) - (20 * dip2pixel + 0.5f));
        AP5_ImageView.setX(coordinate_X_to_Pixel(AP5.getX()) - (10 * dip2pixel + 0.5f));
        AP5_ImageView.setY(coordinate_Y_to_Pixel(AP5.getY()) - (20 * dip2pixel + 0.5f));
        AP6_ImageView.setX(coordinate_X_to_Pixel(AP6.getX()) - (10 * dip2pixel + 0.5f));
        AP6_ImageView.setY(coordinate_Y_to_Pixel(AP6.getY()) - (20 * dip2pixel + 0.5f));
        AP7_ImageView.setX(coordinate_X_to_Pixel(AP7.getX()) - (10 * dip2pixel + 0.5f));
        AP7_ImageView.setY(coordinate_Y_to_Pixel(AP7.getY()) - (20 * dip2pixel + 0.5f));
        AP8_ImageView.setX(coordinate_X_to_Pixel(AP8.getX()) - (10 * dip2pixel + 0.5f));
        AP8_ImageView.setY(coordinate_Y_to_Pixel(AP8.getY()) - (20 * dip2pixel + 0.5f));
        // (x * dip2pixel + 0.5f) is the bias of ImageView itself

        Circles[0].setAlpha(0.0f);
        Circles[1].setAlpha(0.0f);
        Circles[2].setAlpha(0.0f);

        //my desk
        location_pin.setX(coordinate_X_to_Pixel(43.46) - (10 * dip2pixel + 0.5f));
        location_pin.setY(coordinate_Y_to_Pixel(14.66) - (18 * dip2pixel + 0.5f));
    }

    //TODO animated drawable?
    private void update_location_pin() {
        //TODO better coordinate system?
        Handler Update_Location_Handler = new Handler();
        Runnable Update_Location_Runnable = new Runnable() {
            @SuppressLint("ResourceType")
            @Override
            public void run() {
                if (Running) {
                    Update_Location_Handler.postDelayed(this, 1000);

                    if (Calculated_coordinates[0] != null && Calculated_coordinates[1] != null) {
                        //TODO try except for wrong format

                        if (start_localisation == 0) {
                            Previous_location_for_line_drawing = Calculated_coordinates;
                            LocationX.setText(String.format(Locale.getDefault(),
                                    "%.2f", Double.valueOf(Calculated_coordinates[0])));
                            LocationY.setText(String.format(Locale.getDefault(),
                                    "%.2f", Double.valueOf(Calculated_coordinates[1])));

                            location_pin.setX(coordinate_X_to_Pixel(Double.parseDouble(Calculated_coordinates[0]))
                                    - (10 * dip2pixel + 0.5f));
                            location_pin.setY(coordinate_Y_to_Pixel(Double.parseDouble(Calculated_coordinates[1]))
                                    - (18 * dip2pixel + 0.5f));

                            if (buttonState == 1 || buttonState == 2) {
                                visualiseTrilateration();
                            }

                            start_localisation++;

                        } else {
                            LocationX.setText(String.format(Locale.getDefault(),
                                    "%.2f", Double.valueOf(Calculated_coordinates[0])));
                            LocationY.setText(String.format(Locale.getDefault(),
                                    "%.2f", Double.valueOf(Calculated_coordinates[1])));

                            path.moveTo(coordinate_X_to_bitmap(Double.parseDouble(Previous_location_for_line_drawing[0])),
                                    coordinate_Y_to_bitmap(Double.parseDouble(Previous_location_for_line_drawing[1])));

                            location_pin.setX(coordinate_X_to_Pixel(Double.parseDouble(Calculated_coordinates[0]))
                                    - (10 * dip2pixel + 0.5f));
                            location_pin.setY(coordinate_Y_to_Pixel(Double.parseDouble(Calculated_coordinates[1]))
                                    - (18 * dip2pixel + 0.5f));

                            path.lineTo(coordinate_X_to_bitmap(Double.parseDouble(Calculated_coordinates[0])),
                                    coordinate_Y_to_bitmap(Double.parseDouble(Calculated_coordinates[1])));
                            temp_canvas.drawPath(path, paint);
                            floor_plan.setImageBitmap(temp_bitmap);

                            Previous_location_for_line_drawing = Calculated_coordinates;

                            if (buttonState == 1 || buttonState == 2) {
                                visualiseTrilateration();
                            }
                        }
                    }
                } else {
                    Update_Location_Handler.removeCallbacks(this);
                }
            }
        };
        Update_Location_Handler.postDelayed(Update_Location_Runnable, 1000);
    }

    private void visualiseTrilateration() {
        if (APList_end - APList_start > 1) {
            //reset all AP image color
            AP1_ImageView.setColorFilter(color_blue);
            AP2_ImageView.setColorFilter(color_blue);
            AP3_ImageView.setColorFilter(color_blue);
            AP4_ImageView.setColorFilter(color_blue);
            AP5_ImageView.setColorFilter(color_blue);
            AP6_ImageView.setColorFilter(color_blue);
            AP7_ImageView.setColorFilter(color_blue);
            AP8_ImageView.setColorFilter(color_blue);

            //reset all circles transparency
            Circles[0].setAlpha(0.0f);
            Circles[1].setAlpha(0.0f);
            Circles[2].setAlpha(0.0f);

            String[] AP_List = APList.split(", ");
            String[] Dis_List = DisList.split(", ");
            int[] AP_List_int = new int[AP_List.length];
            float[] Dis_List_float = new float[Dis_List.length];
            int[] Diameter = new int[Dis_List.length];

            Log.d("Location information", "Location X/Y: " + Calculated_coordinates[0] + ", "
                    + Calculated_coordinates[1] + ", AP: " + APList + ", Distance: " + DisList);

            //Transfer String[] to int[] and float[], convert distance from meter to pixel
            for (int j = 0; j < AP_List.length; j++) {
                AP_List_int[j] = Integer.parseInt(AP_List[j]);
                Dis_List_float[j] = Float.parseFloat(Dis_List[j]);
                Diameter[j] = Math.round(2 * meter2pixel * Dis_List_float[j]);
            }

            //Change AP image color and draw circle
            for (int i = 0; i < AP_List.length; i++) {
                switch (AP_List_int[i]) {
                    case 1:
                        AP1_ImageView.setColorFilter(color_green);
                        Circles[i].setLayoutParams(new FrameLayout.LayoutParams(Diameter[i], Diameter[i]));
                        Circles[i].setAlpha(0.2f);
                        Circles[i].setX(coordinate_X_to_Pixel(AP1.getX()) - 1f * Diameter[i] / 2);
                        Circles[i].setY(coordinate_Y_to_Pixel(AP1.getY()) - 1f * Diameter[i] / 2 - (10 * dip2pixel + 0.5f));
                        break;
                    case 2:
                        AP2_ImageView.setColorFilter(color_green);
                        Circles[i].setLayoutParams(new FrameLayout.LayoutParams(Diameter[i], Diameter[i]));
                        Circles[i].setAlpha(0.2f);
                        Circles[i].setX(coordinate_X_to_Pixel(AP2.getX()) - 1f * Diameter[i] / 2);
                        Circles[i].setY(coordinate_Y_to_Pixel(AP2.getY()) - 1f * Diameter[i] / 2 - (10 * dip2pixel + 0.5f));
                        break;
                    case 3:
                        AP3_ImageView.setColorFilter(color_green);
                        Circles[i].setLayoutParams(new FrameLayout.LayoutParams(Diameter[i], Diameter[i]));
                        Circles[i].setAlpha(0.2f);
                        Circles[i].setX(coordinate_X_to_Pixel(AP3.getX()) - 1f * Diameter[i] / 2);
                        Circles[i].setY(coordinate_Y_to_Pixel(AP3.getY()) - 1f * Diameter[i] / 2 - (10 * dip2pixel + 0.5f));
                        break;
                    case 4:
                        AP4_ImageView.setColorFilter(color_green);
                        Circles[i].setLayoutParams(new FrameLayout.LayoutParams(Diameter[i], Diameter[i]));
                        Circles[i].setAlpha(0.2f);
                        Circles[i].setX(coordinate_X_to_Pixel(AP4.getX()) - 1f * Diameter[i] / 2);
                        Circles[i].setY(coordinate_Y_to_Pixel(AP4.getY()) - 1f * Diameter[i] / 2 - (10 * dip2pixel + 0.5f));
                        break;
                    case 5:
                        AP5_ImageView.setColorFilter(color_green);
                        Circles[i].setLayoutParams(new FrameLayout.LayoutParams(Diameter[i], Diameter[i]));
                        Circles[i].setAlpha(0.2f);
                        Circles[i].setX(coordinate_X_to_Pixel(AP5.getX()) - 1f * Diameter[i] / 2);
                        Circles[i].setY(coordinate_Y_to_Pixel(AP5.getY()) - 1f * Diameter[i] / 2 - (10 * dip2pixel + 0.5f));
                        break;
                    case 6:
                        AP6_ImageView.setColorFilter(color_green);
                        Circles[i].setLayoutParams(new FrameLayout.LayoutParams(Diameter[i], Diameter[i]));
                        Circles[i].setAlpha(0.2f);
                        Circles[i].setX(coordinate_X_to_Pixel(AP6.getX()) - 1f * Diameter[i] / 2);
                        Circles[i].setY(coordinate_Y_to_Pixel(AP6.getY()) - 1f * Diameter[i] / 2 - (10 * dip2pixel + 0.5f));
                        break;
                    case 7:
                        AP7_ImageView.setColorFilter(color_green);
                        Circles[i].setLayoutParams(new FrameLayout.LayoutParams(Diameter[i], Diameter[i]));
                        Circles[i].setAlpha(0.2f);
                        Circles[i].setX(coordinate_X_to_Pixel(AP7.getX()) - 1f * Diameter[i] / 2);
                        Circles[i].setY(coordinate_Y_to_Pixel(AP7.getY()) - 1f * Diameter[i] / 2 - (10 * dip2pixel + 0.5f));
                        break;
                    case 8:
                        AP8_ImageView.setColorFilter(color_green);
                        Circles[i].setLayoutParams(new FrameLayout.LayoutParams(Diameter[i], Diameter[i]));
                        Circles[i].setAlpha(0.2f);
                        Circles[i].setX(coordinate_X_to_Pixel(AP8.getX()) - 1f * Diameter[i] / 2);
                        Circles[i].setY(coordinate_Y_to_Pixel(AP8.getY()) - 1f * Diameter[i] / 2 - (10 * dip2pixel + 0.5f));
                        break;
                }
            }

        }
    }

    @SuppressLint("MissingPermission")
    private void startRangingRequest() {
        RangingRequest rangingRequest =
                new RangingRequest.Builder().addAccessPoints(RTT_APs).build();

        myWifiRTTManager.startRanging(
                rangingRequest, getApplication().getMainExecutor(), myRTTRangingResultCallback);
    }

    public void onClickStartLoggingData(View view) {
        view.setEnabled(false);
        String url_bit = url_text.getText().toString();
        Log.d("Text", url_bit);
        if (url_bit.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter the server address",
                    Toast.LENGTH_SHORT).show();
        } else {
            String url = "http://" + url_bit + ":5000/server";
            Log.d(TAG, "Start sending to " + url);
            final OkHttpClient client = new OkHttpClient();

            //TODO use thread
            Handler LogRTT_Handler = new Handler();
            Runnable LogRTT_Runnable = new Runnable() {
                @Override
                public void run() {
                    if (Running) {
                        LogRTT_Handler.postDelayed(this, 200);
                        List<String> RangingInfo = new ArrayList<>();
                        for (RangingResult result : Synchronised_RTT) {
                            RangingInfo.add(String.valueOf(result.getMacAddress()));
                            RangingInfo.add(String.valueOf(result.getDistanceMm()));
                            RangingInfo.add(String.valueOf(result.getDistanceStdDevMm()));
                            RangingInfo.add(String.valueOf(result.getRssi()));
                        }

                        RequestBody RTT_body = new FormBody.Builder()
                                .add("RTT_Timestamp", String.valueOf(RTT_timestamp))
                                .add("RTT_Result", String.valueOf(RangingInfo))
                                .add("IMU_Timestamp", String.valueOf(Closest_IMU_timestamp))
                                .add("Accx", String.valueOf(Synchronised_LastAccReading[0]))
                                .add("Accy", String.valueOf(Synchronised_LastAccReading[1]))
                                .add("Accz", String.valueOf(Synchronised_LastAccReading[2]))
                                .add("Gyrox", String.valueOf(Synchronised_LastGyroReading[0]))
                                .add("Gyroy", String.valueOf(Synchronised_LastGyroReading[1]))
                                .add("Gyroz", String.valueOf(Synchronised_LastGyroReading[2]))
                                .add("Magx", String.valueOf(Synchronised_LastMagReading[0]))
                                .add("Magy", String.valueOf(Synchronised_LastMagReading[1]))
                                .add("Magz", String.valueOf(Synchronised_LastMagReading[2]))
                                .add("Azimuth", String.valueOf(Synchronised_orientationAngles[0]))
                                .add("Pitch", String.valueOf(Synchronised_orientationAngles[1]))
                                .add("Roll", String.valueOf(Synchronised_orientationAngles[2]))
                                .build();

                        Request RTT_request = new Request.Builder()
                                .url(url)
                                .post(RTT_body)
                                .build();

                        final Call call = client.newCall(RTT_request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                //Log.i("onFailure",e.getMessage());
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response)
                                    throws IOException {
                                Location_from_server = Objects.requireNonNull(response.body()).string();
                                //Location_from_server = x y [AP1, AP2, AP3] [Distance1, Distance2, Distance3]
                                Log.d(TAG, "Location_from_server = " + Location_from_server);
                                response.close();
                                Calculated_coordinates = Location_from_server.split(" ");

                                APList_start = Location_from_server.indexOf('[');
                                APList_end = Location_from_server.indexOf(']');
                                DisList_start = Location_from_server.indexOf('[', APList_end);
                                DisList_end = Location_from_server.indexOf(']', DisList_start);

                                APList = Location_from_server.substring(APList_start + 1, APList_end);
                                DisList = Location_from_server.substring(DisList_start + 1, DisList_end);
                                //Data format: APList = AP1, AP2, AP3 | DisList = Distance1, Distance2, Distance3
                            }
                        });
                    } else {
                        LogRTT_Handler.removeCallbacks(this);
                    }
                }
            };

            Thread IMU_thread = new Thread(() -> {
                while (Running) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    RequestBody IMU_Body = new FormBody.Builder()
                            .add("Flag", "IMU")
                            .add("Timestamp", String.valueOf(SystemClock.elapsedRealtimeNanos()))
                            .add("Accx", String.valueOf(LastAccReading[0]))
                            .add("Accy", String.valueOf(LastAccReading[1]))
                            .add("Accz", String.valueOf(LastAccReading[2]))
                            .add("Gyrox", String.valueOf(LastGyroReading[0]))
                            .add("Gyroy", String.valueOf(LastGyroReading[1]))
                            .add("Gyroz", String.valueOf(LastGyroReading[2]))
                            .add("Magx", String.valueOf(LastMagReading[0]))
                            .add("Magy", String.valueOf(LastMagReading[1]))
                            .add("Magz", String.valueOf(LastMagReading[2]))
                            .add("Azimuth", String.valueOf(orientationAngles[0]))
                            .add("Pitch", String.valueOf(orientationAngles[1]))
                            .add("Roll", String.valueOf(orientationAngles[2]))
                            .build();

                    Request IMU_Request = new Request.Builder()
                            .url(url)
                            .post(IMU_Body)
                            .build();

                    final Call call = client.newCall(IMU_Request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.i("onFailure", e.getMessage());
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response)
                                throws IOException {
                            String result = Objects.requireNonNull(response.body()).string();
                            response.close();
                            Log.i("result", result);
                        }
                    });
                }

            });
            //IMU_thread.start();
            //wait x ms (only once) before running
            LogRTT_Handler.postDelayed(LogRTT_Runnable, 1000);
        }
    }

    public void onClickChangeVisualisation(View view) {
        if (buttonState == 0) {
            visualiseButton.setText("APs");
            buttonState = 1;

            Snackbar.make(view, "Mode APs: Highlight APs being used for trilateration",
                    Snackbar.LENGTH_SHORT).show();
        } else if (buttonState == 1) {
            visualiseButton.setText("All");
            buttonState = 2;

            Circles[0].setVisibility(View.VISIBLE);
            Circles[1].setVisibility(View.VISIBLE);
            Circles[2].setVisibility(View.VISIBLE);

            Snackbar.make(view, "Mode All: Highlight APs being used for trilateration and draw circle",
                    Snackbar.LENGTH_SHORT).show();
        } else if (buttonState == 2) {
            visualiseButton.setText("None");
            buttonState = 0;

            AP1_ImageView.setColorFilter(color_blue);
            AP2_ImageView.setColorFilter(color_blue);
            AP3_ImageView.setColorFilter(color_blue);
            AP4_ImageView.setColorFilter(color_blue);
            AP5_ImageView.setColorFilter(color_blue);
            AP6_ImageView.setColorFilter(color_blue);
            AP7_ImageView.setColorFilter(color_blue);
            AP8_ImageView.setColorFilter(color_blue);

            Circles[0].setVisibility(View.GONE);
            Circles[1].setVisibility(View.GONE);
            Circles[2].setVisibility(View.GONE);

            Snackbar.make(view, "Mode None: Only display user's path",
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void ScanInBackground() {
        Handler BackgroundScan_Handler = new Handler();
        Runnable BackgroundScan_Runnable = new Runnable() {
            @Override
            public void run() {
                if (Running && (APs_MacAddress.size() < 8)) {
                    Log.d(TAG, "Scanning...");
                    BackgroundScan_Handler.postDelayed(this, 5000);
                    myWifiManager.startScan();
                } else {
                    BackgroundScan_Handler.removeCallbacks(this);
                }
            }
        };
        BackgroundScan_Handler.postDelayed(BackgroundScan_Runnable, 2000);
    }

    private void registerSensors() {
        for (Sensor eachSensor : sensors.values()) {
            sensorManager.registerListener(this,
                    eachSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    private void unregisterSensors() {
        for (Sensor eachSensor : sensors.values()) {
            sensorManager.unregisterListener(this, eachSensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = 0.97f;
        IMU_timestamp = SystemClock.elapsedRealtimeNanos();

        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                LastAccReading[0] = alpha * LastAccReading[0] + (1 - alpha) * sensorEvent.values[0];
                LastAccReading[1] = alpha * LastAccReading[1] + (1 - alpha) * sensorEvent.values[1];
                LastAccReading[2] = alpha * LastAccReading[2] + (1 - alpha) * sensorEvent.values[2];
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                LastMagReading[0] = alpha * LastMagReading[0] + (1 - alpha) * sensorEvent.values[0];
                LastMagReading[1] = alpha * LastMagReading[1] + (1 - alpha) * sensorEvent.values[1];
                LastMagReading[2] = alpha * LastMagReading[2] + (1 - alpha) * sensorEvent.values[2];
                break;

            case Sensor.TYPE_GYROSCOPE:
                LastGyroReading[0] = sensorEvent.values[0];
                LastGyroReading[1] = sensorEvent.values[1];
                LastGyroReading[2] = sensorEvent.values[2];
        }

        // Rotation matrix based on current readings from accelerometer and magnetometer.
        SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix,
                LastAccReading, LastMagReading);
        // Express the updated rotation matrix as three orientation angles.
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        switch (i) {
            case -1:
                Log.d(TAG, "No Contact");
                break;
            case 0:
                Log.d(TAG, "Unreliable");
                break;
            case 1:
                Log.d(TAG, "Low Accuracy");
                break;
            case 2:
                Log.d(TAG, "Medium Accuracy");
                break;
            case 3:
                Log.d(TAG, "High Accuracy");
        }
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            for (ScanResult scanResult : myWifiManager.getScanResults()) {
                if (scanResult.is80211mcResponder()) {
                    if (!APs_MacAddress.contains(scanResult.BSSID)) {
                        APs_MacAddress.add(scanResult.BSSID);
                        RTT_APs.add(scanResult);
                    }
                }
            }
        }
    }

    private class RTTRangingResultCallback extends RangingResultCallback {
        //Start next request
        private void queueNextRangingRequest() {
            RangingRequestDelayHandler.postDelayed(
                    LocalisationActivity.this::startRangingRequest, 100);
        }

        @Override
        public void onRangingFailure(int i) {
            if (Running) {
                queueNextRangingRequest();
            }
        }

        @SuppressLint("WrongConstant")
        @Override
        public void onRangingResults(@NonNull List<RangingResult> list) {
            Ranging_Results.clear();
            for (RangingResult result : list) {
                if (result.getStatus() == 0) {
                    Ranging_Results.add(result);
                }
            }
            RTT_timestamp = SystemClock.elapsedRealtimeNanos();
            Synchronised_RTT = Ranging_Results;
            Synchronised_orientationAngles = orientationAngles;
            Synchronised_LastAccReading = LastAccReading;
            Synchronised_LastGyroReading = LastGyroReading;
            Synchronised_LastMagReading = LastMagReading;
            Closest_IMU_timestamp = IMU_timestamp;
            if (Running) {
                queueNextRangingRequest();
            }
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() LocalisationActivity");
        super.onStop();
        unregisterSensors();
        //unregisterReceiver(myWifiScanReceiver);
        Running = false;
    }

    protected void onResume() {
        Log.d(TAG, "onResume() LocalisationActivity");
        super.onResume();
        registerSensors();
        //registerReceiver(myWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Running = true;
    }
}
