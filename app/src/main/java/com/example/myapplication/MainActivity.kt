package com.example.myapplication

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import com.example.myapplication.examplerecyclerview.RecyclerViewActivity

import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       // Log.e("Roy","hhhhhhhhhhhhhh")

        testServer()
       /*  var retrofit = Retrofit.Builder().baseUrl("http://www.baidu.com")
             .build()


        var myin =  retrofit.create(MyIn::class.java)
        myin.getName("hame").enqueue(object :Callback<String>{
            override fun onResponse(p0: Call<String>, p1: Response<String>) {

            }

            override fun onFailure(p0: Call<String>, p1: Throwable) {

            }

        })*/

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.e("Roy","hhhhhhhhhhhhhh")

  /*      **2、长按HOME键，选择运行其他的程序时。**

        **3、按下电源按键（关闭屏幕显示）时。**

        **4、从activity A中启动一个新的activity时。**

        **5、屏幕方向切换时，例如从竖屏切换到横屏时。***/
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        //只有在activity确实是被系统回收，重新创建activity的情况下才会被调用
      /*  屏幕方向切换时，activity生命周期如下：
        onPause -> onSaveInstanceState -> onStop -> onDestroy -> onCreate -> onStart -> onRestoreInstanceState -> onResume*/
    }



    fun testServer(){
        // 创建两个 Intent 对象
        val serviceIntent1 = Intent(this, MyService::class.java)
        val serviceIntent2 = Intent(this, MyService::class.java)

        // 尝试并发绑定服务
        bindService(serviceIntent1, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // 服务连接成功时的处理
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // 服务连接断开时的处理
            }
        }, Context.BIND_AUTO_CREATE)

        bindService(serviceIntent2, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // 服务连接成功时的处理
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // 服务连接断开时的处理
            }
        }, Context.BIND_AUTO_CREATE)

        //unbindService(ServiceConnection)
    }


    fun okhttp(){
    /*  var build =  OkHttpClient.Builder().build()
        var json = ""
        var body = RequestBody.create( MediaType.get("application/json"),json)
      var request = Request.Builder().addHeader("name","dd")
          .post(body)
          .build()
     var req =  build.newCall(request)
     req.enqueue(object :okhttp3.Callback{
         override fun onFailure(call: okhttp3.Call, e: IOException) {

         }

         override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

         }

     })*/
    }

    fun startActivityMethod(view: View) {
        startActivity(Intent(this,MainActivity2::class.java))
    }

    fun startActivityMethod2(view: View) {

        startActivity(Intent(this,MainActivity3::class.java))
    }

    fun startActivityMethod3(view: View) {
        RecyclerViewActivity.startActivity(this)

    }
}