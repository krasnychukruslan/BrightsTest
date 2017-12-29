package com.creatilas.brightstest;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.creatilas.brightstest.StepService.SHAREPREFERENCESTEPSDAY;


public class MainActivity extends AppCompatActivity {

    public static String androidId;
    private TextView textView;
    public static final String BROADCAST_ACTION = "com.creatilas.brightstest";
    private IntentFilter intentFilter;
    private SharedPreferences sharedPreferences;
    private Button start;
    private Button pause;
    private List<ModelDateStep> listDateStep;
    private RecyclerView mRecycler;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Backendless.initApp(getApplicationContext(), Defaults.APPLICATION_ID, Defaults.API_KEY);
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("DeviceId", androidId);
        textView = findViewById(R.id.textViewSteps);
        sharedPreferences = getSharedPreferences(StepService.SHAREPREFERENCESERVICE, MODE_PRIVATE);
        start = findViewById(R.id.btnStart);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showError()) {
                    startService(new Intent(view.getContext(), StepService.class));
                    registerReceiver(receiver, intentFilter);
                    checkStartService();
                }
            }
        });
        pause = findViewById(R.id.btnPause);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(view.getContext(), StepService.class));
                checkStartService();
            }
        });

        mRecycler = findViewById(R.id.recyclerMain);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        intentFilter = new IntentFilter(BROADCAST_ACTION);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkStartService();
        textView.setText(String.valueOf(Math.round(sharedPreferences.getFloat(SHAREPREFERENCESTEPSDAY, 0))));
        registerReceiver(receiver, intentFilter);
        getDateSteps();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver.getDebugUnregister())
            unregisterReceiver(receiver);
    }

    private boolean showError () {
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor senAccelerometer = null;
        if (sensorManager != null) {
            senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (senAccelerometer == null) {
                Toast.makeText(this, "sorry, we can't count your`s steps. Device doesn't supported SENSOR what we use.",
                        Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return true;
            }
        } else {
            Toast.makeText(this, "sorry, we can't count your`s steps. Device doesn't supported SENSOR SERVICE.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private final StepBroadCastReceiver receiver = new StepBroadCastReceiver() {
        public void onReceive(Context context, Intent intent) {
            textView.setText(intent.getStringExtra(StepService.ACTION_UPDATE));
            checkStartService();
        }
    };

    private void checkStartService() {
        if (isMyServiceRunning(StepService.class)) {
            start.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
        } else {
            start.setVisibility(View.VISIBLE);
            pause.setVisibility(View.GONE);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getDateSteps() {
        Backendless.setUrl(Defaults.SERVER_URL);
        Backendless.initApp(getApplicationContext(),
                Defaults.APPLICATION_ID, Defaults.API_KEY);
        String whereClause = "deviceId = '" + MainActivity.androidId + "'";
        final DataQueryBuilder dataQueryBuilder = DataQueryBuilder.create();
        dataQueryBuilder.setWhereClause(whereClause);
        Backendless.Persistence.of("user_step").find(dataQueryBuilder,
                new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> found) {
                        if (found.size() != 0) {
                            RecyclerView.Adapter adapter = new DateStepAdapter(getDate(found));
                            mRecycler.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.e("error getDateSteps", "error");
                    }
                });
    }

    private List<ModelDateStep> getDate(List<Map> maps) {
        listDateStep = new ArrayList<>();
        for (Map current : maps) {
            Log.d("date", String.valueOf(current.get("currentDate")));
            Log.d("steps", String.valueOf(current.get("steps")));
            listDateStep.add(new ModelDateStep(String.valueOf(current.get("currentDate")), String.valueOf(current.get("steps"))));
        }
        return listDateStep;
    }
}

