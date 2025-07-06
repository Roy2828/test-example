package com.example.myapplication.xunfei.feiqi


import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.io.File
import java.io.FileInputStream

class AudioTrackPlayer(
    sampleRate: Int,
    channelConfig: Int,
    audioFormat: Int
) {
    private var audioTrack: AudioTrack? = null
    private val bufferSize: Int

    init {
        // 初始化 AudioTrack
        bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
    }

    /**
     * 开始播放
     */
    fun start() {
        audioTrack?.play()
    }

    /**
     * 写入 PCM 数据并播放
     * @param pcmData PCM 数据字节
     */
    fun write(pcmData: ByteArray) {
        audioTrack?.apply {
            if (state == AudioTrack.STATE_INITIALIZED) {
                write(pcmData, 0, pcmData.size)
            }
        }
    }

    /**
     * 暂停播放
     */
    fun pause() {
        audioTrack?.pause()
    }

    /**
     * 停止播放并释放资源
     */
    fun stop() {
        audioTrack?.apply {
            stop()
            release()
        }
        audioTrack = null
    }

    /**
     * 获取缓冲区大小
     * @return 缓冲区大小
     */
    fun getBufferSize(): Int {
        return bufferSize
    }



    fun test(pcmFile:File?){
        fun playWav() {

            var audioPlayer =  AudioTrackPlayer(8000, AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT)
            audioPlayer.start()
            try {
                // 文件路径（请确保文件存在）
                val filePath = pcmFile?.absolutePath
                val fis = FileInputStream(filePath)

                // 循环读取PCM数据并写入播放器
                val buffer = ByteArray(audioPlayer.getBufferSize())
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } > 0) {
                    audioPlayer.write(buffer.copyOf(bytesRead)) // 仅写入实际读取的字节
                }

                fis.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 停止播放并释放资源
            audioPlayer.stop()

        }
    }
}