package com.example.myapplication.task.framework

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 任务状态枚举
 */
enum class TaskStatusType(val label: String) {
    IDLE("未开始"),
    START("模拟开始"),
    RUNNING("进行中"),
    WAITING("等待中"),
    PAUSED("暂停"),
    STOPPED("中途终止"),
    ENDED("正常结束")
}

/**
 * 全局任务消息数据载体
 */
data class TaskMessage(
    val status: TaskStatusType,
    val detail: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 全局任务消息总线（使用 StateFlow 进行状态模拟与全局订阅）
 */
object TaskMessageBus {

    private val _messageFlow = MutableStateFlow(TaskMessage(TaskStatusType.IDLE, "等待操作"))
    val messageFlow: StateFlow<TaskMessage> = _messageFlow.asStateFlow()

    @Volatile
    var isPausedFilter: Boolean = false

    fun postMessage(status: TaskStatusType, detail: String = "") {
        if (isPausedFilter && (status == TaskStatusType.RUNNING || status == TaskStatusType.WAITING)) {
            // 处于暂停状态时，拦截可能由于延迟到达的 RUNNING / WAITING 进度消息，避免覆盖 PAUSED 状态
            return
        }
        _messageFlow.value = TaskMessage(status, detail)
    }

    fun reset() {
        isPausedFilter = false
        _messageFlow.value = TaskMessage(TaskStatusType.IDLE, "等待操作")
    }
}
