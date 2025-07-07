package com.example.myapplication.alibaba_Identification

/**
 *    desc   :
 *    date   : 2025/7/2 11:25
 *    author : Roy
 *    version: 1.0
 */
object LogX {

    private const val TAG = "LogX"

    fun d(message: String) {
        android.util.Log.d(TAG, message)
    }

    fun e(message: String) {
        android.util.Log.e(TAG, message)
    }

    fun i(message: String) {
        android.util.Log.i(TAG, message)
    }

    fun w(message: String) {
        android.util.Log.w(TAG, message)
    }

    fun v(message: String) {
        android.util.Log.v(TAG, message)
    }
}