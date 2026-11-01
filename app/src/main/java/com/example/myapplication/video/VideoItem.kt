package com.example.myapplication.video

enum class MediaType {
    VIDEO,
    IMAGE
}

/**
 * 列表媒体数据 Model (支持视频与纯图片类型)
 */
data class VideoItem(
    val id: String,
    val title: String,
    val videoUrl: String = "",
    val coverUrl: String = "",
    val mediaType: MediaType = MediaType.VIDEO
)
