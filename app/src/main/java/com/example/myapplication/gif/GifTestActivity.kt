package com.example.myapplication.gif

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.net.URL

/**
 * GIF 动图测试页面
 *
 * 使用 android-gif-drawable 库：
 * - 原生 C 层解码 GIF 帧 → 直接渲染到 Canvas，不走 Java Bitmap 解码
 * - 帧解码在后台线程，不阻塞 UI Thread
 * - 支持 GifImageView / GifDrawable 两种用法
 * - API 14+ 全版本支持
 */
class GifTestActivity : AppCompatActivity() {

    companion object {
        /** 示例 GIF（经典测试用） */
        private const val TEST_GIF_URL =
            "https://media.giphy.com/media/v1.Y2lkPTc5MGIxNjExNTY3aTNnMTByNDg1NnJtNnRwczExZnVxem9uN3d5cXQzM3FmazF1eSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/xT0GqssRweIlyzLJNS/giphy.gif"

        fun start(context: Context) {
            context.startActivity(Intent(context, GifTestActivity::class.java))
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var gifUrl: GifImageView
    private lateinit var gifAssets: GifImageView
    private lateinit var tvUrlInfo: TextView
    private lateinit var tvAssetsInfo: TextView
    private lateinit var btnPlay: Button
    private lateinit var btnPause: Button
    private lateinit var btnReload: Button
    private lateinit var btnSpeed: Button
    private lateinit var btnClear: Button

    private var speedIdx = 0
    private val speeds = arrayOf(1f, 2f, 0.5f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_test)

        initViews()
        loadGifFromUrl()
        loadGifFromAssets()
        setupButtons()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        // 释放 GIF 资源
        releaseGif(gifUrl)
        releaseGif(gifAssets)
    }

    private fun initViews() {
        gifUrl = findViewById(R.id.gif_url)
        gifAssets = findViewById(R.id.gif_assets)
        tvUrlInfo = findViewById(R.id.tv_url_info)
        tvAssetsInfo = findViewById(R.id.tv_assets_info)
        btnPlay = findViewById(R.id.btn_play)
        btnPause = findViewById(R.id.btn_pause)
        btnReload = findViewById(R.id.btn_reload)
        btnSpeed = findViewById(R.id.btn_speed)
        btnClear = findViewById(R.id.btn_clear)
    }

    private fun releaseGif(view: GifImageView) {
        (view.drawable as? GifDrawable)?.recycle()
    }

    // ======================== 网络 GIF ========================

    private fun loadGifFromUrl() {
        tvUrlInfo.text = "下载中..."
        scope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    URL(TEST_GIF_URL).openStream().use { it.readBytes() }
                }
                val drawable = GifDrawable(bytes)
                gifUrl.setImageDrawable(drawable)
                tvUrlInfo.text = "加载成功 | ${TEST_GIF_URL.takeLast(25)}"
            } catch (e: Exception) {
                tvUrlInfo.text = "加载失败: ${e.message}"
                Toast.makeText(this@GifTestActivity, "网络 GIF 加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ======================== 本地 Assets GIF ========================

    private fun loadGifFromAssets() {
        val fileName = "ic_ai_speek.gif"
        tvAssetsInfo.text = "加载中..."

        try {
            assets.openFd(fileName).use { fd ->
                val drawable = GifDrawable(fd)
                gifAssets.setImageDrawable(drawable)
            }
            tvAssetsInfo.text = "加载成功 | 来源: assets/$fileName"
        } catch (e: Exception) {
            tvAssetsInfo.text = "未找到 assets/$fileName，请放入后再试"
        }
    }

    // ======================== 按钮操作 ========================

    private fun setupButtons() {
        btnPlay.setOnClickListener {
            gifUrl.drawable?.let { (it as? GifDrawable)?.start() }
            gifAssets.drawable?.let { (it as? GifDrawable)?.start() }
        }

        btnPause.setOnClickListener {
            gifUrl.drawable?.let { (it as? GifDrawable)?.pause() }
            gifAssets.drawable?.let { (it as? GifDrawable)?.pause() }
        }

        btnReload.setOnClickListener {
            releaseGif(gifUrl)
            releaseGif(gifAssets)
            loadGifFromUrl()
            loadGifFromAssets()
            Toast.makeText(this, "已重新加载", Toast.LENGTH_SHORT).show()
        }

        btnSpeed.setOnClickListener {
            speedIdx = (speedIdx + 1) % speeds.size
            val s = speeds[speedIdx]
            (gifUrl.drawable as? GifDrawable)?.setSpeed(s)
            (gifAssets.drawable as? GifDrawable)?.setSpeed(s)
            btnSpeed.text = "${s}x"
            Toast.makeText(this, "速度切换为 ${s}x", Toast.LENGTH_SHORT).show()
        }

        btnClear.setOnClickListener {
            releaseGif(gifUrl)
            releaseGif(gifAssets)
            gifUrl.setImageDrawable(null)
            gifAssets.setImageDrawable(null)
            Toast.makeText(this, "GIF 已释放", Toast.LENGTH_SHORT).show()
        }
    }
}
