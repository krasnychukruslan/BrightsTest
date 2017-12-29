package com.creatilas.brightstest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.creatilas.brightstest.servicestepaccelerometer.StepAccelerometerService;
import com.creatilas.brightstest.servicestepcounter.StepService;

/**
 * Created by rusci on 29-Dec-17.
 */

public class StepBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
                    Intent serviceIntent = new Intent(context, StepService.class);
                    context.startService(serviceIntent);
                }
                if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                    Intent serviceIntent = new Intent(context, StepAccelerometerService.class);
                    context.startService(serviceIntent);
                }
            }
        }
    }
}

