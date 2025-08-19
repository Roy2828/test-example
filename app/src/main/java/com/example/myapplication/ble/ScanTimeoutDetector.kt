package com.example.clife_gait

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

class ScanTimeoutDetector  {

    private val timeoutMillis: Long = 10_000L

    private val lastActiveTime = AtomicLong(System.currentTimeMillis())

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private var job:Job?=null

    var onTimeout: (() -> Unit)? = null


    fun onDataReceived() {
        lastActiveTime.set(System.currentTimeMillis())
    }

    fun startMonitoring() {
        if(job!=null && job!!.isActive){
            Log.e("Bluetooth", "检测中-协程存活中")
            return
        }
        job  =   scope.launch {
            try {
                while (true) {
                    val inactiveTime = System.currentTimeMillis() - lastActiveTime.get()
                    if (inactiveTime > timeoutMillis) { // 大于10秒
                        Log.e("Bluetooth", "扫描已经停止了")
                        onTimeout?.invoke()
                        onDataReceived()
                    }
                    delay(2000) // 2秒检测一次
                    Log.e("Bluetooth", "检测中")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun destroy() {
        scope.cancel()
    }
}