package com.example.myapplication.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R


/**
 *    desc   :
 *    date   : 2025/7/12 15:22
 *    author : Roy
 *    version: 1.0
 */
class WebViewActivity: AppCompatActivity() {

    var webiew:WebView?=null

    companion object{

        fun doIntent(context: Context){
            context.startActivity(Intent(context,WebViewActivity::class.java))
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview_activity)


        webiew = MyWebView(this.applicationContext)

        val container = findViewById<FrameLayout>(R.id.webContainer)

        container.addView(webiew)
        container.setOutlineProvider(object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 16f)
            }
        })
        container.setClipToOutline(true)


        webiew?.settings?.apply {
            // 解决WebView不能加载http与https混合内容的问题
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.LOAD_NORMAL
            }
            /***************************************基本配置***************************************/
            // 设置编码格式
            defaultTextEncodingName = "utf-8"

            // 启用JavaScript
            javaScriptEnabled = true

            /***************************************显示相关***************************************/
            // 是否阻止加载网络图片
            blockNetworkImage = false
            // 是否自动加载图片
            loadsImagesAutomatically = true
            // 自动打开窗口
            javaScriptCanOpenWindowsAutomatically = true
            // 禁用多窗口，网页中包含链接或javaScript代码打开新窗口时，新窗口将无法显示在webView中，会尝试启动系统浏览器或其他应用来打开新窗口。
            // [一些网页可能依赖于打开新窗口来展示内容，不能打开]
            setSupportMultipleWindows(false)
            // 设置字体不跟随系统字体大小变化
            textZoom = 100
            mediaPlaybackRequiresUserGesture = false

            /***************************************屏幕缩放***************************************/
            // 是否可以缩放
            setSupportZoom(true)
            // 是否启用内置缩放控件
            builtInZoomControls = false
            // 是否显示缩放按钮
            displayZoomControls = false
            // 自适应屏幕-自动扩展以适应屏幕的宽度
            useWideViewPort = true
            // 自适应屏幕-自动缩小以适应屏幕的宽度
            loadWithOverviewMode = true


            // 设置启用DOM存储
            domStorageEnabled = true
            // 设置启用数据库支持
            databaseEnabled = true

            /***************************************安全相关***************************************/
            // 禁止webView访问本地文件
            allowFileAccess = false
        }

          webiew?.loadUrl("https://www.baidu.com")



    }




    override fun onDestroy() {
        Log.e("Roy","WebViewActivity onDestroy"  )
        webiew?.loadUrl("about:blank")
        webiew?.parent?.let {
            (it as ViewGroup).removeView(webiew)
        }
        webiew?.stopLoading()
        webiew?.settings?.javaScriptEnabled = false
        webiew?.clearHistory()
        webiew?.clearCache(true)
        webiew?.removeAllViewsInLayout()
        webiew?.removeAllViews()
        webiew?.webChromeClient = null
        webiew?.destroy()
        webiew?.apply {
            WebViewReflectionUtils.resetWebViewDestroyedFlag(this)
        }

        webiew = null

        super.onDestroy()


    }
}