package com.example.myapplication.video

/**
 * 视频数据 Model
 */
data class VideoItem(
    val id: String,
    val title: String,
    val videoUrl: String,
    val coverUrl: String = ""
)
