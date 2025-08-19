package com.example.myapplication.speak


import android.os.Bundle
import com.example.myapplication.alibaba_Identification.LogX
import com.example.myapplication.speak.SuperAudioPlay.Companion.AUDIOPLAYER_END
import com.example.myapplication.speak.SuperAudioPlay.Companion.AUDIOPLAYER_INIT
import com.example.myapplication.speak.SuperAudioPlay.Companion.AUDIOPLAYER_START
import com.example.myapplication.speak.SuperAudioPlay.Companion.AUDIOPLAYER_WRITE

import com.iflytek.sparkchain.core.tts.PersonateTTS
import com.iflytek.sparkchain.core.tts.TTS.TTSError
import com.iflytek.sparkchain.core.tts.TTS.TTSResult
import com.iflytek.sparkchain.core.tts.TTSCallbacks

/**
 *    desc   :
 *    date   : 2025/7/7 16:50
 *    author : Roy
 *    version: 1.0
 */
class SuperPersonifiedTtsImpl :ISuperPersonifiedTts{

    /**
     * 播放器，用于播报合成的音频。
     * 注意：当前Demo中的播放器仅实现了播放PCM格式的音频，如果客户合成的是其他格式的音频，需自行实现播放功能。
     */

    private var personateTTS: PersonateTTS? = null



    private var onError: ((errCode: Int?, errMsg: String?, sid: String?) -> Unit)?=null


    private val audioPlay by lazy { SuperAudioPlay() }



    override fun initAudioPlayThread(){
         audioPlay.start()
    }


    override fun startFlow(mPersonateTTSParams:SuperPersonifiedTtsParams) {
        LogX.d("startFlow-->")
        LogX.d("vcn = " + mPersonateTTSParams.vcn)
        LogX.d("pitch = " + mPersonateTTSParams.pitch)
        LogX.d("speed = " + mPersonateTTSParams.speed)
        LogX.d("volume = " + mPersonateTTSParams.volume)
        if (audioPlay.getAudioTrack == null) {
            audioPlay.sendEmptyMessage(AUDIOPLAYER_INIT)
        } else {
            if (!audioPlay.isPlaying() && mPersonateTTSParams.status == PlayStatus.START) {
                audioPlay.sendEmptyMessage(AUDIOPLAYER_START)
            }
        }
        /******************
         * 超拟人发音人设置接口，发音人可从构造方法中设入，也可通过功能参数动态修改。
         * x4_lingxiaoxuan_oral，聆⼩璇，⼥：中⽂
         * x4_lingfeizhe_oral，聆⻜哲，男：中⽂
         */

        personateTTS = personateTTS ?: PersonateTTS(mPersonateTTSParams.vcn)

        personateTTS?.apply {
           speed(mPersonateTTSParams.speed) //语速：0对应默认语速的1/2，100对应默认语速的2倍。最⼩值:0, 最⼤值:100
           pitch(mPersonateTTSParams.pitch) //语调：0对应默认语速的1/2，100对应默认语速的2倍。最⼩值:0, 最⼤值:100
           volume(mPersonateTTSParams.volume) //音量：0是静音，1对应默认音量1/2，100对应默认音量的2倍。最⼩值:0, 最⼤值:100
           sparkAssist(true) //是否通过⼤模型进⾏⼝语化。开启:true, 关闭:false
           oralLevel("high") //⼝语化等级。⾼:high, 中:mid, 低:low
           sampleRate(SuperAudioPlay.SAMPLE_RATE)
           registerCallbacks(mTTSCallback)
            //var status = 0 //输入文本状态，0:开始，1:中间，2:结束
            val res: Int =  aRun(mPersonateTTSParams.content, mPersonateTTSParams.status.value)
            if (res != 0) {
                LogX.e( "合成出错!ret=$res")
            }
        }

    }


    var mTTSCallback: TTSCallbacks = object : TTSCallbacks {

        override fun onResult(result: TTSResult, o: Any?) {
            //解析获取的交互结果，示例展示所有结果获取，开发者可根据自身需要，选择获取。
            val audio = result.data //音频数据
            val len = result.len //音频数据长度
            val status = result.status //数据状态
            val seq = result.seq //数据序号
            val ced = result.ced //进度
            val pybuf = result.pybuf //拼音结果
            val version = result.version //引擎版本号
            val sid = result.sid //sid

            val results =
                "{len=$len,status=$status,seq=$seq,ced=$ced,pybuf=$pybuf,version=$version,sid=$sid"
            LogX.d(results)

            val bundle = Bundle()
            bundle.putByteArray("audio", audio)
            val msg = audioPlay.obtainMessage()
            msg?.what = AUDIOPLAYER_WRITE
            msg?.obj = bundle
            audioPlay.sendMessage(msg)

            if (status == 2) {
                //音频合成回调结束状态，注意，此状态不是播报完成状态
                audioPlay.sendEmptyMessage(AUDIOPLAYER_END)
            }
        }

        override fun onError(ttsError: TTSError, o: Any) {
            val errCode = ttsError.code //错误码
            val errMsg = ttsError.errMsg //错误信息
            val sid = ttsError.sid //sid
            LogX.d("onError:errCode:$errCode,errMsg:$errMsg")
            if (audioPlay.isPlaying()) {
                //如果此时已经播报，则停止播报
                stop()
            }
            onError?.invoke(errCode, errMsg, sid)
        }
    }



    override fun stop() {
        personateTTS?.let {
            audioPlay.removeCallbacksAndMessages()
            audioPlay.sendEmptyMessage(AUDIOPLAYER_END)
            personateTTS?.stop()
            personateTTS = null
        }
    }





    override fun setAudioPlayListener(
        onCompleted: () -> Unit,
        onError: (errCode: Int?, errMsg: String?, sid: String?) -> Unit
    ) {
        this.onError = onError
        audioPlay.setAudioPlayListener(onCompleted,onError)
    }

    override fun isPlaying(): Boolean {
        return audioPlay.isPlaying()
    }

    override fun onDestroy() {
         audioPlay.onDestroy()
    }
}