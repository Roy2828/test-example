package com.example.myapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 * desc   :
 * e-mail : 1391324949@qq.com
 * date   : 2024/8/16 10:24
 * author : Roy
 * version: 1.0
 */
public class MainActivity2 extends AppCompatActivity {
    IService iService;
    MyConn conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    public void bind(View view) {
        Intent intent = new Intent(this, TestService.class);
        conn = new MyConn();
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    public void startService(View view) {
        Intent intent = new Intent(this, TestService.class);
        startService(intent);
    }

    private class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iService = (IService) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    }

    /**
     * 调用借口方法
     *
     * @param view
     */
    public void call(View view) {
        iService.callMethodInService();
    }

    public void unbind(View view) {
        unbindService(conn);
        conn = null;
    }

}
