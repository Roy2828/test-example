package com.example.myapplication.xunfei


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.myapplication.R
import com.iflytek.cloud.ErrorCode
import com.iflytek.cloud.InitListener
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechEvent
import com.iflytek.cloud.SpeechSynthesizer
import com.iflytek.cloud.SpeechUtility
import com.iflytek.cloud.SynthesizerListener
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2025/7/5 15:16
 *    author : Roy
 *    version: 1.0
 */
class Speak {
    // 缓冲进度
    private var mPercentForBuffering = 0

    // 播放进度
    private var mPercentForPlaying = 0

    private var pcmFile: File? = null

    private val texts =
        "科大讯飞作为智能语音技术提供商，在智能语音技术领域有着长期的研究积累，并在中文语音合成、语音识别、口语评测等多项技术上拥有技术成果。科大讯飞是我国以语音技术为产业化方向的“国家863计划产业化基地”....."

    private val voicer = "x4_yezi"//"xiaoyan"

    // 语音合成对象
    private var mTts: SpeechSynthesizer? = null

    // 引擎类型
    private val mEngineType = SpeechConstant.TYPE_CLOUD

    @SuppressLint("StaticFieldLeak")
    var context: Context? = null

    fun init(context: Context) {
        this.context = context
        if (mscInitialize) {
            return
        }
        mscInitialize = true
        SpeechUtility.createUtility(context, "appid=924cb696")
    }

    companion object {
        private var mscInitialize: Boolean = false

        val instance: Speak by lazy {
            Speak()
        }


    }

    /**
     * 初始化监听。
     */
    private val mTtsInitListener = InitListener { code ->

        if (code != ErrorCode.SUCCESS) {
            Log.e(
                "Roy",
                "初始化失败,错误码：$code,请点击网址https://www.xfyun.cn/document/error-code查询解决方案"
            )
        } else {
            // 初始化成功，之后可以调用startSpeaking方法
            // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
            // 正确的做法是将onCreate中的startSpeaking调用移至这里
        }
    }


    fun createSynthesizer(context: Context) {

        // 初始化合成对象
        mTts = SpeechSynthesizer.createSynthesizer(context, mTtsInitListener)
    }


    fun startSpeaking() {
        pcmFile = File(context?.getExternalCacheDir()?.getAbsolutePath(), "tts_pcmFile.pcm")
        pcmFile!!.delete()
        // 设置参数
        setParam()

        // 合成并播放
        // val code = mTts!!.startSpeaking(texts, mTtsListener)


        //			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
        var path = context?.externalCacheDir?.getAbsolutePath() + "/msc/tts.pcm";
//                //  synthesizeToUri 只保存音频不进行播放
        var code = mTts?.synthesizeToUri(texts, path, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            // showTip("语音合成失败,错误码: $code,请点击网址https://www.xfyun.cn/document/error-code查询解决方案")
        }
    }

    /**
     * 参数设置
     *
     * @return
     */
    private fun setParam() {
        // 清空参数
        mTts!!.setParameter(SpeechConstant.PARAMS, null)
        // 根据合成引擎设置相应参数
        if (mEngineType == SpeechConstant.TYPE_CLOUD) {
            mTts!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
            // 支持实时音频返回，仅在 synthesizeToUri 条件下支持
            mTts!!.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1")

            //	mTts.setParameter(SpeechConstant.TTS_BUFFER_TIME,"1");

            // 设置在线合成发音人
            mTts!!.setParameter(SpeechConstant.VOICE_NAME, voicer)
            //设置合成语速
            mTts!!.setParameter(
                SpeechConstant.SPEED,
                "50"
            )
            //设置合成音调
            mTts!!.setParameter(
                SpeechConstant.PITCH,
                "50"
            )
            //设置合成音量
            mTts!!.setParameter(
                SpeechConstant.VOLUME,
                "50"
            )
        } else {
            mTts!!.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL)
            mTts!!.setParameter(SpeechConstant.VOICE_NAME, "")
        }

