package com.example.myapplication.webp

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.integration.webp.decoder.WebpFrameCacheStrategy
import com.bumptech.glide.integration.webp.decoder.WebpFrameLoader
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.myapplication.R

/**
 * WebP 动图预览页面（全版本统一基于 Glide + webpdecoder）
 */
class WebpTestActivity : AppCompatActivity() {

    companion object {
        // 主网络节点 (Google 官方高可用 WebP 样例)
        private const val TEST_WEBP_URL_PRIMARY =
            "https://mathiasbynens.be/demo/animated-webp-supported.webp"
        // 备用 CDN 节点 (Cloudinary 官方 WebP 样例)
        private const val TEST_WEBP_URL_FALLBACK =
            "https://res.cloudinary.com/demo/image/upload/sample.webp"

        private const val ASSETS_WEBP_PATH =
            "file:///android_asset/ic_ai_speek3.webp"

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

    private var isUrlPlaying = true
    private var isAssetsPlaying = true

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

    // ======================== 加载网络 WebP (带 Header 与节点容灾) ========================

    private fun loadWebpFromUrl(url: String = TEST_WEBP_URL_PRIMARY, isFallback: Boolean = false) {
        tvUrlInfo.text = if (isFallback) "主节点连接超时，尝试备用 CDN..." else "网络 WebP 加载中..."



        val options = RequestOptions()
            .format(DecodeFormat.PREFER_ARGB_8888)  // 支持 WebP 动图透明通道
            .set(WebpFrameLoader.FRAME_CACHE_STRATEGY, WebpFrameCacheStrategy.ALL)

        GlideApp.with(this)
            .load(url)
            .apply(options)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("WebpTestActivity", "网络 WebP 加载失败 ($url): ${e?.message}")
                    if (!isFallback) {
                        // 主节点请求失败/超时，切至备用 CDN 节点
                        // 异步提交到 View 消息队列，避免在 onLoadFailed 回调内部同步调用 Glide.into() 打断旧 Request 释放流程抛出 CallbackException
                        ivWebpUrl.post {
                            if (!isDestroyed && !isFinishing) {
                                loadWebpFromUrl(TEST_WEBP_URL_FALLBACK, true)
                            }
                        }
                    } else {
                        tvUrlInfo.text = "网络 WebP 加载失败: ${e?.message}"
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    tvUrlInfo.text = "加载成功 | 来源: ${if (isFallback) "国内备用 CDN 节点" else "主网络节点"}"
                    return false
                }
            })
            .into(ivWebpUrl)
    }

    // ======================== 加载本地 Assets WebP ========================

    private fun loadWebpFromAssets() {
        tvAssetsInfo.text = "Assets WebP 加载中..."

        val options = RequestOptions()
            .format(DecodeFormat.PREFER_ARGB_8888)
            .set(WebpFrameLoader.FRAME_CACHE_STRATEGY, WebpFrameCacheStrategy.ALL)

        GlideApp.with(this)
            .load(ASSETS_WEBP_PATH)
            .apply(options)
            .listener(createRequestListener(tvAssetsInfo, "Assets WebP", ASSETS_WEBP_PATH))
            .into(ivWebpAssets)
    }

    // ======================== 通用加载监听器 ========================

    private fun createRequestListener(
        infoTextView: TextView,
        tag: String,
        source: String
    ): RequestListener<Drawable> {
        return object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                infoTextView.text = "$tag 加载失败: ${e?.message}"
                Log.e("WebpTestActivity", "$tag 加载失败: ${e?.message}")
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                infoTextView.text = "加载成功 | 来源: $source"
                return false
            }
        }
    }

    // ======================== 按钮操作 ========================

    private fun setupButtons() {
        btnReload.setOnClickListener {
            loadWebpFromUrl()
            loadWebpFromAssets()
            Toast.makeText(this, "已重新加载", Toast.LENGTH_SHORT).show()
        }

        btnStop.setOnClickListener {
            isUrlPlaying = !isUrlPlaying
            isAssetsPlaying = !isAssetsPlaying
            toggleAnimation(ivWebpUrl, isUrlPlaying, "网络")
            toggleAnimation(ivWebpAssets, isAssetsPlaying, "Assets")
            btnStop.text = if (isUrlPlaying) "暂停" else "恢复"
        }
    }

    private fun toggleAnimation(imageView: ImageView, play: Boolean, tag: String) {
        val drawable = imageView.drawable
        when (drawable) {
            is Animatable -> {
                if (play) drawable.start() else drawable.stop()
            }
            else -> {
                Toast.makeText(this, "$tag WebP 暂不支持暂停/恢复", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
