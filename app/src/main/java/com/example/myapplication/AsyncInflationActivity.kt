package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView

class AsyncInflationActivity : BaseAsyncActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AsyncInflationActivity::class.java)
            context.startActivity(intent)
        }
    }

    // 只需要指定布局 ID
    override fun getLayoutId(): Int = R.layout.layout_async_content

    // 布局加载完成后会自动回调此方法
    override fun onLayoutInflated(rootView: View) {
        // 在这里进行业务逻辑处理
        val titleTv = rootView.findViewById<TextView>(android.R.id.text1) // 假设布局里有
        // ... 其他初始化代码
    }
}