package com.example.myapplication.speak

import android.content.Context

/**
 *    desc   :
 *    date   : 2025/7/7 18:22
 *    author : Roy
 *    version: 1.0
 */
interface ISuperPersonifiedSparkChainConfig {
    fun initSDK(context: Context, appId: String, apiKey: String, apiSecret: String,onInitListener:((result: Boolean)->Unit)? = null)
    fun  unInit()
}