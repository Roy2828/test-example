package com.example.myapplication.video

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.shuyu.gsyvideoplayer.GSYVideoManager

/**
 * 2列视频列表 Activity 示例
 * 展示封装好的 AppVideoPlayer 控件在 RecyclerView 2列网格中的复用与互斥播放
 */
class VideoPlayActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, VideoPlayActivity::class.java))
        }
    }

    private lateinit var ivBack: ImageView
    private lateinit var rvVideoList: RecyclerView
    private lateinit var videoAdapter: VideoGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        initViews()
        setupRecyclerView()
    }

    private fun initViews() {
        ivBack = findViewById(R.id.iv_back)
        rvVideoList = findViewById(R.id.rv_video_list)

        ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        // 设置 2 列网格布局 (SpanCount = 2)
        val gridLayoutManager = GridLayoutManager(this, 2)
        rvVideoList.layoutManager = gridLayoutManager

        // 示例视频数据
        val sampleVideos = listOf(
            VideoItem(
                id = "1",
                title = "示例视频 1 - Big Buck Bunny",
                videoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4"
            ),
            VideoItem(
                id = "2",
                title = "示例视频 2 - 高清自然风光",
                videoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4"
            ),
            VideoItem(
                id = "3",
                title = "示例视频 3 - 城市风貌",
                videoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4"
            ),
            VideoItem(
                id = "4",
                title = "示例视频 4 - 纪实短片",
                videoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4"
            ),
            VideoItem(
                id = "5",
                title = "示例视频 5 - 动画演示",
                videoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4"
            ),
            VideoItem(
                id = "6",
                title = "示例视频 6 - 校园风光",
                videoUrl = "https://media.w3.org/2010/05/sintel/trailer.mp4"
            )
        )

        videoAdapter = VideoGridAdapter(sampleVideos)
        rvVideoList.adapter = videoAdapter

        // 监听列表滑动：滑出屏幕时释放播放器
        rvVideoList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisible = gridLayoutManager.findFirstVisibleItemPosition()
                val lastVisible = gridLayoutManager.findLastVisibleItemPosition()

                val activePosition = GSYVideoManager.instance().playPosition
                if (activePosition >= 0) {
                    if (activePosition < firstVisible || activePosition > lastVisible) {
                        if (GSYVideoManager.instance().playTag == VideoGridAdapter.TAG) {
                            GSYVideoManager.releaseAllVideos()
                        }
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}
