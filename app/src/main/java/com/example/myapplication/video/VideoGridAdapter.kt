package com.example.myapplication.video

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack

/**
 * 2列双排视频/图片混合列表 Adapter
 * 支持视频卡片 (TYPE_VIDEO) 与 纯图片卡片 (TYPE_IMAGE)
 */
class VideoGridAdapter(
    private val videoList: List<VideoItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TAG = "VIDEO_GRID_PLAY_TAG"
        private const val TYPE_VIDEO = 0
        private const val TYPE_IMAGE = 1
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val player: AppVideoPlayer = itemView.findViewById(R.id.video_player_item)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_video_title)
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPureImage: ImageView = itemView.findViewById(R.id.iv_pure_image)
    }

    override fun getItemViewType(position: Int): Int {
        return if (videoList[position].mediaType == MediaType.IMAGE) {
            TYPE_IMAGE
        } else {
            TYPE_VIDEO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_IMAGE) {
            val view = inflater.inflate(R.layout.item_image_card, parent, false)
            ImageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_video_card, parent, false)
            VideoViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val video = videoList[position]

        if (holder is ImageViewHolder) {
            // 绑定纯图片卡片数据
            if (video.coverUrl.isNotBlank()) {
                Glide.with(holder.itemView.context)
                    .load(video.coverUrl)
                    .centerCrop()
                    .placeholder(R.drawable.shape_gray_alpha_12)
                    .into(holder.ivPureImage)
            } else {
                holder.ivPureImage.setImageResource(R.drawable.shape_gray_alpha_12)
            }
        } else if (holder is VideoViewHolder) {
            // 绑定视频卡片数据
            holder.tvTitle.text = video.title

            // 动态创建并绑定封面图片 View (ThumbImageView)
            val coverImageView = ImageView(holder.itemView.context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            if (video.coverUrl.isNotBlank()) {
                Glide.with(holder.itemView.context)
                    .load(video.coverUrl)
                    .centerCrop()
                    .placeholder(R.drawable.shape_gray_alpha_12)
                    .into(coverImageView)
            } else {
                coverImageView.setImageResource(R.drawable.shape_gray_alpha_12)
            }

            // 配置列表播放项及封面图
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
                .setThumbImageView(coverImageView)
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
    }

    override fun getItemCount(): Int = videoList.size
}
