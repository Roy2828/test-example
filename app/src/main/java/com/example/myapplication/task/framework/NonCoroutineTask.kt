package com.example.myapplication.task.framework

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 可取消的句柄接口
 */
fun interface Cancellable {
    fun cancel()
}

/**
 * 非协程任务抽象接口（适合纯 Java 接口或传统 Callback 模式的第三方 SDK 任务）。
 */
interface NonCoroutineTask<T> {
    interface Callback<T> {
        fun onSuccess(result: T)
        fun onError(throwable: Throwable)
    }

    /**
     * 执行非协程任务
     * @param input 输入数据
     * @param callback 结果回调
     * @return 返回可用于取消该任务的 [Cancellable] 句柄（无法取消可返回 null）
     */
    fun execute(input: T, callback: Callback<T>): Cancellable?
}

/**
 * 将 [NonCoroutineTask] 转换为协程驱动的 [Task]
 */
fun <T> NonCoroutineTask<T>.asTask(): Task<T> = Task { input ->
    suspendCancellableCoroutine { cont ->
        val cancellable = execute(
            input,
            object : NonCoroutineTask.Callback<T> {
                override fun onSuccess(result: T) {
                    if (cont.isActive) {
                        cont.resume(result)
                    }
                }

                override fun onError(throwable: Throwable) {
                    if (cont.isActive) {
                        cont.resumeWithException(throwable)
                    }
                }
            }
        )

        cont.invokeOnCancellation {
            cancellable?.cancel()
        }
    }
}
