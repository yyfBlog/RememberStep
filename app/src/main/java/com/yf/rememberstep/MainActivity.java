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
import android.util.Log;
import android.widget.TextView;

import com.yf.rememberstep.aidl.IStepCounterProcess;
import com.yf.rememberstep.interfaces.IStepCounterSensorChange;
import com.yf.rememberstep.services.StepCounterServiceLocal;
import com.yf.rememberstep.services.StepCounterServiceRemote;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements IStepCounterSensorChange {
    private static final String TAG = "MainActivity";
    private TextView tv_count;
    private IStepCounterProcess mIStepCounterProcess;
    private ScheduledExecutorService mScheduledExecutorService;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mIStepCounterProcess != null) {
                tv_count.setText("" + getCurentStepCount());
            }
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
//        Intent intent = new Intent(this, StepCounterServiceLocal.class);
//        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
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
     * 与另外一个进程建立双向绑定
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            try {
                mIStepCounterProcess = IStepCounterProcess.Stub.asInterface(iBinder);
                Log.d(TAG, "onServiceConnected: 服务连接成功" + mIStepCounterProcess.getStepsNumber());
                tv_count.setText("" + getCurentStepCount());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: 服务断开连接");
            mIStepCounterProcess = null;
            startService(new Intent(MainActivity.this, StepCounterServiceLocal.class));
        }
    };

    /**
     * 获取当前步数
     */
    private double getCurentStepCount() {
        try {
            if (mIStepCounterProcess != null) {
                return mIStepCounterProcess.getStepsNumber();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
