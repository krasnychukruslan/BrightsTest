package com.creatilas.brightstest.servicestepaccelerometer;

import android.app.Service;
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

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.creatilas.brightstest.Defaults;
import com.creatilas.brightstest.MainActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.creatilas.brightstest.servicestepcounter.StepService.ACTION_UPDATE;
import static com.creatilas.brightstest.servicestepcounter.StepService.SHAREPREFERENCECURRENTDAY;
import static com.creatilas.brightstest.servicestepcounter.StepService.SHAREPREFERENCESERVICE;
import static com.creatilas.brightstest.servicestepcounter.StepService.SHAREPREFERENCESTARTSERVICE;
import static com.creatilas.brightstest.servicestepcounter.StepService.SHAREPREFERENCESTEPSDAY;

/**
 * Created by rusci on 27-Dec-17.
 */

public class StepAccelerometerService extends Service implements SensorEventListener, StepListener {

    private ExecutorService es;
    private SharedPreferences sharedPreferences;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;

    @Override
    public void onCreate() {
        super.onCreate();
        es = Executors.newFixedThreadPool(1);
        Toast.makeText(this, " We start count your steps =) ", Toast.LENGTH_SHORT).show();
        sharedPreferences = getSharedPreferences(SHAREPREFERENCESERVICE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHAREPREFERENCESTARTSERVICE, true).apply();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyRun mr = new MyRun();
        es.execute(mr);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Log.d("SensorManager", " = null");
        }
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
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
        Toast.makeText(this, " We stopped counting your steps =( ", Toast.LENGTH_SHORT).show();
        sharedPreferences = getSharedPreferences(SHAREPREFERENCESERVICE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHAREPREFERENCESTARTSERVICE, false).apply();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void step(long time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(SHAREPREFERENCESTEPSDAY, sharedPreferences.getFloat(SHAREPREFERENCESTEPSDAY, 0) + 1).apply();
        Intent updateIntent = new Intent(MainActivity.BROADCAST_ACTION);
        updateIntent.putExtra(ACTION_UPDATE, String.valueOf(Math.round(sharedPreferences.getFloat(SHAREPREFERENCESTEPSDAY, 0))));
        checkDay();
        updateSteps();
        sendBroadcast(updateIntent);
    }

    private void checkDay() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.getString(SHAREPREFERENCECURRENTDAY, "").equals(getDay())) {
            editor.putString(SHAREPREFERENCECURRENTDAY, getDay()).apply();
            editor.putFloat(SHAREPREFERENCESTEPSDAY, 0).apply();
            setSteps();
        }
    }

    private String getDay() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private static String getObjectId(List<Map> maps) {
        String id = null;
        for (Map current : maps) {
            Log.i("device", String.valueOf(current.get("objectId")));
            id = String.valueOf(current.get("objectId"));
        }
        return id;
    }

    private void setSteps() {
        Backendless.setUrl(Defaults.SERVER_URL);
        Backendless.initApp(getApplicationContext(),
                Defaults.APPLICATION_ID, Defaults.API_KEY);

        HashMap<String, String> testObject = new HashMap<>();
        testObject.put(Defaults.STEPS, String.valueOf(Math.round(sharedPreferences.getFloat(SHAREPREFERENCESTEPSDAY, 0))));
        testObject.put(Defaults.DEVICE_ID, MainActivity.androidId);
        testObject.put(Defaults.CURRENT_DATE, sharedPreferences.getString(SHAREPREFERENCECURRENTDAY, ""));
        Backendless.Data.of(Defaults.TABLE_NAME).save(testObject, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map response) {
                Log.d("setSteps", "Create Steps");
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e("setSteps", "Server reported an error " + fault.getMessage());
            }
        });
    }

    private void updateSteps() {
        Backendless.setUrl(Defaults.SERVER_URL);
        Backendless.initApp(getApplicationContext(),
                Defaults.APPLICATION_ID, Defaults.API_KEY);
        String whereClause = "deviceId = '" + MainActivity.androidId + "'" + " AND currentDate = '" + this.getDay() + "'";
        final DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setWhereClause(whereClause);
        Backendless.Persistence.of("user_step").find(dataQueryBuilder,
                new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> found) {
                        if (found.size() != 0) {
                            HashMap<String, String> update = new HashMap<>();
                            update.put(Defaults.STEPS, String.valueOf(Math.round(sharedPreferences.getFloat(SHAREPREFERENCESTEPSDAY, 0))));
                            update.put(Defaults.DEVICE_ID, MainActivity.androidId);
                            update.put(Defaults.CURRENT_DATE, sharedPreferences.getString(SHAREPREFERENCECURRENTDAY, ""));
                            update.put("objectId", getObjectId(found));
                            Backendless.Data.of("user_step").save(update, new AsyncCallback<Map>() {
                                public void handleResponse(Map saved) {
                                    Log.e("updateSteps", "update");
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Log.e("updateSteps", "update error " + fault.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.d("updateSteps", "error " + fault.getMessage());
                    }
                });
    }

    class MyRun implements Runnable {
        public void run() {
            updateSteps();
            Log.d("steps", String.valueOf(sharedPreferences.getFloat(SHAREPREFERENCESTEPSDAY, 0)));
        }
    }
}
