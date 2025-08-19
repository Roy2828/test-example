package com.example.myapplication.speak.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.alibaba_Identification.LogX
import com.example.myapplication.speak.PlayStatus
import com.example.myapplication.speak.SpeakManager
import com.example.myapplication.speak.SuperPersonifiedTtsParams
import com.hjq.permissions.OnPermission
import com.hjq.permissions.XXPermissions

/**
 *    desc   :
 *    date   : 2025/7/7 18:29
 *    author : Roy
 *    version: 1.0
 */
class SpeakSuperPersonifiedActivity : AppCompatActivity() {

    companion object {

        fun doIntent(context: Context) {
            context.startActivity(Intent(context, SpeakSuperPersonifiedActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.speak_super_personified_activity)

        SpeakManager.getInstance().initAudioPlayThread()

        findViewById<TextView>(R.id.tv_init).setOnClickListener {
            // getPermission()
            //系统api高版本不用权限
            SpeakManager.getInstance().init(
                context = this@SpeakSuperPersonifiedActivity,
                appId = "2c27dfe8",
                apiKey = "f7389733aa86577a49d2a15a18b66031",
                apiSecret = "Yjg4YjNhNzM5NGUzN2I2MzEwMTNiY2E4"
            ){
                findViewById<TextView>(R.id.tv_init).text = if (it) {
                    "SDK初始化成功"
                } else {
                    "SDK初始化失败"
                }
            }
        }

        findViewById<TextView>(R.id.tv_start).setOnClickListener {

            SpeakManager.getInstance().startFlowSpeak(SuperPersonifiedTtsParams(content = "开始",status= PlayStatus.fromValue(0)))
            SpeakManager.getInstance().startFlowSpeak(SuperPersonifiedTtsParams(content = "播放中间，",status= PlayStatus.MIDDLE))
            SpeakManager.getInstance().startFlowSpeak(SuperPersonifiedTtsParams(content = "下一条数据",status= PlayStatus.MIDDLE))
            SpeakManager.getInstance().startFlowSpeak(SuperPersonifiedTtsParams(content = "结束",status= PlayStatus.END))
        }

        findViewById<TextView>(R.id.tv_end).setOnClickListener {
           SpeakManager.getInstance().stop()
        }

        SpeakManager.getInstance().setAudioPlayListener(onCompleted = {
            Toast.makeText(this,"播放完成",Toast.LENGTH_SHORT).show()
        },onError = {errCode,errMsg,sid->
            Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show()
        })
    }


    private fun getPermission() {
        XXPermissions.with(this).permission(
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.INTERNET",
        ).request(object : OnPermission {
            override fun hasPermission(granted: List<String>, all: Boolean) {
                LogX.d(

                    "SDK获取系统权限成功:$all"
                )
                for (i in granted.indices) {
                    LogX.d(

                        "获取到的权限有：" + granted[i]
                    )
                }
                if (all) {
                    SpeakManager.getInstance().init(
                        context = this@SpeakSuperPersonifiedActivity,
                        appId = "2c27dfe8",
                        apiKey = "f7389733aa86577a49d2a15a18b66031",
                        apiSecret = "Yjg4YjNhNzM5NGUzN2I2MzEwMTNiY2E4"
                    ){
                        findViewById<TextView>(R.id.tv_init).text = if (it) {
                            "SDK初始化成功"
                        } else {
                            "SDK初始化失败"
                        }
                    }
                }
            }

            override fun noPermission(denied: List<String?>?, quick: Boolean) {
                if (quick) {
                    LogX.e(

                        "onDenied:被永久拒绝授权，请手动授予权限"
                    )
                    XXPermissions.startPermissionActivity(
                        this@SpeakSuperPersonifiedActivity,
                        denied
                    )
                } else {
                    LogX.e(
                        "onDenied:权限获取失败"
                    )
                }
            }
        })
    }

}