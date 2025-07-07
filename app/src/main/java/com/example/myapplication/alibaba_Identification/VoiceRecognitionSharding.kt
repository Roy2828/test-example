package com.example.myapplication.alibaba_Identification

/**
 *    desc   :
 *    date   : 2025/7/2 14:27
 *    author : Roy
 *    version: 1.0
 */
class VoiceRecognitionSharding private constructor(
    val fileName:String,
    val isSaveAudioToLocal: Boolean,
    val sampleRateInHz: Int
){


    fun builder():Build{
        return Build()
    }



    class Build{

        private var fileName:String = ""
        private var isSaveAudioToLocal:Boolean = false
        private var sampleRateInHz: Int = 16000 //默认采样率

        fun setFileName(fileName:String):Build{
            this.fileName = fileName
            return this
        }

        fun  setSaveAudioToLocal(isSaveAudioToLocal:Boolean):Build{
            this.isSaveAudioToLocal = isSaveAudioToLocal
            return this
        }

        //设置采样率  16000 、 8000
        fun setSampleRateInHz(sampleRateInHz: Int):Build{
            this.sampleRateInHz = sampleRateInHz
            return this
        }


        fun build():VoiceRecognitionSharding{
            return VoiceRecognitionSharding(fileName,isSaveAudioToLocal,sampleRateInHz)
        }
    }


}