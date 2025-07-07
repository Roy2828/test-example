package com.example.myapplication.alibaba_Identification

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.text.TextUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.alibaba.idst.nui.AsrResult
import com.alibaba.idst.nui.Constants
import com.alibaba.idst.nui.INativeNuiCallback
import com.alibaba.idst.nui.KwsResult
import com.alibaba.idst.nui.NativeNui
import com.example.myapplication.alibaba_Identification.utils.Auth
import com.example.myapplication.alibaba_Identification.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.LinkedBlockingQueue

/**
 *    desc   :
 *    date   : 2025/7/2 13:52
 *    author : Roy
 *    version: 1.0
 */
class VoiceRecognition : CoroutineScope by MainScope() , INativeNuiCallback{

    private val context get() = SpeechTranscriberManager.getInstance().context;
    private val speechTranscriber get() = SpeechTranscriberManager.getInstance().speechTranscriberLazy;
    val nui_instance: NativeNui = NativeNui()
    private var mInit = false
    private val defaultServerUrl = "wss://nls-gateway.cn-shanghai.aliyuncs.com:443/ws/v1"
    private var serverUrl = "";
    private var curTaskId:String ?= null
    private var mStopping = false
    private var mAudioRecorder: AudioRecord? = null
    private var sharding: VoiceRecognitionSharding ?= null
    private var mRecordingAudioFile: OutputStream? = null
    private var mRecordingAudioFilePath = ""
    private var path:String = ""
    private val tmpAudioQueue: LinkedBlockingQueue<ByteArray> = LinkedBlockingQueue<ByteArray>()
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private val SAMPLE_RATE: Int = 16000
    private val WAVE_FRAM_SIZE: Int = 20 * 2 * 1 * SAMPLE_RATE / 1000

    fun init(sharding: VoiceRecognitionSharding) {
        this.sharding = sharding
        path = "${SpeechTranscriberManager.getInstance().getFilePath()}/${sharding.fileName}"
        Utils.createDir(path)

        //初始化SDK，注意用户需要在Auth.getTicket中填入相关ID信息才可以使用。
        val ret: Int = nui_instance.initialize(
            this, genInitParams(path),
            Constants.LogLevel.LOG_LEVEL_VERBOSE, true
        )

        if (ret == Constants.NuiResultCode.SUCCESS) {
            mInit = true
        } else {
            val msgText = Utils.getMsgWithErrorCode(ret, "init")
            LogX.e(msgText)
        }
    }

    private fun startDialog(): Boolean {
        /*
         * 首先，录音权限动态申请
         * */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            val i = ContextCompat.checkSelfPermission(this, permissions.get(0))
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                this.requestPermissions(permissions, 321)
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (mAudioRecorder == null) {
                //录音初始化，录音参数中格式只支持16bit/单通道，采样率支持8K/16K
                //使用者请根据实际情况选择Android设备的MediaRecorder.AudioSource
                //录音麦克风如何选择,可查看https://developer.android.google.cn/reference/android/media/MediaRecorder.AudioSource
                mAudioRecorder = AudioRecord(
                    MediaRecorder.AudioSource.DEFAULT,
                    sharding.sampleRateInHz,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                     WAVE_FRAM_SIZE * 4
                )
                LogX.d( "AudioRecorder new ...")
            } else {
                LogX.w("AudioRecord has been new ...")
            }
        } else {
            LogX.e("donnot get RECORD_AUDIO permission!")
            launch {
                Toast.makeText(
                    context,
                    "未获得录音权限，无法正常运行。请通过设置界面重新开启权限。",
                    Toast.LENGTH_LONG
                ).show()
            }
            showText("asrView", "未获得录音权限，无法正常运行。通过设置界面重新开启权限。")
            return false
        }

        launch {  //设置相关识别参数，具体参考API文档，在startDialog前调用
            val setParamsString: String = genParams()
            LogX.i("nui set params $setParamsString")
            nui_instance.setParams(setParamsString)
            //开始实时识别
            val ret = nui_instance.startDialog(
                Constants.VadMode.TYPE_P2T,
                genDialogParams()
            )
            LogX.i("start done with $ret")
            if (ret == Constants.NuiResultCode.SUCCESS) {
                Toast.makeText(
                    context,
                    "点击<停止>结束实时识别", Toast.LENGTH_SHORT
                ).show()
            }
        }

