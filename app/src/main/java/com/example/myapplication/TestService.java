package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class TestService extends Service {

    private class MyBinder extends Binder implements IService {
        @Override
        public void callMethodInService() {
            methodInService();
        }
    }

    private void methodInService() {
        Toast.makeText(this, "调用Bind方法", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("onbind");
        return new MyBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("onunbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        System.out.println("onstart");
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onCreate() {
        System.out.println("oncreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        System.out.println("onDestroy");
        super.onDestroy();
    }


}
