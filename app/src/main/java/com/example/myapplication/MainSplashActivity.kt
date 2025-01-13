package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * desc   :
 * e-mail : 1391324949@qq.com
 * date   : 2024/11/7 18:38
 * author : Roy
 * version: 1.0
 */
public class MainSplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_splash_activity);
         startActivity(new Intent(this,MainActivity.class));
       finish();
    }
}
