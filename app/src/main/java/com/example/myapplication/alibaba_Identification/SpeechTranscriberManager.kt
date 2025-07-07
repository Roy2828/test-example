package com.example.myapplication.alibaba_Identification

import android.app.Application
import android.content.Context
import android.os.Environment

/**
 *    desc   :
 *    date   : 2025/7/2 13:36
 *    author : Roy
 *    version: 1.0
 */
class SpeechTranscriberManager {


    companion object{
        @Volatile
        private var instance: SpeechTranscriberManager? = null

        fun getInstance(): SpeechTranscriberManager {
            return instance ?: synchronized(this) {
                instance ?: SpeechTranscriberManager().also { instance = it }
            }
        }
    }


   private var speechTranscriber: SpeechTranscriber? = null

   val speechTranscriberLazy get() = speechTranscriber ?: throw IllegalStateException("SpeechTranscriber is not set. Please call setSpeechTranscriber() first.")

    var context: Application? = null
   private var filePath:String = ""

    fun init(context: Context?){
        if(context == null) {
            throw IllegalArgumentException("Context cannot be null")
        }
        if(this.context != null) {
            return // 已经初始化过了
        }
        if (context is Application) {
            this.context = context
        } else {
            this.context = context.applicationContext as Application
        }

        context.apply {
            val state = Environment.getExternalStorageState()
            if (Environment.MEDIA_MOUNTED == state) {
                val baseDirFile = getExternalFilesDir(null)
                if (baseDirFile == null) {
                    filePath = filesDir.path
                } else {
                    filePath = baseDirFile.path
                }
            } else {
                filePath = filesDir.path
            }
        }
    }

    //获取文件路径
    fun getFilePath():String{
        return filePath
    }


    //配置key等参数
    fun setSpeechTranscriber(speechTranscriber: SpeechTranscriber?) {
        this.speechTranscriber = speechTranscriber
    }


    fun start(){
        if(context == null){
            throw Exception("Context is not set. Please call init() first.")
        }
        if(speechTranscriber == null){
           throw Exception("SpeechTranscriber is not set. Please call setSpeechTranscriber() first.")
        }


    }



}