package com.example.myapplication.task

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.R
import com.example.myapplication.task.framework.TaskMessage
import com.example.myapplication.task.framework.TaskMessageBus
import com.example.myapplication.task.framework.TaskStatusType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 串行任务链取消与状态模拟测试页面
 */
class TaskChainTestActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, TaskChainTestActivity::class.java))
        }
    }

    private lateinit var tvStatusType: TextView
    private lateinit var tvStatusDetail: TextView
    private lateinit var tvStatusTime: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button
    private lateinit var btnRestart: Button
    private lateinit var tvLog: TextView
    private lateinit var scrollLog: ScrollView

    private var taskChain: SerialTaskChain? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_chain_test)

        tvStatusType = findViewById(R.id.tv_status_type)
        tvStatusDetail = findViewById(R.id.tv_status_detail)
        tvStatusTime = findViewById(R.id.tv_status_time)
        btnStart = findViewById(R.id.btn_start)
        btnPause = findViewById(R.id.btn_pause)
        btnStop = findViewById(R.id.btn_stop)
        btnRestart = findViewById(R.id.btn_restart)
        tvLog = findViewById(R.id.tv_log)
        scrollLog = findViewById(R.id.scroll_log)

        taskChain = SerialTaskChain(lifecycleScope) { msg ->
            Log.e("SerialTaskChain", msg)
            runOnUiThread { appendLog(msg) }
        }

        btnStart.setOnClickListener {
            taskChain?.start()
            updatePauseButtonText()
        }

        btnPause.setOnClickListener {
            taskChain?.let { chain ->
                if (chain.isPaused) {
                    chain.resume()
                } else {
                    chain.pause()
                }
                updatePauseButtonText()
            }
        }

        btnStop.setOnClickListener {
            taskChain?.stop()
            updatePauseButtonText()
        }

        btnRestart.setOnClickListener {
            taskChain?.start(fromTask = 3)
            updatePauseButtonText()
        }

        observeTaskMessages()
    }

    private fun observeTaskMessages() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                TaskMessageBus.messageFlow.collect { message: TaskMessage ->
                    tvStatusType.text = message.status.label
                    tvStatusDetail.text = message.detail
                    tvStatusTime.text = timeFormat.format(Date(message.timestamp))

                    // 根据消息状态匹配显著颜色
                    when (message.status) {
                        TaskStatusType.START -> {
                            tvStatusType.setBackgroundColor(Color.parseColor("#2196F3")) // 蓝色
                        }
                        TaskStatusType.RUNNING -> {
                            tvStatusType.setBackgroundColor(Color.parseColor("#4CAF50")) // 绿色
                        }
                        TaskStatusType.WAITING -> {
                            tvStatusType.setBackgroundColor(Color.parseColor("#FF9800")) // 橙色
                        }
                        TaskStatusType.PAUSED -> {
                            tvStatusType.setBackgroundColor(Color.parseColor("#9C27B0")) // 紫色
                        }
                        TaskStatusType.STOPPED -> {
                            tvStatusType.setBackgroundColor(Color.parseColor("#F44336")) // 红色（中途终止）
                        }
                        TaskStatusType.ENDED -> {
                            tvStatusType.setBackgroundColor(Color.parseColor("#009688")) // 青色
                        }
                        TaskStatusType.IDLE -> {
                            tvStatusType.setBackgroundColor(Color.parseColor("#999999")) // 灰色
                        }
                    }
                    updatePauseButtonText()
                }
            }
        }
    }

    private fun updatePauseButtonText() {
        val isPaused = taskChain?.isPaused == true
        btnPause.text = if (isPaused) "恢复" else "暂停"
    }

    private fun appendLog(msg: String) {
        tvLog.append("$msg\n")
        scrollLog.post { scrollLog.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
