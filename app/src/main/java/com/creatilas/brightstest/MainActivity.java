package com.creatilas.brightstest;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.IDataStore;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import java.util.Map;


public class MainActivity extends AppCompatActivity {

    public static String androidId;
    private TextView textView;
    public static final String BROADCAST_ACTION = "com.creatilas.brightstest";
    private IntentFilter intentFilter;
    private SharedPreferences sharedPreferences;
    private Button start;
    private Button pause;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Backendless.initApp( getApplicationContext(), Defaults.APPLICATION_ID, Defaults.API_KEY);
        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("DeviceId", androidId);
        textView = findViewById(R.id.textViewSteps);
        sharedPreferences = getSharedPreferences(StepService.SHAREPREFERENCESERVICE, MODE_PRIVATE);
        start = findViewById(R.id.btnStart);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(view.getContext(), StepService.class));
                registerReceiver(receiver, intentFilter);
                checkStartService();
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
        intentFilter = new IntentFilter(BROADCAST_ACTION);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkStartService();
        textView.setText(String.valueOf(sharedPreferences.getFloat(StepService.SHAREPREFERENCESTEPSDAY, 0)));
        registerReceiver(receiver, intentFilter);
        getSteps();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver.getDebugUnregister())
            unregisterReceiver(receiver);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            textView.setText(intent.getStringExtra(StepService.ACTION_UPDATE));
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

    private void getSteps() {
        IDataStore<Map> contactStorage = Backendless.Data.of( "user_step" );

        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause( "deviceId = '" + androidId + "'");
        contactStorage.getObjectCount( queryBuilder, new AsyncCallback<Integer>()
        {
            @Override
            public void handleResponse( Integer objectCount )
            {
                Log.i( "MYAPP", "There are " + objectCount + " objects matching the query" );
            }

            @Override
            public void handleFault( BackendlessFault fault )
            {
                Log.e( "MYAPP", "Server reported an error - " + fault.getMessage() );
            }
        } );
    }
}

