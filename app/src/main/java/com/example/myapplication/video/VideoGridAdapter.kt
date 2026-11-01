package com.example.myapplication.video

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack

/**
 * 2列双排视频列表 Adapter
 */
class VideoGridAdapter(
    private val videoList: List<VideoItem>
) : RecyclerView.Adapter<VideoGridAdapter.VideoViewHolder>() {

    companion object {
        const val TAG = "VIDEO_GRID_PLAY_TAG"
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val player: AppVideoPlayer = itemView.findViewById(R.id.video_player_item)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_video_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_card, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]
        holder.tvTitle.text = video.title

        // 配置列表播放项
        val gsyVideoOption = GSYVideoOptionBuilder()
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setAutoFullWithSize(true)
            .setShowFullAnimation(false)
            .setNeedLockFull(true)
            .setUrl(video.videoUrl)
            .setVideoTitle(video.title)
            .setCacheWithPlay(true)
            .setPlayTag(TAG)
            .setPlayPosition(position)
            .setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onStartPrepared(url: String?, vararg objects: Any?) {
                    super.onStartPrepared(url, *objects)
                    // 确保播放新视频时，列表中其他正在播放的视频自动暂停/释放
                    GSYVideoManager.releaseAllVideos()
                }

                override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                    super.onQuitFullscreen(url, *objects)
                    holder.player.hideSoftKeyboard()
                }
            })

        gsyVideoOption.build(holder.player)

        // 点击全屏按钮事件
        holder.player.fullscreenButton?.setOnClickListener {
            holder.player.hideSoftKeyboard()
            holder.player.startWindowFullscreen(holder.itemView.context, true, true)
        }
    }

    override fun getItemCount(): Int = videoList.size
}
