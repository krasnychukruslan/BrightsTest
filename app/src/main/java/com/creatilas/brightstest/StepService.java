package com.creatilas.brightstest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by rusci on 27-Dec-17.
 */

public class StepService extends Service {

    public static final String ACTION_UPDATE = "com.creatilas.brightstest.intentservice.UPDATE";
    private String text;
    private ExecutorService es;
    private SensorManager sensorManager;
    private boolean startService;

    @Override
    public void onCreate() {
        super.onCreate();
        es = Executors.newFixedThreadPool(1);
        Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        startService = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyRun mr = new MyRun(startId);
        es.execute(mr);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Sensor senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
//                sensorManager.registerListener(new SensorEventListener() {
//                    @Override
//                    public void onSensorChanged(SensorEvent sensorEvent) {
//                        text = String.valueOf(sensorEvent.values[0]);
//                        Log.d("test", text);
//                    }
//
//                    @Override
//                    public void onAccuracyChanged(Sensor sensor, int i) {
//
//                    }
//                }, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
//
//                Intent updateIntent = new Intent(MainActivity.BROADCAST_ACTION);
//                updateIntent.putExtra(ACTION_UPDATE, String.valueOf(text));
//                sendBroadcast(updateIntent);
//            }
//        });
        return Service.START_STICKY;
    }
//        return Service.START_STICKY;


    class MyRun implements Runnable {
        int startId;
        MyRun(int startId) {
            this.startId = startId;
        }

        public void run() {
            if (sensorManager != null) {
                Sensor senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                sensorManager.registerListener(new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent sensorEvent) {
                        text = String.valueOf(sensorEvent.values[0]);
                        if (startService) {
                            Intent updateIntent = new Intent(MainActivity.BROADCAST_ACTION);
                            updateIntent.putExtra(ACTION_UPDATE, String.valueOf(text));
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        startService = false;
    }
}
