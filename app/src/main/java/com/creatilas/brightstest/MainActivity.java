package com.creatilas.brightstest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor senAccelerometer;
    private TextView textView;
    private Button start;
    private Button pause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("DeviceId", androidId);
        textView = findViewById(R.id.textViewSteps);
        start = findViewById(R.id.btnStart);
        pause = findViewById(R.id.btnPause);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            sensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        textView.setText(String.valueOf(sensorEvent.values[0]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
