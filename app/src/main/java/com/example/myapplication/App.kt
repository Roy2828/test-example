package com.example.myapplication

import android.app.Application
import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter
import com.example.myapplication.utils.HookUtil


/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2024/9/28 13:51
 *    author : Roy
 *    version: 1.0
 */
class App :Application() {
    override fun onCreate() {
        super.onCreate()
        ARouter.init(this);
    }

    override fun attachBaseContext(base: Context?) {

        try {
            HookUtil.attachContext()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.attachBaseContext(base)
    }
}