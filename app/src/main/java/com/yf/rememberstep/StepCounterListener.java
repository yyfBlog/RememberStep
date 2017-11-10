package com.yf.rememberstep;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by yyf on 2017/11/7.
 */

/**
 * 由于传感器的回调会非常灵敏，所以在这里应该是等待一段时间，并将这个步数存储起来
 * 供UI读取
 * <p>
 * 经测试发现魅族mx6在传感器这块应该是做了处理，没有Vivo 回调的快，会有间隔
 *
 * oppo 前10步不计，后面没走一步返回一步
 *
 * 魅蓝5s 没有不支持这个传感器
 */
public class StepCounterListener implements SensorEventListener {
    private static final String TAG = "StepCounterListener";
    private ScheduledExecutorService mScheduledExecutorService;

    public StepCounterListener() {
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            Log.d(TAG, "onSensorChanged: " + sensorEvent.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "onAccuracyChanged: ");
    }

    private class mRunnable implements Runnable {
        @Override
        public void run() {

        }
    }
}
