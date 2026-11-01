package com.example.myapplication.webp

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

/**
 * WebP 动图预览页面（演示使用封装的 WebpLoader 加载与控制）
 */
class WebpTestActivity : AppCompatActivity() {

    companion object {
        private const val TEST_WEBP_URL_PRIMARY =
            "https://mathiasbynens.be/demo/animated-webp-supported.webp"
        private const val TEST_WEBP_URL_FALLBACK =
            "https://res.cloudinary.com/demo/image/upload/sample.webp"

        private const val ASSETS_WEBP_PATH = "ic_ai_speek3.webp"

        fun start(context: Context) {
            context.startActivity(Intent(context, WebpTestActivity::class.java))
        }
    }

    private lateinit var ivWebpUrl: ImageView
    private lateinit var ivWebpAssets: ImageView
    private lateinit var tvUrlInfo: TextView
    private lateinit var tvAssetsInfo: TextView
    private lateinit var btnReload: Button
    private lateinit var btnStop: Button

    private var isPlaying = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 允许 StrictMode 放行主线程 IO
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        setContentView(R.layout.activity_webp_test)

        initViews()
        loadWebpFromUrl()
        loadWebpFromAssets()
        setupButtons()
    }

    private fun initViews() {
        ivWebpUrl = findViewById(R.id.iv_webp_url)
        ivWebpAssets = findViewById(R.id.iv_webp_assets)
        tvUrlInfo = findViewById(R.id.tv_url_info)
        tvAssetsInfo = findViewById(R.id.tv_assets_info)
        btnReload = findViewById(R.id.btn_reload)
        btnStop = findViewById(R.id.btn_stop)
    }

    // ======================== 使用 WebpLoader 加载网络 WebP ========================

    private fun loadWebpFromUrl() {
        tvUrlInfo.text = "网络 WebP 加载中..."

        WebpLoader.loadWithFallback(
            imageView = ivWebpUrl,
            primaryUrl = TEST_WEBP_URL_PRIMARY,
            fallbackUrl = TEST_WEBP_URL_FALLBACK,
            callback = object : WebpLoader.Callback {
                override fun onSuccess(drawable: Drawable, isFallback: Boolean) {
                    tvUrlInfo.text = "加载成功 | 来源: ${if (isFallback) "备用 CDN 节点" else "主网络节点"}"
                }

                override fun onError(throwable: Throwable?, isFallback: Boolean) {
                    tvUrlInfo.text = "网络 WebP 加载失败: ${throwable?.message}"
                    Log.e("WebpTestActivity", "网络 WebP 加载失败: ${throwable?.message}")
                }
            }
        )
    }

    // ======================== 使用 WebpLoader 加载 Assets WebP ========================

    private fun loadWebpFromAssets() {
        tvAssetsInfo.text = "Assets WebP 加载中..."

        WebpLoader.loadAssets(
            imageView = ivWebpAssets,
            assetFileName = ASSETS_WEBP_PATH,
            callback = object : WebpLoader.Callback {
                override fun onSuccess(drawable: Drawable, isFallback: Boolean) {
                    tvAssetsInfo.text = "加载成功 | 来源: Assets 静态/动图"
                }

                override fun onError(throwable: Throwable?, isFallback: Boolean) {
                    tvAssetsInfo.text = "Assets WebP 加载失败: ${throwable?.message}"
                }
            }
        )
    }

    // ======================== 按钮交互与动画控制 ========================

    private fun setupButtons() {
        btnReload.setOnClickListener {
            loadWebpFromUrl()
            loadWebpFromAssets()
            Toast.makeText(this, "已重新加载", Toast.LENGTH_SHORT).show()
        }

        btnStop.setOnClickListener {
            isPlaying = !isPlaying
            if (isPlaying) {
                WebpLoader.startAnimation(ivWebpUrl)
                WebpLoader.startAnimation(ivWebpAssets)
                btnStop.text = "暂停"
            } else {
                WebpLoader.stopAnimation(ivWebpUrl)
                WebpLoader.stopAnimation(ivWebpAssets)
                btnStop.text = "恢复"
            }
        }
    }
}
