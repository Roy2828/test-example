package com.example.myapplication.lottie

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.RenderMode
import com.example.myapplication.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class LottieTestActivity : AppCompatActivity() {

    companion object {
        private const val TEST_LOTTIE_URL =
            "https://assets2.lottiefiles.com/packages/lf20_xycw2nvz.json"

        fun start(context: Context) {
            context.startActivity(Intent(context, LottieTestActivity::class.java))
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var lottieUrl: LottieAnimationView
    private lateinit var lottieAssets: LottieAnimationView
    private lateinit var tvRenderMode: TextView
    private lateinit var tvUrlInfo: TextView
    private lateinit var tvAssetsInfo: TextView
    private lateinit var btnPlay: Button
    private lateinit var btnPause: Button
    private lateinit var btnReload: Button
    private lateinit var btnToggleRender: Button
    private lateinit var btnSpeed: Button

    private var renderMode = RenderMode.AUTOMATIC
    private var speedIdx = 0
    private val speeds = arrayOf(1f, 2f, 0.5f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lottie_test)

        initViews()
        updateRenderModeInfo()
        loadLottieFromUrl()
        loadLottieFromAssets()
        setupButtons()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun initViews() {
        lottieUrl = findViewById(R.id.lottie_url)
        lottieAssets = findViewById(R.id.lottie_assets)
        tvRenderMode = findViewById(R.id.tv_render_mode)
        tvUrlInfo = findViewById(R.id.tv_url_info)
        tvAssetsInfo = findViewById(R.id.tv_assets_info)
        btnPlay = findViewById(R.id.btn_play)
        btnPause = findViewById(R.id.btn_pause)
        btnReload = findViewById(R.id.btn_reload)
        btnToggleRender = findViewById(R.id.btn_toggle_render)
        btnSpeed = findViewById(R.id.btn_speed)
    }

    private fun updateRenderModeInfo() {
        val label = when (renderMode) {
            RenderMode.HARDWARE -> "硬件 GPU 渲染"
            RenderMode.SOFTWARE -> "软件 CPU 渲染"
            else -> "自动渲染"
        }
        tvRenderMode.text = label
    }

    private fun applyRenderMode() {
        lottieUrl.setRenderMode(renderMode)
        lottieAssets.setRenderMode(renderMode)
    }

    // ======================== 网络 Lottie ========================

    private fun loadLottieFromUrl() {
        tvUrlInfo.text = "下载中..."
        scope.launch {
            try {
                val jsonString = withContext(Dispatchers.IO) {
                    URL(TEST_LOTTIE_URL).openStream().use { it.bufferedReader().readText() }
                }
                lottieUrl.setAnimationFromJson(jsonString, "url_anim")
                tvUrlInfo.text = "加载成功 | ${TEST_LOTTIE_URL.takeLast(30)}"
            } catch (e: Exception) {
                tvUrlInfo.text = "加载失败: ${e.message}"
                Toast.makeText(this@LottieTestActivity, "网络 JSON 加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ======================== 本地 Assets Lottie ========================

    private fun loadLottieFromAssets() {
        val fileName = "attendance_card.json"
        tvAssetsInfo.text = "加载中..."
        lottieAssets.setAnimation(fileName)
        // autoPlay="true" 在 XML 中，加载完成后自动播放
        tvAssetsInfo.text = "加载成功 | 来源: assets/$fileName"
    }

    // ======================== 按钮操作 ========================

    private fun setupButtons() {
        btnPlay.setOnClickListener {
            lottieUrl.playAnimation()
            lottieAssets.playAnimation()
        }

        btnPause.setOnClickListener {
            lottieUrl.pauseAnimation()
            lottieAssets.pauseAnimation()
        }

        btnReload.setOnClickListener {
            loadLottieFromUrl()
            loadLottieFromAssets()
            Toast.makeText(this, "已重新加载", Toast.LENGTH_SHORT).show()
        }

        btnToggleRender.setOnClickListener {
            renderMode = when (renderMode) {
                RenderMode.AUTOMATIC -> RenderMode.HARDWARE
                RenderMode.HARDWARE -> RenderMode.SOFTWARE
                else -> RenderMode.AUTOMATIC
            }
            applyRenderMode()
            updateRenderModeInfo()
        }

        btnSpeed.setOnClickListener {
            speedIdx = (speedIdx + 1) % speeds.size
            val s = speeds[speedIdx]
            lottieUrl.speed = s
            lottieAssets.speed = s
            btnSpeed.text = "${s}x"
            Toast.makeText(this, "速度切换为 ${s}x", Toast.LENGTH_SHORT).show()
        }
    }
}
