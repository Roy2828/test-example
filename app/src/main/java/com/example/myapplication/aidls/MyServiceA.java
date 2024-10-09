package com.example.myapplication.aidls;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

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

    private final IMyService.Stub mBinder = new IMyService.Stub() {
        @Override
        public String getMessage() {
            return "Hello, this is a message from the service!";
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
}