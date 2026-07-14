package com.example.myapplication.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * MVI 基础 ViewModel
 */
abstract class BaseMviViewModel<State : IUiState, Intent : IUiIntent, Effect : IUiEffect> : ViewModel() {

    /**
     * 初始状态，由子类实现
     */
    abstract fun createInitialState(): State

    /**
     * UI 状态流，持有当前界面的唯一状态
     */
    private val _uiState: MutableStateFlow<State> by lazy { MutableStateFlow(createInitialState()) }
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    /**
     * 意图流，用于接收用户操作
     */
    private val _intent: MutableSharedFlow<Intent> = MutableSharedFlow()

    /**
     * 副作用流，用于发送一次性事件
     */
    private val _effect: Channel<Effect> = Channel()
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    init {
        // 订阅意图流并处理
        viewModelScope.launch {
            _intent.collect {
                handleIntent(it)
            }
        }
    }

    /**
     * 发送意图
     */
    fun sendIntent(intent: Intent) {
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }

    /**
     * 处理意图的具体逻辑，由子类实现
     */
    protected abstract fun handleIntent(intent: Intent)

    /**
     * 更新状态
     */
    protected fun setState(reduce: State.() -> State) {
        val newState = _uiState.value.reduce()
        _uiState.value = newState
    }

    /**
     * 发送副作用
     */
    protected fun setEffect(builder: () -> Effect) {
        val newEffect = builder()
        viewModelScope.launch {
            _effect.send(newEffect)
        }
    }
}