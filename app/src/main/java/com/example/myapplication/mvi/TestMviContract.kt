package com.example.myapplication.mvi

/**
 * 测试界面的 MVI 协议 - 优化版（符合单一职责原则）
 */
class TestMviContract {

    // --- 子状态定义 ---

    /** 业务内容状态 */
    data class ContentState(
        val isLoading: Boolean = false,
        val data: String = "等待加载..."
    )

    /** 计数器状态 */
    data class CounterState(
        val count: Int = 0
    )

    /** 用户表单状态 */
    data class UserFormState(
        val userName: String = ""
    )

    // --- 总 UI 状态 ---

    data class TestState(
        val content: ContentState = ContentState(),
        val counter: CounterState = CounterState(),
        val userForm: UserFormState = UserFormState()
    ) : IUiState

    // --- 用户意图 ---
    sealed class TestIntent : IUiIntent {
        object FetchData : TestIntent()
        object IncrementCount : TestIntent()
        data class UpdateName(val name: String) : TestIntent()
    }

    // --- UI 副作用 ---
    sealed class TestEffect : IUiEffect {
        data class ShowToast(val message: String) : TestEffect()
    }
}