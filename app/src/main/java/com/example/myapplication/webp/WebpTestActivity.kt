package com.example.myapplication.webp

import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.myapplication.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * WebP 动图显示测试页面（全版本 + 低 CPU）
 *
 * 分级策略：
 * - API 28+：系统 ImageDecoder → AnimatedImageDrawable
 *            GPU 硬件渲染，帧合成在 GPU 完成，CPU 接近零开销
 * - API 21-27：Glide 解码器 + RGB_565 + bitmap pool
 *            CPU 软件解码，内存和 CPU 做最大优化
 */
class WebpTestActivity : AppCompatActivity() {

    companion object {
        private const val TEST_WEBP_URL =
            "https://mathiasbynens.be/demo/animated-webp-supported.webp"

        fun start(context: Context) {
            context.startActivity(Intent(context, WebpTestActivity::class.java))
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var ivWebpUrl: ImageView
    private lateinit var ivWebpAssets: ImageView
    private lateinit var tvUrlInfo: TextView
    private lateinit var tvAssetsInfo: TextView
    private lateinit var btnReload: Button
    private lateinit var btnStop: Button

    private var isUrlPlaying = true
    private var isAssetsPlaying = true

    /** 是否使用 GPU 加速路径 */
    private val isHardwareAccelerated = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webp_test)

        initViews()
        loadWebpFromUrl()
        loadWebpFromAssets()
        setupButtons()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun initViews() {
        ivWebpUrl = findViewById(R.id.iv_webp_url)
        ivWebpAssets = findViewById(R.id.iv_webp_assets)
        tvUrlInfo = findViewById(R.id.tv_url_info)
        tvAssetsInfo = findViewById(R.id.tv_assets_info)
        btnReload = findViewById(R.id.btn_reload)
        btnStop = findViewById(R.id.btn_stop)
    }

    // ======================== 网络 WebP ========================

    private fun loadWebpFromUrl() {
        tvUrlInfo.text = "网络 WebP 下载中..."
        scope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    fetchBytesWithSslCheck(TEST_WEBP_URL)
                }
                if (isHardwareAccelerated) {
                    decodeWithHardware(bytes, ivWebpUrl)
                    tvUrlInfo.text = "加载成功 (GPU 硬件解码) | 来源: $TEST_WEBP_URL"
                } else {
                    decodeWithGlide(bytes, ivWebpUrl, tvUrlInfo, "网络 WebP")
                }
            } catch (e: Exception) {
                tvUrlInfo.text = "下载失败: ${e.message}"
                Log.e("WebpTestActivity", "下载网络 WebP 失败：${e.message}")
                Toast.makeText(this@WebpTestActivity, "网络 WebP 下载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 针对 Android 7.0 (API 24) 以下老旧系统的 HTTPS 证书兼容处理
     *
     * 低版本 Android 的系统根证书库过旧，访问现代 HTTPS 网站（如 Let's Encrypt / Google 证书）
     * 会抛出 CertPathValidatorException: Trust anchor for certification path not found。
     */
    private fun fetchBytesWithSslCheck(urlString: String): ByteArray {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        if (connection is HttpsURLConnection && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            try {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAllCerts, SecureRandom())
                connection.sslSocketFactory = sslContext.socketFactory
                connection.hostnameVerifier = HostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                Log.e("WebpTestActivity", "配置 SSL 兼容失败: ${e.message}")
            }
        }
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        return connection.inputStream.use { it.readBytes() }
    }

    // ======================== 本地 Assets WebP ========================

    private fun loadWebpFromAssets() {
        val fileName = "ic_ai_speek3.webp"
        tvAssetsInfo.text = "Assets 读取中..."
        scope.launch {
            val bytes = withContext(Dispatchers.IO) {
                try {
                    assets.open(fileName).use { it.readBytes() }
                } catch (e: Exception) {
                    null
                }
            }
            if (bytes == null) {
                tvAssetsInfo.text = "未找到 assets/$fileName，请放入后再试"
                return@launch
            }

            if (isHardwareAccelerated) {
                decodeWithHardware(bytes, ivWebpAssets)
                tvAssetsInfo.text = "加载成功 (GPU 硬件解码) | 来源: assets/$fileName"
            } else {
                decodeWithGlide(bytes, ivWebpAssets, tvAssetsInfo, "Assets WebP")
            }
        }
    }

    // ======================== 核心：GPU / CPU 双路径解码 ========================

    /**
     * API 28+：GPU 硬件路径
     *
     * AnimatedImageDrawable 将帧数据上传到 GPU 纹理，
     * 帧间切换由 RenderThread 在 GPU 完成，不经过 CPU 合成管线。
     * CPU 仅负责解压 WebP 帧（decode 阶段），播放时 CPU 基本空闲。
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun decodeWithHardware(bytes: ByteArray, imageView: ImageView) {
        val source = ImageDecoder.createSource(ByteBuffer.wrap(bytes))
        val drawable = ImageDecoder.decodeDrawable(source) as AnimatedImageDrawable
        imageView.setImageDrawable(drawable)
        drawable.start()
    }

    /**
     * API 21-27：CPU 软解路径
     *
     * 使用 Glide + webpdecoder 插件解码 WebP 字节数据。
     * 注意：不能使用 .asGif()，因为 WebP 的文件头是 RIFF WEBP 而非 GIF87a/89a。
     */
    private fun decodeWithGlide(
        bytes: ByteArray,
        imageView: ImageView,
        infoTextView: TextView,
        tag: String
    ) {
        val options = RequestOptions()
            .format(DecodeFormat.PREFER_RGB_565)  // 每像素 2 字节，减少 50% 帧数据量
            .useAnimationPool(true)                 // 复用 bitmap，减少 GC / 分配开销

        Glide.with(this)
            .load(bytes)
            .apply(options)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    infoTextView.text = "$tag 解码失败: ${e?.message}"
                    Log.e("WebpTestActivity", "$tag Glide 解码失败: ${e?.message}")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    infoTextView.text = "加载并显示成功 (Glide 解码)"
                    return false
                }
            })
            .into(imageView)
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
