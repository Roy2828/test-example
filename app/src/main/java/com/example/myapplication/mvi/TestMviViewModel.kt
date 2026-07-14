package com.example.myapplication.mvi

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 测试界面的 ViewModel 实现 - 优化版
 */
class TestMviViewModel : BaseMviViewModel<TestMviContract.TestState, TestMviContract.TestIntent, TestMviContract.TestEffect>() {

    override fun createInitialState(): TestMviContract.TestState {
        return TestMviContract.TestState()
    }

    override fun handleIntent(intent: TestMviContract.TestIntent) {
        when (intent) {
            is TestMviContract.TestIntent.FetchData -> fetchData()
            is TestMviContract.TestIntent.IncrementCount -> incrementCount()
            is TestMviContract.TestIntent.UpdateName -> updateName(intent.name)
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            // 1. 设置加载状态
            setState { 
                copy(content = content.copy(isLoading = true, data = "正在从网络加载数据...")) 
            }
            
            // 2. 模拟网络耗时
            delay(2000)
            
            // 3. 更新成功状态
            setState { 
                copy(content = content.copy(isLoading = false, data = "数据加载成功：MVI 架构运行良好！")) 
            }
            
            // 4. 发送一个副作用事件
            setEffect { TestMviContract.TestEffect.ShowToast("数据加载完成！") }
        }
    }

    private fun incrementCount() {
        setState { 
            copy(counter = counter.copy(count = counter.count + 1)) 
        }
    }

    private fun updateName(name: String) {
        setState { 
            copy(userForm = userForm.copy(userName = name)) 
        }
    }
}