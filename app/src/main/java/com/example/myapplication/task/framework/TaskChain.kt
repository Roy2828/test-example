package com.example.myapplication.task.framework

import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 任务抽象：链式任务，接收输入 [T]，处理后返回新的 [T]。
 */
fun interface Task<T> {
    suspend fun run(input: T): T
}

/**
 * 串行任务链执行器。
 *
 * @param scope 承载任务链的协程作用域（如 lifecycleScope）
 * @param onLog 日志回调，输出任务链级日志（启动 / 取消 / 完成）
 */
class TaskChain<T>(
    private val scope: CoroutineScope,
    private val onLog: (String) -> Unit = {}
) {

    private var job: Job? = null
    private var chainId = 0

    @Volatile
    var isPaused: Boolean = false
        private set

    private val pauseGate = AtomicReference<CompletableDeferred<Unit>?>(null)

    val isRunning: Boolean
        get() = job?.isActive == true

    /**
     * 检查并响应暂停
     */
    suspend fun checkPaused() {
        pauseGate.get()?.await()
    }

    /**
     * 启动任务链。
     */
    fun start(fromIndex: Int = 0, initial: T, tasks: List<Task<T>>) {
        if (tasks.isEmpty()) return
        TaskMessageBus.isPausedFilter = false
        // 重新启动前如果处于暂停状态，解开挂起关卡
        resume()
        job?.cancel()
        val safeFrom = fromIndex.coerceIn(0, tasks.lastIndex)
        val id = ++chainId
        onLog("[#$id] 任务链启动（从第${safeFrom + 1}个任务开始，共${tasks.size}个）")
        TaskMessageBus.postMessage(
            TaskStatusType.START,
            "任务链启动（从第${safeFrom + 1}个任务开始，共${tasks.size}个）"
        )

        job = scope.launch {
            try {
                var result = initial
                for (i in safeFrom until tasks.size) {
                    checkPaused()
                    result = tasks[i].run(result)
                }
                onLog("[#$id] 全部任务完成，结果=$result")
                TaskMessageBus.postMessage(
                    TaskStatusType.ENDED,
                    "全部任务正常完成，最终结果=$result"
                )
            } catch (e: CancellationException) {
                onLog("[#$id] 任务链已取消/中途终止")
                TaskMessageBus.postMessage(
                    TaskStatusType.STOPPED,
                    "任务在中途被终止！"
                )
                throw e
            }
        }
    }

    /** 暂停任务链 */
    fun pause() {
        if (!isPaused && isRunning) {
            isPaused = true
            TaskMessageBus.isPausedFilter = true
            pauseGate.set(CompletableDeferred())
            onLog(">>> 任务链已暂停")
            TaskMessageBus.postMessage(TaskStatusType.PAUSED, "任务链已暂停")
        }
    }

    /** 恢复运行任务链 */
    fun resume() {
        if (isPaused) {
            isPaused = false
            TaskMessageBus.isPausedFilter = false
            pauseGate.getAndSet(null)?.complete(Unit)
            onLog(">>> 任务链恢复运行")
            TaskMessageBus.postMessage(TaskStatusType.RUNNING, "任务链恢复运行")
        }
    }

    /** 停止正在跑的任务链。 */
    fun stop() {
        onLog(">>> 收到停止信号")
        TaskMessageBus.isPausedFilter = false
        if (isPaused) {
            isPaused = false
            pauseGate.getAndSet(null)?.complete(Unit)
        }
        job?.cancel()
    }
}
