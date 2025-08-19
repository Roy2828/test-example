package com.example.myapplication.speak

import android.content.Context
import com.example.myapplication.alibaba_Identification.LogX
import com.iflytek.sparkchain.core.SparkChain
import com.iflytek.sparkchain.core.SparkChainConfig

/**
 *    desc   :
 *    date   : 2025/7/7 18:21
 *    author : Roy
 *    version: 1.0
 */
class SuperPersonifiedSparkChainConfig :ISuperPersonifiedSparkChainConfig{

    private var isInit = false

    //初始化需要权限
    // "android.permission.WRITE_EXTERNAL_STORAGE"
    // , "android.permission.READ_EXTERNAL_STORAGE"
    // , "android.permission.INTERNET"

    override fun initSDK(context: Context, appId: String, apiKey: String, apiSecret: String,onInitListener:((result: Boolean)->Unit)?) {
        if(isInit){
            LogX.e("超拟人语音合成 SDK 已经初始化")
            onInitListener?.invoke(true)
            return
        }
        LogX.d("超拟人语音合成 initSDK")
        // 初始化SDK，Appid等信息在清单中配置
        val sparkChainConfig = SparkChainConfig.builder()
        sparkChainConfig.appID(appId)
            .apiKey(apiKey)
            .apiSecret(apiSecret) //应用申请的appid三元组
            //                .uid(getAndroidId())
            //                .logPath("/sdcard/iflytek/AEELog.txt")
            .logLevel(666)

        val ret = SparkChain.getInst().init(context.applicationContext, sparkChainConfig)
        val result: String
        if (ret == 0) {
            result = "SDK初始化成功,请选择相应的功能点击体验。"
            isInit = true
            onInitListener?.invoke(true)
        } else {
            result = "SDK初始化失败,错误码:$ret"
            isInit = false
            onInitListener?.invoke(false)
        }
        LogX.d(result)
    }


    override fun  unInit(){
        isInit = false
        SparkChain.getInst().unInit()
    }
}