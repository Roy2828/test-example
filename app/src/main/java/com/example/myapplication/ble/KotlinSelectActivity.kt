package com.example.myapplication.ble

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.selects.select

/**
 * Kotlin select 表达式演示 Activity
 *
 * 展示 kotlinx.coroutines.selects.select 的三种核心用法：
 * 1. select + Channel（多通道竞速）
 * 2. select + Deferred（多任务竞速）
 * 3. select + onAwait + onJoin（协程控制）
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class KotlinSelectActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "KotlinSelect"

        fun start(context: Context) {
            context.startActivity(Intent(context, KotlinSelectActivity::class.java))
        }
    }

    private lateinit var tvLog: TextView
    private lateinit var btnChannelSelect: Button
    private lateinit var btnDeferredSelect: Button
    private lateinit var btnChannelAll: Button
    private lateinit var btnDeferredAll: Button
    private lateinit var btnClear: Button

    private val logLines = mutableListOf<String>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_select)

        tvLog = findViewById(R.id.tv_select_log)
        btnChannelSelect = findViewById(R.id.btn_channel_select)
        btnDeferredSelect = findViewById(R.id.btn_deferred_select)
        btnChannelAll = findViewById(R.id.btn_channel_all)
        btnDeferredAll = findViewById(R.id.btn_deferred_all)
        btnClear = findViewById(R.id.btn_clear)

        btnChannelSelect.setOnClickListener { demoChannelSelect() }
        btnDeferredSelect.setOnClickListener { demoDeferredSelect() }
        btnChannelAll.setOnClickListener { demoChannelAll() }
        btnDeferredAll.setOnClickListener { demoDeferredAll() }
        btnClear.setOnClickListener { clearLog() }

        addLog("=== Kotlin select 表达式演示 ===")
        addLog("select 可以从多个挂起通道/任务中，")
        addLog("等待第一个结果返回（类似 Go 的 select）")
        addLog("")
        addLog("点击下方按钮查看不同场景的演示：")
        addLog("  [通道竞速] - 两个通道同时发数据，select取最快")
        addLog("  [协程竞速] - 两个协程同时计算，select取最快")
        addLog("  [通道遍历] - select 逐一读取两个通道")
        addLog("  [协程遍历] - select 逐一读取多个Deferred")
    }

    /**
     * 演示1：select + Channel —— 多通道竞速
     * 两个通道同时发送数据，select 只接收最先到达的那个
     */
    private fun demoChannelSelect() {
        scope.launch {
            addLog("")
            addLog("━━━ 通道竞速 ━━━")
            addLog("两个通道同时发送，select 取最先到达的")

            val channel1 = Channel<String>(Channel.RENDEZVOUS)
            val channel2 = Channel<String>(Channel.RENDEZVOUS)

            // 模拟两个异步数据源
            launch {
                delay(500 + kotlin.random.Random.nextLong() % 1000)
                channel1.send("通道1: 数据A (耗时 ${System.currentTimeMillis() % 1000})")
            }
            launch {
                delay(500 + kotlin.random.Random.nextLong() % 1000)
                channel2.send("通道2: 数据B (耗时 ${System.currentTimeMillis() % 1000})")
            }

            val result = select<String> {
                channel1.onReceive { value ->
                    "← 选中通道1: $value"
                }
                channel2.onReceive { value ->
                    "← 选中通道2: $value"
                }
            }

            addLog("结果: $result")
            addLog("说明: 只收到了最快的那个通道数据")
            addLog("      另一个通道的数据被丢弃了")
        }
    }

    /**
     * 演示2：select + Deferred —— 多协程竞速
     * 多个 async 协程同时计算，select 取最先完成的那个
     */
    private fun demoDeferredSelect() {
        scope.launch {
            addLog("")
            addLog("━━━ 协程竞速 ━━━")
            addLog("两个协程同时计算，select 取最先完成的")

            val deferred1 = async {
                delay(800 + kotlin.random.Random.nextLong() % 1200)
                "协程1: 计算结果 = ${(100..999).random()}"
            }
            val deferred2 = async {
                delay(800 + kotlin.random.Random.nextLong() % 1200)
                "协程2: 计算结果 = ${(100..999).random()}"
            }

            val result = select<String> {
                deferred1.onAwait { value ->
                    "← 选中协程1: $value"
                }
                deferred2.onAwait { value ->
                    "← 选中协程2: $value"
                }
            }

            addLog("结果: $result")
            addLog("说明: 两个协程并发执行，select 取最快返回的")
            addLog("      另一个协程的结果被取消/忽略")
        }
    }

    /**
     * 演示3：select + Channel —— 多通道逐一接收
     * 轮流从两个通道中读取数据，直到所有通道关闭
     */
    private fun demoChannelAll() {
        scope.launch {
            addLog("")
            addLog("━━━ 通道遍历 ━━━")
            addLog("select 循环读取两个通道，直到全部关闭")

            val channel1 = produce {
                for (i in 1..3) {
                    delay(300)
                    send("通道1-数据$i")
                }
            }
            val channel2 = produce {
                for (i in 1..3) {
                    delay(500)
                    send("通道2-数据$i")
                }
            }

            // 用两个 Boolean 标记通道是否关闭
            var c1Closed = false
            var c2Closed = false

            while (!c1Closed || !c2Closed) {
                select<Unit> {
                    if (!c1Closed) {
                        channel1.onReceiveCatching { result ->
                            result.onSuccess { value ->
                                addLog("  ← 通道1: $value")
                            }
                            result.onFailure {
                                c1Closed = true
                                addLog("  通道1 已关闭")
                            }
                        }
                    }
                    if (!c2Closed) {
                        channel2.onReceiveCatching { result ->
                            result.onSuccess { value ->
                                addLog("  ← 通道2: $value")
                            }
                            result.onFailure {
                                c2Closed = true
                                addLog("  通道2 已关闭")
                            }
                        }
                    }
                }
            }

            addLog("说明: select 轮流接收两个通道的数据")
            addLog("      通道按各自节奏发送，互不阻塞")
        }
    }

    /**
     * 演示4：select + Deferred —— 多协程逐一接收
     * 同时启动多个任务，select 逐一获取完成的结果
     */
    private fun demoDeferredAll() {
        scope.launch {
            addLog("")
            addLog("━━━ 协程遍历 ━━━")
            addLog("select 逐一读取多个 Deferred 的结果")

            val deferreds = listOf(
                async {
                    delay(1200)
                    "任务A: 网络请求完成"
                },
                async {
                    delay(800)
                    "任务B: 数据库查询完成"
                },
                async {
                    delay(1500)
                    "任务C: 文件读写完成"
                }
            )

            addLog("启动 3 个异步任务...")

            // select 循环，每次取最快完成的任务
            val remaining = deferreds.toMutableList()
            var index = 0
            while (remaining.isNotEmpty()) {
                val result = select<Pair<Int, String>> {
                    remaining.forEachIndexed { i, deferred ->
                        deferred.onAwait { value ->
                            i to value
                        }
                    }
                }
                val (completedIndex, value) = result
                addLog("  #${++index} 完成: $value")
                remaining.removeAt(completedIndex)
            }

            addLog("说明: 3 个任务并发执行")
            addLog("      select 按完成顺序逐一取出结果")
        }
    }

    private fun addLog(message: String) {
        logLines.add(message)
        if (logLines.size > 100) {
            logLines.removeAt(0)
        }
        tvLog.text = logLines.joinToString("\n")
    }

    private fun clearLog() {
        logLines.clear()
        addLog("日志已清空")
        addLog("=== Kotlin select 表达式演示 ===")
        addLog("点击下方按钮查看不同场景的演示：")
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
