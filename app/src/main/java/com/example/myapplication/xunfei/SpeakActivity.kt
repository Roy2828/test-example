package com.example.myapplication.xunfei

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.myapplication.R


/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2025/7/5 15:49
 *    author : Roy
 *    version: 1.0
 */
class SpeakActivity:AppCompatActivity() {

    var speak:Speak?=null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.speak_activity)
        speak = Speak.instance
        speak!!.init(this)
        speak?.createSynthesizer(this)

        findViewById<TextView>(R.id.tv_start).setOnClickListener {
            start()
        }

        findViewById<TextView>(R.id.tv_end).setOnClickListener {
            endMethod()
        }



    }

    fun start() {
        speak?.startSpeaking()
    }


    companion object {
        fun startActivity(context: Context) {
            context.startActivity(
                Intent(context, SpeakActivity::class.java)
            )
        }
    }

    fun endMethod() {

        speak?.stopSpeaking()
    }


    //1. 语音 pcm格式本地播放
    //2. 协程并发处理语音合成
    //3. 语音合成失败处理机制
    //4. 动态添加语音合成任务
    //5. 协程实现顺序播放
    //6.编写flutter插件通道
    //7.结合项目中的音频播放器实现语音合成

}