        //设置播放器音频流类型
        mTts!!.setParameter(
            SpeechConstant.STREAM_TYPE,
            "3"
        )
        // 设置播放合成音频打断音乐播放，默认为true
        mTts!!.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false")

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts!!.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm")
        context?.apply {
            mTts!!.setParameter(
                SpeechConstant.TTS_AUDIO_PATH,
                getExternalFilesDir("msc")?.getAbsolutePath() + "/tts.pcm"
            )
        }

    }


    /**
     * 合成回调监听。
     */
    private val mTtsListener: SynthesizerListener = object : SynthesizerListener {
        override fun onSpeakBegin() {
            // showTip("开始播放")
        }

        override fun onSpeakPaused() {
            // showTip("暂停播放")
        }

        override fun onSpeakResumed() {
            // showTip("继续播放")
        }

        override fun onBufferProgress(
            percent: Int, beginPos: Int, endPos: Int,
            info: String?
        ) {
            // 合成进度
            Log.e("MscSpeechLog_", "percent =$percent")
            mPercentForBuffering = percent
            Log.e(
                "Roy", java.lang.String.format(
                    context?.getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying
                )
            )
        }

        override fun onSpeakProgress(percent: Int, beginPos: Int, endPos: Int) {
            // 播放进度
            Log.e("MscSpeechLog_", "percent =$percent")
            mPercentForPlaying = percent
            Log.e(
                "Roy", java.lang.String.format(
                    context!!.getString(R.string.tts_toast_format),
                    mPercentForBuffering, mPercentForPlaying
                )
            )

            /*  val style = SpannableStringBuilder(texts)
            Log.e(TAG, "beginPos = $beginPos  endPos = $endPos")
            style.setSpan(
                BackgroundColorSpan(Color.RED),
                beginPos,
                endPos,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            (findViewById(R.id.tts_text) as EditText).setText(style)*/
        }

        override fun onCompleted(error: SpeechError?) {
            // showTip("播放完成")
            if (error != null) {
                //  showTip(error.getPlainDescription(true))
                return
            }
            play()
        }

        override fun onEvent(eventType: Int, arg1: Int, arg2: Int, obj: Bundle?) {
            //	 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	 若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                val sid = obj?.getString(SpeechEvent.KEY_EVENT_SESSION_ID)
                // Log.d(TAG, "session id =$sid")
            }
            // 当设置 SpeechConstant.TTS_DATA_NOTIFY 为1时，抛出buf数据
            if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
                val buf = obj?.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER)
                //Log.e(TAG, "EVENT_TTS_BUFFER = " + buf!!.size)
                // 保存文件
                appendFile(pcmFile!!, buf!!)
            }
        }
    }

    /**
     * 给file追加数据
     */
    private fun appendFile(file: File, buffer: ByteArray) {
        try {
            if (!file.exists()) {
                val b = file.createNewFile()
            }
            val randomFile = RandomAccessFile(file, "rw")
            randomFile.seek(file.length())
            randomFile.write(buffer)
            randomFile.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun stopSpeaking() {
        mTts?.stopSpeaking()

        player.stopPlay()
    }



    val player = AudioPlayerWrapper()
    fun play(){
// 设置监听器
        player.setPlaybackListener(object : AudioPlayerWrapper.OnPlaybackListener {
            override fun onProgress(currentMs: Int, totalMs: Int) {
              //  progressBar.max = totalMs
               // progressBar.progress = currentMs
                val percent = if (totalMs <= 0) 0f else (currentMs * 100f / totalMs).toInt().coerceIn(0, 100)
            Log.e("Roy", "当前进度: $percent, 总时长: $totalMs")
            }

            override fun onCompletion() {
                Toast.makeText(context, "播放完成", Toast.LENGTH_SHORT).show()
            }
        })

// 开始播放
        player.startPlay(pcmFile!!.absolutePath)
    }





}