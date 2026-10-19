package com.example.myapplication.task

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R

/**
 * 串行任务链取消测试页面
 *
 * - 启动任务链：10 个任务串行执行（奇数异步、偶数同步）
 * - 停止任务链：一次取消整条链，正在执行的任务收到取消信号后退出
 * - 日志实时输出到界面
 */
class TaskChainTestActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, TaskChainTestActivity::class.java))
        }
    }

    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnRestart: Button
    private lateinit var tvLog: TextView
    private lateinit var scrollLog: ScrollView

    private var taskChain: SerialTaskChain? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_chain_test)

        btnStart = findViewById(R.id.btn_start)
        btnStop = findViewById(R.id.btn_stop)
        btnRestart = findViewById(R.id.btn_restart)
        tvLog = findViewById(R.id.tv_log)
        scrollLog = findViewById(R.id.scroll_log)

        taskChain = SerialTaskChain(lifecycleScope) { msg ->
            // 同时打印到 Logcat 和界面，方便排查
            Log.e("SerialTaskChain", msg)
            runOnUiThread { appendLog(msg) }
        }

        btnStart.setOnClickListener { taskChain?.start() }
        btnStop.setOnClickListener { taskChain?.stop() }
        // 触发新任务：先停掉正在跑的任务，再从任务3开始执行
        btnRestart.setOnClickListener {
            taskChain?.start(fromTask = 3)
        }
    }

    private fun appendLog(msg: String) {
        tvLog.append("$msg\n")
        scrollLog.post { scrollLog.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
