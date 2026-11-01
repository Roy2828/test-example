package com.example.myapplication.video

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.OrientationUtils

/**
 * 视频播放 Activity 入口
 * 基于 GSYVideoPlayer (来自 android-classpad-landscape 项目迁移)
 */
class VideoPlayActivity : AppCompatActivity() {

    companion object {
        private const val SAMPLE_VIDEO_URL =
            "https://v-cdn.zjol.com.cn/280443.mp4"

        fun start(context: Context) {
            context.startActivity(Intent(context, VideoPlayActivity::class.java))
        }
    }

    private lateinit var videoPlayer: CampusGSYVideoPlayer
    private lateinit var ivBack: ImageView
    private lateinit var ivPlayLandscape: ImageView
    private lateinit var etVideoUrl: EditText
    private lateinit var btnPlayUrl: Button
    private lateinit var btnPlaySample: Button
    private lateinit var btnPause: Button
    private lateinit var btnFullscreen: Button
    private lateinit var tvStatus: TextView

    private lateinit var orientationUtils: OrientationUtils
    private var isPlay = false
    private var isPause = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        initViews()
        setupPlayer()
        setupListeners()

        // 默认自动播放示例视频
        playVideo(SAMPLE_VIDEO_URL, "Big Buck Bunny 示例视频")
    }

    private fun initViews() {
        videoPlayer = findViewById(R.id.video_player)
        ivBack = findViewById(R.id.iv_back)
        ivPlayLandscape = findViewById(R.id.iv_play_landscape)
        etVideoUrl = findViewById(R.id.et_video_url)
        btnPlayUrl = findViewById(R.id.btn_play_url)
        btnPlaySample = findViewById(R.id.btn_play_sample)
        btnPause = findViewById(R.id.btn_pause)
        btnFullscreen = findViewById(R.id.btn_fullscreen)
        tvStatus = findViewById(R.id.tv_status)

        etVideoUrl.setText(SAMPLE_VIDEO_URL)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = currentFocus ?: window.decorView
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
        etVideoUrl.clearFocus()
    }

    private fun setupPlayer() {
        // 旋转逻辑控制
        orientationUtils = OrientationUtils(this, videoPlayer)
        orientationUtils.isEnable = false

        // 设置全屏按键功能
        videoPlayer.fullscreenButton.setOnClickListener {
            hideKeyboard()
            orientationUtils.resolveByClick()
            videoPlayer.startWindowFullscreen(this, true, true)
        }

        // 设置返回键
        videoPlayer.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun playVideo(url: String, title: String) {
        if (url.isBlank()) {
            Toast.makeText(this, "请输入有效的视频地址", Toast.LENGTH_SHORT).show()
            return
        }

        tvStatus.text = "正在准备视频: $title"

        val gsyVideoOption = GSYVideoOptionBuilder()
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setAutoFullWithSize(true)
            .setShowFullAnimation(false)
            .setNeedLockFull(true)
            .setUrl(url)
            .setCacheWithPlay(true)
            .setVideoTitle(title)
            .setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onPrepared(url: String?, vararg objects: Any?) {
                    super.onPrepared(url, *objects)
                    orientationUtils.isEnable = true
                    isPlay = true
                    tvStatus.text = "视频已就绪，播放中: $title"
                }

                override fun onPlayError(url: String?, vararg objects: Any?) {
                    super.onPlayError(url, *objects)
                    tvStatus.text = "视频播放失败 ($url)"
                    Toast.makeText(this@VideoPlayActivity, "视频播放出错", Toast.LENGTH_SHORT).show()
                }

                override fun onAutoComplete(url: String?, vararg objects: Any?) {
                    super.onAutoComplete(url, *objects)
                    tvStatus.text = "视频播放完毕"
                }
            })
            .build(videoPlayer)

        videoPlayer.startPlayLogic()
    }

    private fun setupListeners() {
        ivBack.setOnClickListener {
            finish()
        }

        btnPlayUrl.setOnClickListener {
            hideKeyboard()
            val inputUrl = etVideoUrl.text.toString().trim()
            playVideo(inputUrl, "自定义视频")
        }

        btnPlaySample.setOnClickListener {
            hideKeyboard()
            etVideoUrl.setText(SAMPLE_VIDEO_URL)
            playVideo(SAMPLE_VIDEO_URL, "Big Buck Bunny 示例视频")
        }

        btnPause.setOnClickListener {
            if (isPause) {
                videoPlayer.onVideoResume()
                isPause = false
                tvStatus.text = "恢复播放"
            } else {
                videoPlayer.onVideoPause()
                isPause = true
                tvStatus.text = "已暂停播放"
            }
        }

        btnFullscreen.setOnClickListener {
            hideKeyboard()
            orientationUtils.resolveByClick()
            videoPlayer.startWindowFullscreen(this, true, true)
        }

        ivPlayLandscape.setOnClickListener {
            hideKeyboard()
            orientationUtils.resolveByClick()
            videoPlayer.startWindowFullscreen(this, true, true)
        }
    }

    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        videoPlayer.currentPlayer.onVideoPause()
        super.onPause()
        isPause = true
    }

    override fun onResume() {
        videoPlayer.currentPlayer.onVideoResume()
        super.onResume()
        isPause = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPlay) {
            videoPlayer.currentPlayer.release()
        }
        orientationUtils.releaseListener()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isPlay && !isPause) {
            videoPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true)
        }
    }
}
