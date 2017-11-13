package com.yf.rememberstep.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.yf.rememberstep.aidl.IStepCounterProcess;

/**
 * Created by yyf on 2017/11/10.
 */

public class StepCounterServiceRemote extends Service {
    private static final String TAG = "StepCounterServiceRemo";
    private MyBinder mMyBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        mMyBinder = new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        bindService(new Intent(StepCounterServiceRemote.this, StepCounterServiceLocal.class), mServiceConnection, Context.BIND_IMPORTANT);
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMyBinder;
    }

    private class MyBinder extends IStepCounterProcess.Stub {
        @Override
        public double getStepsNumber() throws RemoteException {
            return 0;
        }
    }

    /**
     * 与本地服务绑定
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: ");
            Toast.makeText(getApplicationContext(), TAG + "服务连接", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: ");
            StepCounterServiceRemote.this.startService(new Intent(StepCounterServiceRemote.this, StepCounterServiceLocal.class));
            StepCounterServiceRemote.this.bindService(new Intent(StepCounterServiceRemote.this,StepCounterServiceLocal.class),mServiceConnection,Context.BIND_IMPORTANT);
            Toast.makeText(getApplicationContext(), TAG + "服务断开连接", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }
}
