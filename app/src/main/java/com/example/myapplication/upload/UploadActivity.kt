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

        val sessionToken = "3HPamzCP0EGVPLaZmrFlLu4cuJt5Q9Ra97d6fdf8481451edfb2225b30a515d2f3fQoA71gWNwMFQHYUcl0s5b9hcNJOz5o4lIaj52AWucSV_B-8SUTn9_vlVFrAtp9mmU4btpwKP68oI3rKqFF0x7vVgiBTqZFT9CM4YlSOgj2jOrnGQ-SLwX3E1QUmRGmmEK1Mz5hYl_4XTKU-VBKQd1HYg-bHk8GU3LqrA4CaDahMzANhvVBqbCsv1T6TUul_dXTcA6-HJtoXlmaKQVcnVltlItp1ZHCebylzTsbpZMFJQJnQgFxym8K3RZfu60eVLWFtUQulORW54_XA_a1W0uh8DUnhH_9bsttFRubAUKaHnIj0_kb0L8n_nK64AF01jluHAwlZ8eG-W87lsbP4lrEk9mKo8VrONDPKY-RBA8nkATSuS6GdwZwDKdEjV8cwSTWwEbTc3myMx--Pww9euTTYZIEPrNH9D5DuB3l-u2I_pRe5bsnRifVcwKY6WS52S6ndhoExFuxgsUPPsZGLdVhnKyFxDKYUO7Pji8ow4J1Il4TaAyD4fbNMTXQSq6UgP9YKe-2eXlOIE4HNnmI70eXtsaMecrgjaiUhS-W0aUcHLqQgxQDCX5fZxDwIKx92vjRgfVC8FKpgkfpx0ukBsWcME7IfH1YJNDcjB7JxHo"
        val secretId = "AKID3L9yQf8wWPjN7C9fwOS5udtiIz-zFBY5oPGdXqVigE141ZJczbzYsnI627Wcglzf"
        val secretKey = "yeY4bZHXQqn33nyANmFikmOJtJW+kW29nqv8/mg3OgI="

        val uploadSharding = UploadSharding.builder()
            .setSrcFile(srcFile!!)
            .setPartSize(2 * 1024 * 1024) // 设置分片大小
            .setBucket("campus-test-1323116912")
            .setCosPath("exampleobject")
            .setContext(context)
            .setServiceEnvironment(
                "ap-guangzhou",
                secretId,
                secretKey,
                sessionToken,
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
            runOnUiThread {
                findViewById<TextView>(R.id.tvUrl).text = "上传失败"
            }
            println("Upload Failed: ${clientException?.message ?: serviceException?.message}")
        }, onProgress = { progress, max ->
            // 上传进度的回调
            //TODO 还需要增加功能
            println("Upload Progress: $progress / $max")
        })


    }
}