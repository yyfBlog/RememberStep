package com.yf.rememberstep;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.yf.rememberstep.aidl.IStepCounterProcess;
import com.yf.rememberstep.interfaces.IStepCounterSensorChange;
import com.yf.rememberstep.services.StepCounterServiceLocal;
import com.yf.rememberstep.services.StepCounterServiceRemote;
import com.yf.rememberstep.utils.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements IStepCounterSensorChange {
    private static final String TAG = "MainActivity";
    private TextView tv_count;
    private ScheduledExecutorService mScheduledExecutorService;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tv_count.setText("" + getCurentStepCount());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        startService(new Intent(this, StepCounterServiceLocal.class));
        startService(new Intent(this, StepCounterServiceRemote.class));

        tv_count = (TextView) findViewById(R.id.tv_count);
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(0);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stepCounterChange(int currentCount) {
        tv_count.setText(String.valueOf(currentCount));
    }


    /**
     * 获取当前步数
     */
    private double getCurentStepCount() {
        try {
            String todayData = (String) SharedPreferencesUtils.getParam(getApplicationContext(), "todayData", "");
            if (!TextUtils.isEmpty(todayData)) {
                JSONObject jsonObject = new JSONObject(todayData);
                int oldTodayCount = jsonObject.getInt("todayCount");
                return oldTodayCount;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