        return true
    }


    private fun genInitParams( debug_path: String): String {
        var str = ""
        try {
            //获取账号访问凭证：
            var method: Auth.GetTicketMethod =
                Auth.GetTicketMethod.GET_TOKEN_FROM_SERVER_FOR_ONLINE_FEATURES
            if (speechTranscriber.appKey.isNotEmpty()) {
                Auth.setAppKey(speechTranscriber.appKey)
            }
            if (speechTranscriber.accessToken.isNotEmpty()) {
                Auth.setToken(speechTranscriber.accessToken)
            }
            if (speechTranscriber.accessKey.isNotEmpty()) {
                Auth.setAccessKey(speechTranscriber.accessKey)
            }
            if (speechTranscriber.accessKeySecret.isNotEmpty()) {
                Auth.setAccessKeySecret(speechTranscriber.accessKeySecret)
            }
            if (speechTranscriber.stsToken.isNotEmpty()) {
                Auth.setStsToken(speechTranscriber.stsToken)
            }

            // 此处展示将用户传入账号信息进行交互，实际产品不可以将任何账号信息存储在端侧
            if (speechTranscriber.appKey.isNotEmpty()) {
                if (speechTranscriber.accessKey.isNotEmpty() && speechTranscriber.accessKeySecret.isNotEmpty()) {
                    method = if (speechTranscriber.stsToken.isEmpty()) {
                        Auth.GetTicketMethod.GET_ACCESS_IN_CLIENT_FOR_ONLINE_FEATURES
                    } else {
                        Auth.GetTicketMethod.GET_STS_ACCESS_IN_CLIENT_FOR_ONLINE_FEATURES
                    }
                }
                if (speechTranscriber.accessToken.isNotEmpty()) {
                    method = Auth.GetTicketMethod.GET_TOKEN_IN_CLIENT_FOR_ONLINE_FEATURES
                }
            }
            LogX.i("Use method:$method")
            val `object`: JSONObject = Auth.getTicket(method)
            if (!`object`.containsKey("token")) {
                LogX.e("Cannot get token !!!")
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "未获得有效临时凭证！", Toast.LENGTH_LONG
                    ).show()
                }
            }

            `object`["device_id"] = "empty_device_id" // 必填, 推荐填入具有唯一性的id, 方便定位问题
            if (speechTranscriber.serverUrl.isEmpty()) {
                serverUrl = defaultServerUrl // 默认
            }else{
                serverUrl = speechTranscriber.serverUrl
            }
            `object`["url"] = serverUrl

            //工作目录路径，SDK从该路径读取配置文件
