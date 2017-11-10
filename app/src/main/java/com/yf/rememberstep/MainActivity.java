package com.yf.rememberstep;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isSupportStepCounterSensor();
    }

    /**
     * 首先判断手机设备是否支持计步传感器
     */
    private void isSupportStepCounterSensor() {
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);//步进式传感器
        if (mSensor != null) {
            mSensorManager.registerListener(new StepCounterListener(), mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        } else {// 不支持该计步传感器
            Log.e(TAG, "isSupportStepCounterSensor:  不支持传感器");
        }
    }
}
