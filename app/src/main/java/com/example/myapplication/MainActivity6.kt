package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2024/9/28 13:20
 *    author : Roy
 *    version: 1.0
 */

class MainActivity6 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main6)

        ARouter.getInstance().build(intent.data?.getQueryParameter("url")).navigation()
       // ARouter.getInstance().build("/chat/details").navigation(this)
    }


/*

val uriString = "router://superlink?url=chat/details&param2=value2"

// 将Uri链接字符串转换为Uri对象

// 将Uri链接字符串转换为Uri对象
val uri: Uri = Uri.parse(uriString)

// 创建Intent并传递带参数的Uri

// 创建Intent并传递带参数的Uri
val intent = Intent("router")
intent.data = uri
startActivity(intent)

接收
  class ChatDetailsActivity : Activity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // 获取从上一个页面传递过来的参数
            val uri: Uri? = intent.data
            val url: String? = uri?.getQueryParameter("url")
            val param2: String? = uri?.getQueryParameter("param2")

            // 在这里处理接收到的参数值
            Log.d("ChatDetailsActivity", "URL: $url, Param2: $param2")
        }
    }*/


/*    val uriString = "router://superlink"
    val uri: Uri = Uri.parse(uriString)

    val intent = Intent("router")
    intent.data = uri

// 将参数作为额外数据传递
    intent.putExtra("url", "chat/details")
    intent.putExtra("param2", "value2")

    startActivity(intent)


才能这样接手
class ChatDetailsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取从上一个页面传递过来的参数
        val url: String? = intent.getStringExtra("url")
        val param2: String? = intent.getStringExtra("param2")

        // 在这里处理接收到的参数值
        Log.d("ChatDetailsActivity", "URL: $url, Param2: $param2")
    }
}



    */
}