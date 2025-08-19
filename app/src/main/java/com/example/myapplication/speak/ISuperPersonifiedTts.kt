package com.example.myapplication.speak

/**
 *    desc   :
 *    date   : 2025/7/7 16:52
 *    author : Roy
 *    version: 1.0
 */


interface AudioPlayListener{
    fun setAudioPlayListener(onCompleted: () -> Unit, onError: (errCode:Int?,errMsg:String?,sid:String?)->Unit)
}


interface ISuperPersonifiedTts :AudioPlayListener{

    fun initAudioPlayThread()

    fun startFlow(mPersonateTTSParams:SuperPersonifiedTtsParams)

    fun stop()


    fun isPlaying(): Boolean



    fun onDestroy()
}