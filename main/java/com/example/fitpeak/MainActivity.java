package com.example.fitpeak;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks  {


    private GoogleApiClient apiclient;
    private SensorManager sensorManager;
    private Sensor mLight;
    float lux = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        init();
        inith();
    }


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        lux = event.values[0];
        TextView lrate = findViewById(R.id.input);
        lrate.setText("heartrate is currently: " + lux);
        String text = lrate.getText().toString();
        sendMessage("/message", text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void inith() {
        apiclient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .build();
        apiclient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        apiclient.disconnect();
    }

    private void init() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void sendMessage( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( apiclient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            apiclient, node.getId(), path, text.getBytes() ).await();
                }

                runOnUiThread( new Runnable() {
                    @Override

                    public void run() {
                        TextView lrate = findViewById(R.id.input);
                        if(lux==0) {
                            lrate.setText("detecting heart rate.....");
                        }
                        else{
                            lrate.setText("current heart rate: " + lux);
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onConnected(Bundle bundle) {
        sendMessage("/start_activity", "");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}