package com.example.myapplication.speak

import android.app.Application
import android.content.Context

/**
 *    desc   :
 *    date   : 2025/7/7 16:22
 *    author : Roy
 *    version: 1.0
 */
class SpeakManager private constructor(){


    companion object {
        @Volatile
        private var instance: SpeakManager? = null

        fun getInstance(): SpeakManager {
            return instance ?: synchronized(this) {
                instance ?: SpeakManager().also { instance = it }
            }
        }
    }

    var context: Application? = null

    private val superPersonified:ISuperPersonifiedTts by lazy { SuperPersonifiedTtsImpl()}

    private val sparkChainConfig:ISuperPersonifiedSparkChainConfig by lazy { SuperPersonifiedSparkChainConfig() }


    //初始化需要权限
    fun init(context: Context, appId: String, apiKey: String, apiSecret: String,onInitListener:((result: Boolean)->Unit)? = null){

        if (context is Application) {
            this.context = context
        } else {
            this.context = context.applicationContext as Application
        }

        sparkChainConfig.initSDK(context,appId,apiKey,apiSecret,onInitListener)
    }


    //sdk 取消初始化
    fun unInit(){
        sparkChainConfig.unInit()
    }


    //初始化音频播放线程
    fun initAudioPlayThread(){
        superPersonified.initAudioPlayThread()
    }


    //播放内容 按照顺序播放
    fun startFlowSpeak(mPersonateTTSParams:SuperPersonifiedTtsParams){
        superPersonified.startFlow(mPersonateTTSParams)
    }


    //停止播放
    fun stop(){
        superPersonified.stop()
    }


    //设置播放监听
    fun setAudioPlayListener(onCompleted: () -> Unit, onError: (errCode:Int?,errMsg:String?,sid:String?)->Unit){
        superPersonified.setAudioPlayListener(onCompleted,onError)
    }


    //是否正在播放
    fun  isPlaying(): Boolean {
        return superPersonified.isPlaying()
    }


    fun destroy(){
        superPersonified.onDestroy()
        unInit()
    }


}