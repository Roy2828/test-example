package com.example.myapplication

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.StrictMode
import com.alibaba.android.arouter.launcher.ARouter
import com.example.myapplication.matrix.AppMatrix
import com.example.myapplication.utils.HookUtil
import com.example.myapplication.utils.WeChatLargeScreenAdapter
import me.jessyan.autosize.AutoSize
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.onAdaptListener


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

        // 捕获全局未捕获异常，彻底静默拦截 StrictMode$StrictModeViolation，防止控制台打印日志与崩溃
        val defaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (isStrictModeViolation(throwable)) {
                // 静默拦截 StrictMode 相关的非致命异常
            } else {
                defaultUncaughtHandler?.uncaughtException(thread, throwable)
            }
        }

        try {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
            StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        ARouter.init(this);

        AutoSize.checkAndInit(this)

        AutoSizeConfig.getInstance().setOnAdaptListener(object : onAdaptListener {
            override fun onAdaptBefore(target: Any?, activity: Activity?) {
                activity?.let(WeChatLargeScreenAdapter::adapt)
            }

            override fun onAdaptAfter(target: Any?, activity: Activity?) {

            }
        })

        AppMatrix.initMatrix(this)

    }

    override fun attachBaseContext(base: Context?) {

        try {
            HookUtil.attachContext()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.attachBaseContext(base)
    }

    private fun isStrictModeViolation(throwable: Throwable?): Boolean {
        var current: Throwable? = throwable
        while (current != null) {
            val className = current.javaClass.name
            val msg = current.message ?: ""
            if (className.contains("StrictMode") || className.contains("BlockGuard") || msg.contains("StrictMode")) {
                return true
            }
            for (suppressed in current.suppressed) {
                if (isStrictModeViolation(suppressed)) {
                    return true
                }
            }
            current = current.cause
        }
        return false
    }
}
