package com.example.myapplication.video

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import com.example.myapplication.R
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer

/**
 * 通用独立视频播放器控件 (无业务耦合，方便多项目复用)
 */
class AppVideoPlayer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : StandardGSYVideoPlayer(context, attrs) {

    private var touchListener: OnTouchListener? = null

    override fun getLayoutId(): Int {
        return R.layout.layout_app_video_player
    }

    override fun init(context: Context) {
        super.init(context)
        // 点击全屏图标时主动收起键盘
        fullscreenButton?.setOnClickListener {
            hideSoftKeyboard()
            startWindowFullscreen(context, true, true)
        }
    }

    override fun updateStartImage() {
        if (mStartButton is android.widget.ImageView) {
            val imageView = mStartButton as android.widget.ImageView
            if (mCurrentState == CURRENT_STATE_PLAYING) {
                imageView.setImageResource(R.drawable.ic_pause_circle)
            } else {
                imageView.setImageResource(R.drawable.ic_play_circle)
            }
        } else {
            super.updateStartImage()
        }
    }

    override fun onClickUiToggle(e: MotionEvent?) {
        super.onClickUiToggle(e)
        touchListener?.onTouch(this, e)
    }

    fun setLockOnTouchListener(onTouchListener: OnTouchListener?) {
        this.touchListener = onTouchListener
    }

    /**
     * 极简配置并准备播放视频
     * @param url 视频链接
     * @param title 视频标题
     * @param playTag 列表上下文 Tag
     * @param position 在列表中的 Index
     * @param autoPlay 是否立即自动播放
     */
    fun setUpVideo(
        url: String,
        title: String,
        playTag: String = "",
        position: Int = 0,
        autoPlay: Boolean = false
    ) {
        val optionBuilder = GSYVideoOptionBuilder()
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setAutoFullWithSize(true)
            .setShowFullAnimation(false)
            .setNeedLockFull(true)
            .setUrl(url)
            .setCacheWithPlay(true)
            .setVideoTitle(title)
            .setPlayTag(playTag)
            .setPlayPosition(position)
            .setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                    super.onQuitFullscreen(url, *objects)
                    hideSoftKeyboard()
                }
            })

        optionBuilder.build(this)

        if (autoPlay) {
            startPlayLogic()
        }
    }

    fun hideSoftKeyboard() {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(windowToken, 0)
        } catch (_: Exception) {
        }
    }
}
