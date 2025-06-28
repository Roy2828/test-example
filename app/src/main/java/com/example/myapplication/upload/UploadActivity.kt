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
import com.example.myapplication.uploadnew.UploadSharding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

/**
 *    desc   :
 *    date   : 2025/6/25 09:30
 *    author : Roy
 *    version: 1.0
 */
class UploadActivity :AppCompatActivity() {

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
        val uploadSharding = UploadSharding.builder()
            .setSrcFile(srcFile!!)
            .setPartSize(2 * 1024 * 1024) // 设置分片大小
            .setBucket("campus-test-1323116912")
            .setCosPath("exampleobject")
            .setContext(context)
            .setServiceEnvironment(
                "ap-guangzhou",
                "tmpSecretId",
                "tmpSecretKey",
                "sessionToken",
                1850838683,
                1556182000L
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
            println("Upload Failed: ${clientException?.message ?: serviceException?.message}")
        }, onProgress = { progress, max ->
            // 上传进度的回调
            println("Upload Progress: $progress / $max")
        })


    }
}