package com.example.myapplication.uploadnew

import androidx.activity.result.launch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *    desc   :
 *    date   : 2025/8/15 18:28
 *    author : Roy
 *    version: 1.0
 */
class CoroutineDebouncer  (
    private val delayMillis: Long = 300L,
) : CoroutineScope by MainScope(){

    private var debounceJob: Job? = null

    /**
     * 延迟执行回调。如果在延迟时间内再次调用此方法，
     * 先前的回调将被取消，并为新的回调重新计时。
     *
     * @param callback 要延迟执行的挂起函数或普通函数。
     */
    fun delaySend(callback: suspend () -> Unit) {
        debounceJob?.cancel() // 取消上一个正在等待的任务
        debounceJob =  launch {
            delay(delayMillis) // 等待指定的延迟时间
            callback()         // 执行回调
        }
    }

    // 如果回调不是挂起函数
    fun delaySendNonSuspend(callback: () -> Unit) {
        debounceJob?.cancel()
        debounceJob =  launch {
            delay(delayMillis)
            callback()
        }
    }


    /**
     * 取消任何正在等待的回调。
     */
    fun cancel() {
        debounceJob?.cancel()
        debounceJob = null
    }
}