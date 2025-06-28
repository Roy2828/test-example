package com.example.myapplication.uploadnew

import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials
import com.tencent.qcloud.core.auth.SessionQCloudCredentials
import com.tencent.qcloud.core.common.QCloudClientException

/**
 *    desc   :
 *    date   : 2025/6/27 09:19
 *    author : Roy
 *    version: 1.0
 */
class ServerCredentialProvider : BasicLifecycleCredentialProvider() {

   var tmpSecretId: String? = null
   var tmpSecretKey: String? = null
   var sessionToken: String? = null
   var expiredTime: Long = 0L
   var startTime: Long = 0L

    @Throws(QCloudClientException::class)
    override fun fetchNewCredentials(): QCloudLifecycleCredentials {
        // 首先从您的临时密钥服务器获取包含了密钥信息的响应
        // 临时密钥生成和使用指引参见https://cloud.tencent.com/document/product/436/14048

        // 然后解析响应，获取密钥信息

      /*  val tmpSecretId = "AKIDZtUMakil_6dyvxYuMal-IjNmVG5mminrfxWORUr5qBm3OD54-4_9LpLynHt3fmEK"
        val tmpSecretKey = "QpvXfO4qRZtAbwmfdkoExsFWNrhNI5nHLIH604lO2S4="
        val sessionToken =
            "o68uXnjbFioF5jjr5OWCE5RvXyEOTVxa0598c6140c144a277058e194f46891c2xqfnCyl1o38U7L2OcpccQEXhm9Y9fKOXrctYF3-JczH-b5cdTu3NFIxY5z8SqEgY-R1P-ftrcUeg_PIcVoeT33oEwdv_NN-M_bqBCaWiuOMLJmr5KgUoX3COxZY92O00PgyXjQd8CJupwHOIYK4KAlD03rY1FFCDoewB-46bmqbBwYsLOCV0OUcT7UK475tzku2rr0B7m6fsrfb3L2UUIUqOV9xueivY8we9qYSD_X3xNpnEYLr91PlN0U6rY99AxwmsP87iN-nTRyS1b4lOaMzLGxh9BiquMuCXpKMbvcovH9jOV8JRZTidXpxb2T0iweApfBJj3qd2zY6lrU7S7JVAmXRbBxoU7AFub4R13xs46VEfjlH8nzfk_EfWgBQEG3yPxuLYgPVwQGBoqj5YAeNWcyzwbREz7xblyGdcbMVzA262VVRJEgMNyGcvTYiz-7Fe7Sew_vjHYmWgve8NopgHtqJWi4CJyxI1h2agE3oGtWFqXiFdBeFOfaKKQVh3TxERcBCtAo3sFeEvymKmLFCdDFQ57Egzrtel5uB2b-eXaJio8VK03QxoohngJXd95wS9GQCB2-bhPQ2t5c5QCTQcoBp74pIeWpPTpjMgYzA"
        val expiredTime: Long = 1850838683 //临时密钥有效截止时间戳，单位是秒

        *//*强烈建议返回服务器时间作为签名的开始时间，用来避免由于用户手机本地时间偏差过大导致的签名不正确 *//*
        // 返回服务器时间作为签名的起始时间
        val startTime = 1556182000L //临时密钥有效起始时间，单位是秒*/

        // 最后返回临时密钥信息对象
        return SessionQCloudCredentials(
            tmpSecretId, tmpSecretKey,
            sessionToken, startTime, expiredTime
        )

    }


    fun setEnvironment(
        tmpSecretId: String?,
        tmpSecretKey: String?,
        sessionToken: String?,
        expiredTime: Long,
        startTime: Long
    ) {
        this.tmpSecretId = tmpSecretId
        this.tmpSecretKey = tmpSecretKey
        this.sessionToken = sessionToken
        this.expiredTime = expiredTime
        this.startTime = startTime
    }
}