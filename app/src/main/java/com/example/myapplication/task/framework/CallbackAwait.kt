package com.example.myapplication.task.framework

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 通用回调接口，用于把回调式异步统一成 suspend。
 */
interface Callback<T> {
    fun onResult(value: T)
    fun onError(e: Throwable)
}

/**
 * 把「请求在 A 处、回调在 B 处」的回调式异步转成 suspend 函数。
 *
 * 用法（以 EventBus / 观察者 / 跨模块回调为例）：
 * ```
 * val result = awaitCallback<Int>(
 *     register = { cb -> eventBus.register(cb) },
 *     unregister = { cb -> eventBus.unregister(cb) }
 * )
 * ```
 *
 * 外层协程被取消时，会自动调用 [unregister] 反注册，避免悬挂的 continuation 泄漏。
 */
suspend fun <T> awaitCallback(
    register: (Callback<T>) -> Unit,
    unregister: (Callback<T>) -> Unit
): T = suspendCancellableCoroutine { cont ->
    val callback = object : Callback<T> {
        override fun onResult(value: T) {
            if (cont.isActive) cont.resume(value)
        }

        override fun onError(e: Throwable) {
            if (cont.isActive) cont.resumeWithException(e)
        }
    }
    register(callback)
    cont.invokeOnCancellation { unregister(callback) }
}
