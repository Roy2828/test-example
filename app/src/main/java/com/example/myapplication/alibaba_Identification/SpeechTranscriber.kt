package com.example.myapplication.alibaba_Identification

import android.text.TextUtils
import android.util.Log


/**
 *    desc   :
 *    date   : 2025/7/2 10:58
 *    author : Roy
 *    version: 1.0
 */
class SpeechTranscriber private constructor(
    val appKey: String,
    val accessToken: String,
    val accessKey: String,
    val accessKeySecret: String,
    val stsToken: String,
    val serverUrl: String    //服务地址

) {

    companion object {

        fun builder(): Builder {
            return Builder()
        }
    }

    class Builder {
        private var appKey: String = ""
        private var accessToken: String = ""
        private var accessKey: String = ""
        private var accessKeySecret: String = ""
        private var stsToken: String = ""
        private var serverUrl: String = " "

        fun setAppKey(appKey: String): Builder {
            this.appKey = appKey
            return this
        }

        fun setAccessToken(accessToken: String): Builder {
            this.accessToken = accessToken
            return this
        }

        fun setAccessKey(accessKey: String): Builder {
            this.accessKey = accessKey
            return this
        }

        fun setAccessKeySecret(accessKeySecret: String): Builder {
            this.accessKeySecret = accessKeySecret
            return this
        }

        fun setStsToken(stsToken: String): Builder {
            this.stsToken = stsToken
            return this
        }

        fun setServerUrl(serverUrl: String): Builder {

            this.serverUrl = serverUrl
            return this
        }


        fun build(): SpeechTranscriber {
            LogX.e("appKey: $appKey accessToken: $accessToken accessKey: $accessKey accessKeySecret: $accessKeySecret stsToken: $stsToken serverUrl: $serverUrl")
            return SpeechTranscriber(
                appKey,
                accessToken,
                accessKey,
                accessKeySecret,
                stsToken,
                serverUrl
            )
        }


    }


}