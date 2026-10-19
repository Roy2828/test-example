package com.example.myapplication.task

import android.util.Log
import com.example.myapplication.task.framework.Callback
import com.example.myapplication.task.framework.Task
import com.example.myapplication.task.framework.TaskChain
import com.example.myapplication.task.framework.awaitCallback
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 串行任务链示例：10 个任务依次执行，奇数网络请求（约 60 秒）、偶数同步。
 * 其中任务3 是「请求与回调分离」模式。
 *
 * 基于 [TaskChain] 框架封装，演示三类任务如何统一成一个 [Task]。
 */
class SerialTaskChain(
    private val scope: CoroutineScope,
    private val onLog: (String) -> Unit = { Log.e(TAG, it) }
) {

    companion object {
        private const val TAG = "SerialTaskChain"

        /** 模拟网络请求耗时（秒）。真实场景约 60 秒；测试时可改小以便观察。 */
        private const val NETWORK_REQUEST_SECONDS = 60
    }

    private val chain = TaskChain<Int>(scope, onLog)

    /** 模拟「请求与回调分离」的挂起回调，真实场景即 EventBus/观察者持有的引用 */
    @Volatile
    private var pendingCallback: Callback<Int>? = null

    val isRunning: Boolean
        get() = chain.isRunning

    fun start(fromTask: Int = 1) {
        chain.start(
            fromIndex = fromTask - 1,
            initial = fromTask, // 任务 N 的输入恰好是 N，作为从中间开始时的初始值
            tasks = buildTasks()
        )
    }

    fun stop() = chain.stop()

    private fun buildTasks(): List<Task<Int>> = listOf(
        Task { input -> network("任务1", input) },
        Task { input -> sync("任务2", input) },
        Task { input -> callbackSeparated("任务3", input) },
        Task { input -> sync("任务4", input) },
        Task { input -> network("任务5", input) },
        Task { input -> sync("任务6", input) },
        Task { input -> network("任务7", input) },
        Task { input -> sync("任务8", input) },
        Task { input -> network("任务9", input) },
        Task { input -> sync("任务10", input) }
    )

    /**
     * 协程友好请求：用 withContext(IO) 模拟，父协程取消时自动中断。
     * 对应 Retrofit suspend / OkHttp await() 这类天然可取消的请求。
     */
    private suspend fun network(name: String, input: Int): Int =
        withContext(Dispatchers.IO) {
            log("$name（网络请求）开始，预计 ${NETWORK_REQUEST_SECONDS} 秒")
            try {
                var second = 0
                while (second < NETWORK_REQUEST_SECONDS) {
                    delay(1000)
                    second++
                    log("$name 请求进行中 $second/$NETWORK_REQUEST_SECONDS 秒")
                }
                log("$name（网络请求）完成，返回=${input + 1}")
                input + 1
            } catch (e: CancellationException) {
                log("$name 底层网络请求已中断")
                throw e
            }
        }

    /** 同步 CPU 任务：循环内用 ensureActive() 检查取消。 */
    private suspend fun sync(name: String, input: Int): Int {
        log("$name（同步）开始，输入=$input")
        withContext(Dispatchers.Default) {
            var sum = 0L
            repeat(200_000_000) {
                ensureActive() // 每个迭代检查取消信号，收到即退出
                sum += it
            }
        }
        log("$name（同步）完成，返回=${input + 1}")
        return input + 1
    }

    /** 请求与回调分离：用框架的 awaitCallback 包装，取消时自动反注册。 */
    private suspend fun callbackSeparated(name: String, input: Int): Int =
        awaitCallback(
            register = { cb ->
                log("$name 发起请求（登记等待回调）")
                pendingCallback = cb
                dispatchToRemote(name, input)
            },
            unregister = { cb ->
                log("$name 已取消：清理挂起的请求")
                if (pendingCallback === cb) pendingCallback = null
            }
        )

    /** 回调方：模拟「另一个地方」（另一模块 / 线程 / 事件总线）在 3 秒后回调。 */
    private fun dispatchToRemote(name: String, input: Int) {
        scope.launch(Dispatchers.IO) {
            delay(3000)
            pendingCallback?.let { cb ->
                log("$name 别处回调到达，返回结果")
                cb.onResult(input + 1)
            }
            pendingCallback = null
        }
    }

    private fun log(msg: String) = onLog(msg)
}