//            object.put("workspace", workpath); // V2.6.2版本开始纯云端功能可不设置workspace

            //当初始化SDK时的save_log参数取值为true时，该参数生效。表示是否保存音频debug，该数据保存在debug目录中，需要确保debug_path有效可写。
            `object`["save_wav"] = "true"
            //debug目录，当初始化SDK时的save_log参数取值为true时，该目录用于保存中间音频文件。
            `object`["debug_path"] = debug_path
            //设置本地存储日志文件的最大字节数, 最大将会在本地存储2个设置字节大小的日志文件
            `object`["max_log_file_size"] = 50 * 1024 * 1024

            //过滤SDK内部日志通过回调送回到用户层
            `object`["log_track_level"] =
                Constants.LogLevel.toInt(Constants.LogLevel.LOG_LEVEL_INFO).toString()

            // FullMix = 0   // 选用此模式开启本地功能并需要进行鉴权注册
            // FullCloud = 1
            // FullLocal = 2 // 选用此模式开启本地功能并需要进行鉴权注册
            // AsrMix = 3    // 选用此模式开启本地功能并需要进行鉴权注册
            // AsrCloud = 4
            // AsrLocal = 5  // 选用此模式开启本地功能并需要进行鉴权注册
            // 这里只能选择FullMix和FullCloud
            `object`["service_mode"] = Constants.ModeFullCloud // 必填
            str = `object`.toString()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        // 注意! str中包含ak_id ak_secret token app_key等敏感信息, 实际产品中请勿在Log中输出这类信息！
        LogX.i("InsideUserContext:$str")
        return str
    }



    override fun onNuiEventCallback(
        event: Constants.NuiEvent?,
        resultCode: Int,
        arg2: Int,
        kwsResult: KwsResult?,
        asrResult: AsrResult?
    ) {
        LogX.i("event=$event resultCode=$resultCode")
        // asrResult包含task_id，task_id有助于排查问题，请用户进行记录保存。
        if (event == Constants.NuiEvent.EVENT_TRANSCRIBER_STARTED) {
            // EVENT_TRANSCRIBER_STARTED 为V2.6.3版本新增
            //showText(asrView, "EVENT_TRANSCRIBER_STARTED")
            val jsonObject = JSON.parseObject(asrResult?.allResponse)
            val header = jsonObject.getJSONObject("header")
            curTaskId = header.getString("task_id")
        } else if (event == Constants.NuiEvent.EVENT_TRANSCRIBER_COMPLETE) {
            setButtonState("startButton", true) //开始 按钮可用
            setButtonState("cancelButton", false) //停止 按钮不可用
            appendText("asrView",asrResult?.allResponse)
            mStopping = false
        } else if (event == Constants.NuiEvent.EVENT_ASR_PARTIAL_RESULT || event == Constants.NuiEvent.EVENT_SENTENCE_END) {
            if (mStopping) {
                appendText("asrView",asrResult?.asrResult)
            } else {
                showText("asrView", asrResult?.asrResult)
            }
            val jsonObject = JSON.parseObject(asrResult?.allResponse)
            val payload = jsonObject.getJSONObject("payload")
            val result = payload.getString("result")
            showText("resultView", result)
        } else if (event == Constants.NuiEvent.EVENT_VAD_START) {
            showText("asrView", "EVENT_VAD_START")
        } else if (event == Constants.NuiEvent.EVENT_VAD_END) {
            appendText( "asrView","EVENT_VAD_END")
        } else if (event == Constants.NuiEvent.EVENT_ASR_ERROR) {
            // asrResult在EVENT_ASR_ERROR中为错误信息，搭配错误码resultCode和其中的task_id更易排查问题，请用户进行记录保存。
            appendText("asrView",asrResult?.asrResult)
             launch(Dispatchers.Main) {
                 Toast.makeText(
                     context,
                     "ERROR with $resultCode",
                     Toast.LENGTH_SHORT
                 ).show()
             }
            val msg_text = Utils.getMsgWithErrorCode(resultCode, "start")
            launch(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    msg_text, Toast.LENGTH_SHORT
                ).show()
            }


            setButtonState("startButton", true)
            setButtonState("cancelButton", false)
            mStopping = false
        } else if (event == Constants.NuiEvent.EVENT_MIC_ERROR) {
            // EVENT_MIC_ERROR表示2s未传入音频数据，请检查录音相关代码、权限或录音模块是否被其他应用占用。
            val msg_text = Utils.getMsgWithErrorCode(resultCode, "start")
             launch(Dispatchers.Main) {
                 Toast.makeText(
                     context,
                     msg_text, Toast.LENGTH_SHORT
                 ).show()
             }

            setButtonState("startButton", true)
            setButtonState("cancelButton", false)
            mStopping = false
            // 此处也可重新启动录音模块
        } else if (event == Constants.NuiEvent.EVENT_DIALOG_EX) { /* unused */
            LogX.i("dialog extra message = " + asrResult?.asrResult)
        }
    }

    override fun onNuiNeedAudioData(buffer: ByteArray?, len: Int): Int {
        if (mAudioRecorder == null || buffer == null) {
            return -1
        }

        if (mAudioRecorder!!.getState() != AudioRecord.STATE_INITIALIZED) {
            LogX.e("audio recorder not init")
            return -1
        }


        // 送入SDK
        val audio_size: Int = mAudioRecorder!!.read(buffer, 0, len)


        sharding?.let {
            // 音频存储到本地
            if (it.isSaveAudioToLocal && audio_size > 0) {
                if (mRecordingAudioFile == null) {
                    // 音频存储文件未打开，则等获得task_id后打开音频存储文件，否则数据存储到tmpAudioQueue
                    if (!TextUtils.isEmpty(curTaskId) && mRecordingAudioFile == null) {
                        try {
                            mRecordingAudioFilePath =
                                path + "/" + "st_task_id_" + curTaskId + ".pcm"
                            LogX.i("save recorder data into $mRecordingAudioFilePath")
                            mRecordingAudioFile = FileOutputStream(mRecordingAudioFilePath, true)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        tmpAudioQueue.offer(buffer)
                    }
                }
                if (mRecordingAudioFile != null) {
                    // 若tmpAudioQueue有存储的音频，先存到音频存储文件中
                    if (tmpAudioQueue.size > 0) {
                        try {
                            // 将未打开recorder前的音频存入文件中
                            val audioData: ByteArray = tmpAudioQueue.take()
                            try {
                                mRecordingAudioFile?.write(audioData)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }

                    // 当前音频数据存到音频存储文件
                    try {
                        mRecordingAudioFile?.write(buffer)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }


        return audio_size
    }

    override fun onNuiAudioStateChanged(state: Constants.AudioState?) {
        LogX.i("onNuiAudioStateChanged")
        if (state == Constants.AudioState.STATE_OPEN) {
            LogX.i("audio recorder start")
                mAudioRecorder?.startRecording()
            LogX.i("audio recorder start done")
        } else if (state == Constants.AudioState.STATE_CLOSE) {
            LogX.i( "audio recorder close")
                mAudioRecorder?.release()

            try {
                if (mRecordingAudioFile != null) {
                    mRecordingAudioFile!!.close()
                    mRecordingAudioFile = null
                    val show = "存储录音音频到 $mRecordingAudioFilePath"
                    appendText("asrView", show)
                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            show, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (state == Constants.AudioState.STATE_PAUSE) {
            LogX.i( "audio recorder pause")
            if (mAudioRecorder != null) {
                mAudioRecorder!!.stop()
            }

            try {
                if (mRecordingAudioFile != null) {
                    mRecordingAudioFile!!.close()
                    mRecordingAudioFile = null
                    val show = "存储录音音频到 $mRecordingAudioFilePath"
                    appendText("asrView", show)
                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            show, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onNuiAudioRMSChanged(vol: Float) {

    }

    override fun onNuiVprEventCallback(event: Constants.NuiVprEvent?) {

    }




    fun release(){
        mAudioRecorder?.release()
        mAudioRecorder = null
    }



    fun setButtonState(tag:String,state:Boolean){

    }



    fun appendText(tag:String,text:String?){

    }

    fun showText(tag:String?,text: String?){

    }



}