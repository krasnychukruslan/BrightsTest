package com.creatilas.brightstest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by rusci on 27-Dec-17.
 */

public class StepService extends Service {

    public static final String ACTION_UPDATE = "com.creatilas.brightstest.intentservice.UPDATE";
    public static final String SHAREPREFERENCESERVICE = "com.creatilas.brightstest.SERVICE";
    public static final String SHAREPREFERENCESTARTSERVICE = "com.creatilas.brightstest.STARTSERVICE";
    public static final String SHAREPREFERENCECURRENTDAY = "com.creatilas.brightstest.CURRENTDAY";
    public static final String SHAREPREFERENCESTEPSDAY = "com.creatilas.brightstest.STEPSDAY";

    private String text;
    private ExecutorService es;
    private SensorManager sensorManager;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        es = Executors.newFixedThreadPool(1);
        Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = getSharedPreferences(SHAREPREFERENCESERVICE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHAREPREFERENCESTARTSERVICE, true).apply();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyRun mr = new MyRun(startId);
        es.execute(mr);
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        sharedPreferences = getSharedPreferences(SHAREPREFERENCESERVICE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHAREPREFERENCESTARTSERVICE, false).apply();
    }

    class MyRun implements Runnable {
        int startId;
        MyRun(int startId) {
            this.startId = startId;
        }

        public void run() {
            checkDay();
            if (sensorManager != null) {
                Sensor senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                sensorManager.registerListener(new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent sensorEvent) {
                        text = String.valueOf(sensorEvent.values[0]);
                        if (sharedPreferences.getBoolean(SHAREPREFERENCESTARTSERVICE, false)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putFloat(SHAREPREFERENCESTEPSDAY, sharedPreferences.getFloat(SHAREPREFERENCESTEPSDAY, 0) + 1).apply();
                            Intent updateIntent = new Intent(MainActivity.BROADCAST_ACTION);
                            updateIntent.putExtra(ACTION_UPDATE, String.valueOf(sharedPreferences.getFloat(SHAREPREFERENCESTEPSDAY, 0)));
                            sendBroadcast(updateIntent);
                            Log.d("test", text);
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int i) {

                    }
                }, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    private void checkDay() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getString(SHAREPREFERENCECURRENTDAY,"").equals("")) {
            editor.putString(SHAREPREFERENCECURRENTDAY, getDay()).apply();
            editor.putFloat(SHAREPREFERENCESTEPSDAY, 0).apply();
        } else if (!sharedPreferences.getString(SHAREPREFERENCECURRENTDAY,"").equals(getDay())) {
            editor.putString(SHAREPREFERENCECURRENTDAY, getDay()).apply();
            editor.putFloat(SHAREPREFERENCESTEPSDAY, 0).apply();
        }
    }

    private String getDay() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
