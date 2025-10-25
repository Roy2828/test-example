package com.example.myapplication.aidls;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.myapplication.IMyService;
import com.example.myapplication.R;
import com.example.myapplication.data.DataTest;

/**
 * desc   :
 * e-mail : 1391324949@qq.com
 * date   : 2024/10/2 19:59
 * author : Roy
 * version: 1.0
 */
@Route(path = "/user/adil")
public class MainActivityAidl extends AppCompatActivity {
    private IMyService myService;
    private boolean isBound = false;

    @Autowired
    DataTest dataTest;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = IMyService.Stub.asInterface(service);
            isBound = true;
            try {
                String message = myService.getMessage();
                TextView textView = findViewById(R.id.text_view);
                textView.setText(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ARouter.getInstance().inject(this);
        setContentView(R.layout.activity_main_aidl);
      Intent intent =  new Intent("com.example.IMyService");
         intent.setPackage("com.example.myapplication");
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}
