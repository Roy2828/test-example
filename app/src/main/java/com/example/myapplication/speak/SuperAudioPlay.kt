package com.example.myapplication.speak

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.example.myapplication.alibaba_Identification.LogX


/**
 *    desc   :
 *    date   : 2025/7/8 09:58
 *    author : Roy
 *    version: 1.0
 */
class SuperAudioPlay : AudioPlayListener {

    private var audioTrack: AudioTrack? = null

    val getAudioTrack get() = audioTrack


    private var mAudioPlayHandler: Handler? = null

    private var isPlaying = false

    private var onCompleted: (() -> Unit)? = null
    private var onError: ((errCode: Int?, errMsg: String?, sid: String?) -> Unit)? = null

    companion object {
        const val AUDIOPLAYER_INIT: Int = 0x0000
        const val AUDIOPLAYER_START: Int = 0x0001
        const val AUDIOPLAYER_WRITE: Int = 0x0002
        const val AUDIOPLAYER_END: Int = 0x0003
        const val CHANNEL_CONFIG: Int = AudioFormat.CHANNEL_OUT_MONO // 单声道输出
        const val AUDIO_FORMAT: Int = AudioFormat.ENCODING_PCM_16BIT // PCM 16位编码

        const val SAMPLE_RATE = 16000 //合成音频的采样率，支持8K 16K音频，具体参见集成文档
    }


    private val mAudioPlayThread = Thread {
        Looper.prepare()
        mAudioPlayHandler = object : Handler(Looper.myLooper()!!) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    AUDIOPLAYER_INIT -> {
                        LogX.d("audioInit")
                        val minBufferSize =
                            AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
                        audioTrack = AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            SAMPLE_RATE,
                            CHANNEL_CONFIG,
                            AUDIO_FORMAT,
                            minBufferSize,
                            AudioTrack.MODE_STREAM
                        )
                        mAudioPlayHandler!!.sendEmptyMessage(AUDIOPLAYER_START)
                    }

                    AUDIOPLAYER_START -> {
                        LogX.d("audioStart")
                        audioTrack?.let {
                            isPlaying = true
                            audioTrack?.play()
                        }
                    }

                    AUDIOPLAYER_WRITE -> {
                        val bundle = msg.obj as Bundle
                        val audioData = bundle.getByteArray("audio")
                        if (audioTrack != null && (audioData?.size ?: 0) > 0) {
                            audioTrack?.write(audioData!!, 0, audioData.size)
                        }
                    }

                    AUDIOPLAYER_END -> {
                        LogX.d("audioEnd")
                        audioTrack?.let {
                            audioTrack?.stop()
                            isPlaying = false
                        }
                        onCompleted?.invoke()

                    }
                }
            }
        }
        Looper.loop()
    }


    fun start() {
        if (!mAudioPlayThread.isAlive) {
            mAudioPlayThread.start()
        }
    }


    fun sendEmptyMessage(what: Int) {
        mAudioPlayHandler?.sendEmptyMessage(what)
    }


    fun obtainMessage(): Message? {
        return mAudioPlayHandler?.obtainMessage()
    }


    fun sendMessage(msg: Message?) {
        msg?.apply {
            mAudioPlayHandler?.sendMessage(this)
        }

    }


    fun removeCallbacksAndMessages() {
        mAudioPlayHandler?.removeCallbacksAndMessages(null)
    }


    fun onDestroy() {
        if (isPlaying) {
            isPlaying = false
            mAudioPlayHandler?.removeCallbacksAndMessages(null)
            mAudioPlayHandler?.sendEmptyMessage(AUDIOPLAYER_END)
        }
    }


    fun isPlaying(): Boolean {
        return isPlaying
    }

    override fun setAudioPlayListener(
        onCompleted: () -> Unit,
        onError: (errCode: Int?, errMsg: String?, sid: String?) -> Unit
    ) {

        this.onCompleted = onCompleted
        this.onError = onError
    }


}