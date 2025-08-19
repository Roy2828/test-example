package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson.JSONObject
import com.example.myapplication.view.FacePositionView
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.HttpHeaders
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Response
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit


/**
 *    desc   :
 *    date   : 2025/7/30 15:34
 *    author : Roy
 *    version: 1.0
 */
class TestBotActivity : AppCompatActivity() {


    companion object{
        fun doIntent(context: Context){
            context.startActivity(Intent(context, TestBotActivity::class.java))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_activity)

        val builder = OkHttpClient.Builder()
            .readTimeout(10000, TimeUnit.MILLISECONDS) // 读取超时
            .writeTimeout(10000, TimeUnit.MILLISECONDS) // 写入超时


        // 添加公共请求头/参数（可选）
        val headers: HttpHeaders = HttpHeaders()
        headers.put("App-Version", BuildConfig.VERSION_NAME)
        headers.put("Content-Type", "application/json")
        val params: HttpParams = HttpParams()
        params.put("platform", "android")

        OkGo.getInstance()
            .init(this.application)
            .setOkHttpClient(builder.build())
            .addCommonHeaders(headers) // 全局公共头 [3](@ref)
            .addCommonParams(params) // 全局公共参数


    }




    @SuppressLint("SuspiciousIndentation")
    fun sendMessage(view:View){
      var  mFaceBitmap = BitmapFactory.decodeStream(assets.open("face3.png"))
        mFaceBitmap?.let {
           var ss = findViewById<FacePositionView>(R.id.facePositionView).setImageResourceFitCenter(it)
        }
    }



    fun sendMessage2(view: View) {

        // 1. 构建 JSON 对象
        val json: JSONObject = JSONObject()
        json.put("msgtype", "text")
        val jsonChild = JSONObject()
        jsonChild.put("content", "广州今日天气：29度，大部分多云，降雨概率：60%")
        jsonChild.put("mentioned_list", listOf("张三", "@All")) // 指定接收人
        json.put("text", jsonChild)



// 2. 发起 JSON POST 请求
        OkGo.post<String>("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=ce9143c9-7a19-44a7-a143-5214d47014bb")
            .upJson(json.toString()) // 直接上传 JSON 字符串 [3](@ref)
            .execute(object : StringCallback(){
                override fun onSuccess(response: Response<String>?) {
                    val task   = response?.body() // 自动反序列化
                    Log.d("TASK_ID", "Created task ID: " + task)
                }

                override fun onError(response: Response<String>?) {
                    super.onError(response)
                    Log.e("TAG", "Error: ${response?.message()}")
                }
            })
    }
}