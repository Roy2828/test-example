package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.window.SplashScreen
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity


/**
 * desc   :
 * e-mail : 1391324949@qq.com
 * date   : 2024/11/7 18:38
 * author : Roy
 * version: 1.0
 */
class MainSplashActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainSplashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_splash_activity)
        viewModel.liveData.observe(this){
            startActivity(Intent(this@MainSplashActivity, MainActivity::class.java))
            Log.e("Roy","跳转")
            finish()
        }

        viewModel.start()


   //     val binding = ActivityMainBinding.inflate(layoutInflater)
    }
}
