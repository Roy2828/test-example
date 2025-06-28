package com.example.myapplication.uploadnew

import android.app.Application
import android.content.Context

import com.tencent.cos.xml.CosXmlService
import com.tencent.cos.xml.CosXmlServiceConfig

/**
 *    desc   :
 *    date   : 2025/6/28 09:52
 *    author : Roy
 *    version: 1.0
 */
class UploadConfig private constructor() {

    private var context: Application? = null
    private var cosXmlService: CosXmlService? = null
    private val serverCredentialProvider: ServerCredentialProvider by lazy {
        ServerCredentialProvider()
    }

    companion object {
        @Volatile
        private var instance: UploadConfig? = null

        fun getInstance(): UploadConfig {
            return instance ?: synchronized(this) {
                instance ?: UploadConfig().also { instance = it }
            }
        }
    }


    fun init(context: Context?){
        if(context == null) {
            throw IllegalArgumentException("Context cannot be null")
        }
        if(this.context != null) {
            return // 已经初始化过了
        }
        if (context is Application) {
            this.context = context
        } else {
            this.context = context.applicationContext as Application
        }
    }


    //token过期需要重新初始化
    fun initServiceEnvironment(region: String?, tmpSecretId: String?,
        tmpSecretKey: String?,
        sessionToken: String?,
        expiredTime: Long,
        startTime: Long
    ) {
        // 存储桶region可以在COS控制台指定存储桶的概览页查看 https://console.cloud.tencent.com/cos5/bucket/ ，关于地域的详情见 https://cloud.tencent.com/document/product/436/6224
        //val region = "ap-guangzhou"

        val serviceConfig: CosXmlServiceConfig = CosXmlServiceConfig.Builder()
            .setRegion(region)
            .isHttps(true) // 使用 HTTPS 请求，默认为 HTTP 请求
            .builder()

        serverCredentialProvider.setEnvironment(
            tmpSecretId,
            tmpSecretKey,
            sessionToken,
            expiredTime,
            startTime
        )
        cosXmlService = CosXmlService(
            context, serviceConfig,
            serverCredentialProvider
        )
    }


    fun getCosXmlService(): CosXmlService? {
        return cosXmlService
    }

    fun getContext(): Application? {
        return context
    }
}