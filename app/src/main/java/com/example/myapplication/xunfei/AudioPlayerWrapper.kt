package com.example.myapplication.xunfei

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream

class AudioPlayerWrapper {
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var playbackThread: Thread? = null
    private val handler = Handler(Looper.getMainLooper())
    private var listener: OnPlaybackListener? = null

    // 播放监听接口
    interface OnPlaybackListener {
        fun onProgress(currentPositionMs: Int, totalDurationMs: Int)
        fun onCompletion()
    }

    /**
     * 开始播放（流模式）
     * @param dataProvider 音频数据提供器
     * @param sampleRate 采样率（默认44100Hz）
     * @param channelConfig 声道（默认单声道）
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun startPlay(
        pcmFilePath:String,
        sampleRate: Int = 8000,
        channelConfig: Int = AudioFormat.CHANNEL_OUT_STEREO
    ) {
        if (isPlaying) return

        // 1. 检查文件是否存在
        val pcmFile: File = File(pcmFilePath)
        if (!pcmFile.exists()) {
            Log.e("AudioPlayerWrapper", "PCM file does not exist: $pcmFilePath")
            return
        }

        var dataProvider = AudioDataProviderImpl(pcmFile)

        // 1. 初始化AudioTrack [3,6](@ref)
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            channelConfig,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize * 2) // 扩大缓冲区减少卡顿
            .build()

        // 2. 启动播放线程
        isPlaying = true
        playbackThread = Thread {
            audioTrack?.play()
            val buffer = ByteArray(bufferSize)
            var totalBytes = 0

            try {
                while (isPlaying) {
                    val readBytes = dataProvider.read(buffer)
                    if (readBytes <= 0) break // 数据读取结束

                    audioTrack?.write(buffer, 0, readBytes)
                    totalBytes += readBytes

                    // 计算进度（毫秒）[2](@ref)
                    val currentMs = bytesToMilliseconds(totalBytes, sampleRate, channelConfig)
                    val totalMs = bytesToMilliseconds(dataProvider.totalSize, sampleRate, channelConfig)
                    notifyProgress(currentMs, totalMs)
                }

                // 播放完成回调
                if (isPlaying) {
                    handler.post { listener?.onCompletion() }
                }
            } finally {
                stopPlay() // 确保资源释放
            }
        }.apply { start() }
    }

    /**
     * 停止播放并释放资源
     */
    fun stopPlay() {
        isPlaying = false
        playbackThread?.interrupt()
        playbackThread = null

        audioTrack?.apply {
            if (state != AudioTrack.STATE_UNINITIALIZED) {
                stop()
                release()
            }
        }
        audioTrack = null
    }

    /**
     * 设置播放监听器
     */
    fun setPlaybackListener(listener: OnPlaybackListener) {
        this.listener = listener
    }

    // 字节转毫秒计算公式 [2](@ref)
    private fun bytesToMilliseconds(
        bytes: Int,
        sampleRate: Int,
        channelConfig: Int
    ): Int {
        val bytesPerSecond = sampleRate *
                when (AudioFormat.ENCODING_PCM_16BIT) {
                    AudioFormat.ENCODING_PCM_16BIT -> 2
                    else -> 1
                } *
                when (channelConfig) {
                    AudioFormat.CHANNEL_OUT_STEREO -> 2
                    else -> 1
                }
        return (bytes * 1000L / bytesPerSecond).toInt()
    }

    // 主线程进度通知
    private fun notifyProgress(currentMs: Int, totalMs: Int) {
        handler.post { listener?.onProgress(currentMs, totalMs) }
    }

    /**
     * 音频数据提供接口
     */
    interface AudioDataProvider {
        val totalSize: Int // 总字节数（未知返回-1）
        fun read(buffer: ByteArray): Int // 返回读取字节数（≤0表示结束）
    }

    class  AudioDataProviderImpl (pcmFile: File):AudioDataProvider {
        private val file = File(pcmFile.absolutePath)
        private val fis = FileInputStream(file)

        override val totalSize: Int get() = file.length().toInt()

        override fun read(buffer: ByteArray): Int {
            return fis.read(buffer)
        }
    }
}