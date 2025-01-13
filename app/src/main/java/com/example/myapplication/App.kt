package com.example.myapplication

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.alibaba.android.arouter.launcher.ARouter
import com.example.myapplication.utils.HookUtil
import me.jessyan.autosize.AutoSize
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.onAdaptListener
import me.jessyan.autosize.utils.ScreenUtils


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
      /*  AutoSizeConfig.getInstance().setOnAdaptListener(object : onAdaptListener {
            override fun onAdaptBefore(target: Any?, activity: Activity?) {
                //使用以下代码, 可以解决横竖屏切换时的屏幕适配问题
                //首先设置最新的屏幕尺寸，ScreenUtils.getScreenSize(activity) 的参数一定要不要传 Application !!!
                AutoSizeConfig.getInstance().setScreenWidth(ScreenUtils.getScreenSize(activity)[0]);
                AutoSizeConfig.getInstance().setScreenHeight(ScreenUtils.getScreenSize(activity)[1]);
                //根据屏幕方向，设置设计尺寸
         *//*       activity?.apply {
                    if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        //设置横屏设计尺寸
                        AutoSizeConfig.getInstance()
                            .setDesignWidthInDp(1920)
                            .setDesignHeightInDp(1080);
                    } else {
                        //设置竖屏设计尺寸
                        AutoSizeConfig.getInstance()
                            .setDesignWidthInDp(1080)
                            .setDesignHeightInDp(1920);
                    }
                }*//*

                AutoSizeConfig.getInstance()
                    .setDesignWidthInDp(1920)
                    .setDesignHeightInDp(1080);
            }

            override fun onAdaptAfter(target: Any?, activity: Activity?) {

            }
        })*/

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