package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2024/9/28 13:30
 *    author : Roy
 *    version: 1.0
 */
@Route(path = "/chat/details")
class MainActivity7:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main7)
    }
}