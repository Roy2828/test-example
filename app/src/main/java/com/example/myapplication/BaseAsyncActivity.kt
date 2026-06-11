package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.asynclayoutinflater.view.AsyncLayoutInflater

/**
 * 支持异步加载布局的基础 Activity
 */
abstract class BaseAsyncActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. 设置基础加载容器
        setContentView(R.layout.activity_base_async_loading)
        
        val container = findViewById<ViewGroup>(R.id.base_async_container)
        val loadingView = findViewById<View>(R.id.base_async_loading)

        // 2. 开始异步加载子类布局
        val asyncLayoutInflater = AsyncLayoutInflater(this)
        asyncLayoutInflater.inflate(getLayoutId(), container) { view, resid, parent ->
            // 隐藏加载状态
            loadingView.visibility = View.GONE
            
            // 将加载好的布局添加到容器
            parent?.addView(view)
            
            // 通知子类布局已加载完成，可以开始初始化视图
            onLayoutInflated(view)
        }
    }

    /**
     * 子类需提供要异步加载的布局 ID
     */
    @LayoutRes
    abstract fun getLayoutId(): Int

    /**
     * 布局加载完成后的回调，子类在此处进行 findViewById 或视图初始化
     */
    abstract fun onLayoutInflated(rootView: View)
}