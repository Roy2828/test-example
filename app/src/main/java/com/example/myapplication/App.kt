package com.example.myapplication

import android.app.Activity
import android.app.Application
import android.content.Context
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
}
