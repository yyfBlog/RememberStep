package com.yf.rememberstep.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.yf.rememberstep.MainActivity;
import com.yf.rememberstep.aidl.IStepCounterProcess;
import com.yf.rememberstep.interfaces.IStepCounterSensorChange;
import com.yf.rememberstep.utils.DateUtils;
import com.yf.rememberstep.utils.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by yyf on 2017/11/7.
 */

/**
 * 由于传感器的回调会非常灵敏，所以在这里应该是等待一段时间，并将这个步数存储起来
 * 供UI读取
 * <p>
 * 经测试发现魅族mx6在传感器这块应该是做了处理，没有Vivo 回调的快，会有间隔
 * <p>
 * oppo 前10步不计，后面没走一步返回一步
 * <p>
 * 魅蓝5s 没有不支持这个传感器
 */
public class StepCounterServiceLocal extends Service implements SensorEventListener {
    private static final String TAG = "StepCounterServiceLocal";
    private int todayCount;
    private JSONObject mJSONObject;
    private MyBinder mMyBinder;
    private IStepCounterProcess mIStepCounterProcess;
    private ScheduledExecutorService mScheduledExecutorService;

    @Override
    public void onCreate() {
        super.onCreate();
        mJSONObject = new JSONObject();
        isSupportStepCounterSensor();
        mMyBinder = new MyBinder();
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        bindService(new Intent(this, StepCounterServiceRemote.class), mServiceConnection, Context.BIND_IMPORTANT);
        startTask();
        return START_REDELIVER_INTENT;
    }


    private void startTask() {
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    String oldTodayData = (String) SharedPreferencesUtils.getParam(getApplicationContext(), "todayData", "");
                    if (!TextUtils.isEmpty(oldTodayData)) {
                        JSONObject jsonObject = new JSONObject(oldTodayData);
                        int oldTodayCount = jsonObject.getInt("todayCount");
                        String oldTodayTime = jsonObject.getString("todayTime");
                        if (oldTodayTime.equals(DateUtils.getTodayDate())) {
                            //如果是发生在同一天内 那么今天的步数=上次保存的步数+ 传感器获取的步数
                            if (oldTodayCount > todayCount) {
                                //如果保存的步数大于传感器发出的步数，那么可以断定手机关机了
                                todayCount = oldTodayCount + todayCount;
                            }
                            mJSONObject.put("todayCount", todayCount);
                        } else { //如果不是同一天且传感器的步数大于保存的 那么可以断定 手机没有关机
                            if (todayCount > oldTodayCount) {
                                //今天的步数=传感器的步数-上次保存的步数
                                todayCount = todayCount - oldTodayCount;
                            }
                            mJSONObject.put("todayCount", todayCount);
                        }
                    }else {
                        //假设用户没关机 有1万步
                        mJSONObject.put("todayCount", todayCount);
                    }
                    mJSONObject.put("todayTime", DateUtils.getTodayDate());
                    SharedPreferencesUtils.setParam(getApplicationContext(), "todayData", mJSONObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 0,1000, TimeUnit.MILLISECONDS);
    }


    /**
     * 与另外一个进程建立双向绑定
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                mIStepCounterProcess = IStepCounterProcess.Stub.asInterface(iBinder);
                Log.d(TAG, "onServiceConnected: 服务连接成功" + mIStepCounterProcess.getStepsNumber());
                Toast.makeText(getApplicationContext(), TAG + "服务连接", Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: 服务断开连接");
            Toast.makeText(getApplicationContext(), TAG + "服务断开连接", Toast.LENGTH_SHORT).show();
            mIStepCounterProcess = null;
            StepCounterServiceLocal.this.startService(new Intent(StepCounterServiceLocal.this, StepCounterServiceRemote.class));
            StepCounterServiceLocal.this.bindService(new Intent(StepCounterServiceLocal.this, StepCounterServiceRemote.class), mServiceConnection, Context.BIND_IMPORTANT);
        }
    };


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            Log.d(TAG, "onSensorChanged: " + sensorEvent.values[0]);
            todayCount = (int) sensorEvent.values[0];

            Log.d(TAG, "onSensorChanged: "+sensorEvent.timestamp);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "onAccuracyChanged: ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMyBinder;
    }


    /**
     * 进程间通信
     */
    private class MyBinder extends IStepCounterProcess.Stub {
        @Override
        public double getStepsNumber() throws RemoteException {
            return todayCount;
        }
    }


    /**
     * 首先判断手机设备是否支持计步传感器
     */
    private void isSupportStepCounterSensor() {
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);//步进式传感器
        if (mSensor != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        } else {// 不支持该计步传感器
            Log.e(TAG, "isSupportStepCounterSensor:  不支持传感器");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}
