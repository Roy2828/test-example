package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * desc   :
 * e-mail : 1391324949@qq.com
 * date   : 2024/8/16 9:45
 * author : Roy
 * version: 1.0
 */
public class MyService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}