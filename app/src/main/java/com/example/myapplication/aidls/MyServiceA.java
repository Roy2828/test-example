package com.example.myapplication.aidls;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.example.clife_gait.ClifeGaitPlugin;
import com.example.myapplication.IMyAidlInterface;
import com.example.myapplication.IMyService;

/**
 * desc   :
 * e-mail : 1391324949@qq.com
 * date   : 2024/10/2 19:57
 * author : Roy
 * version: 1.0
 */
public class MyServiceA extends Service {

    private static final String TAG = "MyServiceA";
    private final IMyService.Stub mBinder = new IMyService.Stub() {
        @Override
        public String getMessage() {
            //这个方法在 Binder:3206_3  这个线程池中调用
            return "Hello, this is a message from the service!";
        }

        @Override
        public void initialize()  {
            ClifeGaitPlugin.INSTANCE.onMethodCall(getBaseContext(),"initialize");
        }

        @Override
        public void startScan()  {
            ClifeGaitPlugin.INSTANCE.onMethodCall(getBaseContext(),"startScan");
        }

        @Override
        public void stopScan() {

            ClifeGaitPlugin.INSTANCE.onMethodCall(getBaseContext(),"stopScan");
            killServiceProcess();
        }

        @Override
       public void unwatchOnlineState( int conversationType, String[] targets,  IMyAidlInterface callback) throws RemoteException {
            callback.onSuccess();
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private void killServiceProcess() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String processName = getPackageName() + ":service_aidl"; // 与android:process匹配

        // 查找并终止进程
        for (ActivityManager.RunningAppProcessInfo procInfo : am.getRunningAppProcesses()) {
            if (procInfo.processName.equals(processName)) {
                android.os.Process.killProcess(procInfo.pid);
                break;
            }
        }
    }
}