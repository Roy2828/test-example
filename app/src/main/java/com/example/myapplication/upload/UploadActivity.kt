package com.example.myapplication.upload

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.TestService
import com.example.myapplication.uploadnew.ServerCredentialProvider
import com.example.myapplication.uploadnew.UploadSharding
import com.tencent.qcloud.core.auth.SessionQCloudCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

/**
 *    desc   :
 *    date   : 2025/6/25 09:30
 *    author : Roy
 *    version: 1.0
 */
class UploadActivity :AppCompatActivity() , CoroutineScope by MainScope() {

    companion object{
        fun doIntent(context:Context){
            context.startActivity(Intent(context,UploadActivity::class.java))
        }
    }

    val multiPartsUploadObject =MultiPartsUploadObject()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_activity)


        findViewById<TextView>(R.id.tv).setOnClickListener {
            findViewById<TextView>(R.id.tvUrl).text = "上传中"
           /* multiPartsUploadObject.testMultiPartsUploadObject(this){
                runOnUiThread {
                    findViewById<TextView>(R.id.tvUrl).text = it
                }
            }*/
            test(this)
        }


    }


    fun test(context: Context) {
        var srcFile: File? = null
        try {
            srcFile = File(context.cacheDir, "exampleobject")
            if (!srcFile!!.exists() && srcFile!!.createNewFile()) {
                val raf = RandomAccessFile(srcFile, "rw")
                raf.setLength(3000000)
                raf.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val sessionToken = "dPz0ibunE6wxT5oJvcvTb5UR2fBltQzaf30d4104ca1609720d3f2547900154b7wuX7UtY-sA5YL66OHEym9Lx2e41Y7AJo5ABkjPONjcayVj3Cqrj7LDuECEKQ8AbX6gYrcXF-lz4oj_Lr0-OrOZk_4reY6uK0OTUbviWW9PMm0wLIgfDOKwgKJU8Wy3hMKiHUnDiUVDU7uscEetYyLYnT2crPB_z8fNanBc7mRnhnT5DcDUxYCGhFNVqTXBkBpE7WisKHazY89NROKXzfgzIJ2_mI4jD8Lza5jMKN4EseBwl2N0QxJoc8NR-W7B23kskSyQ0PKH7IAIoU17Tfs7Rccg4Tg0M0B82Z-FlxKTYax-U0-aQHHe85KfDLDY6-igMfk-FB0ENlDjINdTzvYVdWgVj7pN_4XqNlNE2ghnjTmrwsC2qi7Vs7V4rSNdpjXh_nxHK-CNYn1Q42Hs_ed5iXiqgy8yqKcOYpAjCCRK7vAfNRXU39S8meU8CzL4TF9FM6FGgXmq6r52uyhBmIXd3IKSp-qkJCyh039S-YbhOEAJuK7QxiziHUP_Hvv4MvT3ZqDEF022Z17nkbErQ9eAZkQFmJDMFk2eGVQFbzmJK5cEFG4h7NF5_0fUiGmt7afhjymodLDUGQINRxSTjLK_SiHJ14naazIOh9-tiP3sk"
        val secretId = "AKIDxYTAvGVNF-mtMoQUg-rCD4E-8r3ViVT5azAAej-fT0wp05rMMNVPlUxl-Qbkddp8"
        val secretKey = "NYq9yCW4JjDMZSMiDqs+lGRyEkWCZDTIN8bM6myTCxQ="

        val uploadSharding = UploadSharding.builder()
            .setSrcFile(srcFile!!)
            .setPartSize(2 * 1024 * 1024) // 设置分片大小
            .setBucket("campus-test-1323116912")
            .setCosPath("exampleobject")
            .setContext(context)
            .setServiceEnvironment(
                "ap-guangzhou",
                object :ServerCredentialProvider() {
                    override fun sessionQCloudCredentials(): SessionQCloudCredentials {
                        //秘钥过期会自动来这里获取新的秘钥

                        return SessionQCloudCredentials(
                            secretId, secretKey, sessionToken,
                            1755591989, 1755600834
                        )
                    }
                }
            )
            .build()

        uploadSharding.upload(onSuccess = { cosXmlRequest, result ->
            runOnUiThread {
                findViewById<TextView>(R.id.tvUrl).text = result.accessUrl
            }
            // 上传成功的回调
            println("Upload Success: ${result.accessUrl}")
        }, onFail = { cosXmlRequest, clientException, serviceException ->
            // 上传失败的回调
            runOnUiThread {
                findViewById<TextView>(R.id.tvUrl).text = "上传失败"
            }
            println("Upload Failed: ${clientException?.message ?: serviceException?.message}")
        }, onProgress = { progress, max ->
            // 上传进度的回调
            //TODO 还需要增加功能
            println("Upload Progress: 上传进度: ${progress.toFloat() / max.toFloat()  * 100 }%")
            runOnUiThread {
                findViewById<TextView>(R.id.tv_progress).text = "上传进度: ${progress.toFloat() / max.toFloat()  * 100 }%"
            }

        })


    }
}