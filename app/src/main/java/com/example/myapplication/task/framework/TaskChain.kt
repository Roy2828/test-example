package com.example.myapplication.task.framework

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 任务抽象：链式任务，接收输入 [T]，处理后返回新的 [T]。
 *
 * 同步任务、异步请求、请求回调分离，都统一实现为该接口：
 * - 同步 CPU 任务：内部用 withContext(Dispatchers.Default) + ensureActive()
 * - 协程友好请求：直接挂起调用（Retrofit suspend / delay 等），自动响应取消
 * - 请求回调分离：用 [awaitCallback] 包装
 */
fun interface Task<T> {
    suspend fun run(input: T): T
}

/**
 * 串行任务链执行器。
 *
 * 规则：
 * - 串行：前一个任务完成后才执行下一个。
 * - 抢占：再次调用 [start] 会先取消正在跑的任务，再执行新任务。
 * - 指定起始：[start] 的 fromIndex 指定从第几个任务开始。
 * - 取消：基于结构化并发；任务内部需配合检查点（ensureActive / delay / awaitCallback）
 *   才能及时响应取消。
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

    val isRunning: Boolean
        get() = job?.isActive == true

    /**
     * 启动任务链。
     *
     * @param fromIndex 从第几个任务开始（0-based）
     * @param initial   第一个被执行任务的输入（模拟前序任务已完成的结果）
     * @param tasks     任务列表，按顺序串行执行
     */
    fun start(fromIndex: Int = 0, initial: T, tasks: List<Task<T>>) {
        if (tasks.isEmpty()) return
        job?.cancel()
        val safeFrom = fromIndex.coerceIn(0, tasks.lastIndex)
        val id = ++chainId
        onLog("[#$id] 任务链启动（从第${safeFrom + 1}个任务开始，共${tasks.size}个）")
        job = scope.launch {
            try {
                var result = initial
                for (i in safeFrom until tasks.size) {
                    result = tasks[i].run(result)
                }
                onLog("[#$id] 全部任务完成，结果=$result")
            } catch (e: CancellationException) {
                onLog("[#$id] 任务链已取消")
                throw e
            }
        }
    }

    /** 停止正在跑的任务链。 */
    fun stop() {
        onLog(">>> 收到停止信号")
        job?.cancel()
    }
}
