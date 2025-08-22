package com.example.myapplication.uploadnew

import android.annotation.SuppressLint
import android.content.Context

/**
 *    desc   :
 *    date   : 2025/8/20 09:56
 *    author : Roy
 *    version: 1.0
 */
class UploadInit {


    companion object{
        @Volatile
        private var instance: UploadInit? = null

        fun getInstance(): UploadInit {
            return instance ?: synchronized(this) {
                instance ?: UploadInit().also { instance = it }
            }
        }
    }

    var uploadSharding:UploadSharding? = null

    fun init(uploadSharding: UploadSharding) {
      this.uploadSharding =   uploadSharding
    }


}