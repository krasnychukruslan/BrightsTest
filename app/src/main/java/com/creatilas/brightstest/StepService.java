package com.creatilas.brightstest;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.DataQueryBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        Toast.makeText(this, " We start count your steps =) ", Toast.LENGTH_SHORT).show();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sharedPreferences = getSharedPreferences(SHAREPREFERENCESERVICE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHAREPREFERENCESTARTSERVICE, true).apply();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyRun mr = new MyRun();
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
        Toast.makeText(this, " We stopped counting your steps =( ", Toast.LENGTH_SHORT).show();
        sharedPreferences = getSharedPreferences(SHAREPREFERENCESERVICE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHAREPREFERENCESTARTSERVICE, false).apply();
    }

    class MyRun implements Runnable {
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
                            updateSteps();
                            updateIntent.putExtra(ACTION_UPDATE, String.valueOf(Math.round(sharedPreferences.getFloat(SHAREPREFERENCESTEPSDAY, 0))));
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
        if (!sharedPreferences.getString(SHAREPREFERENCECURRENTDAY,"").equals(getDay())) {
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

    private void setSteps() {
        Backendless.setUrl(Defaults.SERVER_URL);
        Backendless.initApp(getApplicationContext(),
                Defaults.APPLICATION_ID, Defaults.API_KEY);

        HashMap<String, String> testObject = new HashMap<>();
        testObject.put( "steps", String.valueOf(Math.round(sharedPreferences.getFloat(SHAREPREFERENCESTEPSDAY, 0))));
        testObject.put( "deviceId", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        testObject.put( "currentDate", sharedPreferences.getString(SHAREPREFERENCECURRENTDAY, ""));
        Backendless.Data.of( "user_step" ).save(testObject, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map response) {
                Log.d("Send", "Steps");
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e( "MYAPP", "Server reported an error " + fault.getMessage() );
            }
        });
    }

    private void updateSteps() {
        Backendless.setUrl(Defaults.SERVER_URL);
        Backendless.initApp(getApplicationContext(),
                Defaults.APPLICATION_ID, Defaults.API_KEY);
        String whereClause = "currentDate = " + getDay();
        DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setWhereClause(whereClause);
//        List<Map> result = Backendless.Persistence.of( "user_step" ).find(dataQueryBuilder);
//        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
//        dataQuery.setWhereClause( whereClause );
        Backendless.Persistence.of( "user_step" ).find(dataQueryBuilder,
            new AsyncCallback<List<Map>>(){
                @Override
                public void handleResponse( List<Map> foundContacts )
                {
                    Log.d("qqqqq", "good");
                    HashMap<String, String> update = new HashMap<>();
                    update.put( "steps", String.valueOf(Math.round(sharedPreferences.getFloat(SHAREPREFERENCESTEPSDAY, 0))));
                    update.put( "objectId", foundContacts.get(0).toString());
                    update.put( "deviceId", MainActivity.androidId);
                    update.put( "currentDate", sharedPreferences.getString(SHAREPREFERENCECURRENTDAY, ""));
                    Backendless.Data.of("user_step").save(update, new AsyncCallback<Map>() {
                        public void handleResponse(Map saved)
                        {
                            Log.d("qqqq", "update");
                        }
                        @Override
                        public void handleFault( BackendlessFault fault )
                        {
                            // an error has occurred, the error code can be retrieved with fault.getCode()
                        }
                    });

                }
                @Override
                public void handleFault( BackendlessFault fault )
                {
                    // an error has occurred, the error code can be retrieved with fault.getCode()
                    Log.d("qqqqq", "error");
                }
            });

//        HashMap<String, Object> testObject = new HashMap<>();
//        testObject.put( "steps", String.valueOf(Math.round(sharedPreferences.getFloat(SHAREPREFERENCESTEPSDAY, 0))));
//        Backendless.Data.of( "user_step" ).update(MainActivity.androidId, testObject);
    }
}
