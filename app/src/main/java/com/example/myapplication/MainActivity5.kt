package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION_CODES.R
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2024/9/28 13:12
 *    author : Roy
 *    version: 1.0
 */
class MainActivity5 :AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main5)
    }

    fun test(view: View) {

      //  val uriString = "router://superlink?url=/chat/details"
        val uriString = "router://superlink?url=chat/detail?groupId=5754&groupName=11"

// 将Uri链接字符串转换为Uri对象

// 将Uri链接字符串转换为Uri对象
        val uri: Uri = Uri.parse(uriString)

// 创建Intent并传递带参数的Uri

// 创建Intent并传递带参数的Uri
        val intent = Intent("router")
        intent.data = uri
        startActivity(intent)
    }
}