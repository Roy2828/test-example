package com.example.myapplication.upload

import android.content.Context
import android.util.Log
import com.tencent.cos.xml.CosXmlService
import com.tencent.cos.xml.CosXmlServiceConfig
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.listener.CosXmlProgressListener
import com.tencent.cos.xml.listener.CosXmlResultListener
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import com.tencent.cos.xml.model.bucket.ListMultiUploadsRequest
import com.tencent.cos.xml.model.bucket.ListMultiUploadsResult
import com.tencent.cos.xml.model.`object`.CompleteMultiUploadRequest
import com.tencent.cos.xml.model.`object`.CompleteMultiUploadResult
import com.tencent.cos.xml.model.`object`.InitMultipartUploadRequest
import com.tencent.cos.xml.model.`object`.InitMultipartUploadResult
import com.tencent.cos.xml.model.`object`.ListPartsRequest
import com.tencent.cos.xml.model.`object`.ListPartsResult
import com.tencent.cos.xml.model.`object`.UploadPartRequest
import com.tencent.cos.xml.model.`object`.UploadPartResult
import com.tencent.cos.xml.model.tag.ListParts
import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials
import com.tencent.qcloud.core.auth.SessionQCloudCredentials
import com.tencent.qcloud.core.common.QCloudClientException
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import kotlin.math.ceil


class MultiPartsUploadObject {
    private var context: Context? = null
    private var cosXmlService: CosXmlService? = null
    private var uploadId: String? = null
    private var srcFile: File? = null
    private val eTags: MutableMap<Int, String> = HashMap()
    private var completeMultiUploadResult: ((String?)->Unit)? = null

    class ServerCredentialProvider : BasicLifecycleCredentialProvider() {
        @Throws(QCloudClientException::class)
        override fun fetchNewCredentials(): QCloudLifecycleCredentials {
            // 首先从您的临时密钥服务器获取包含了密钥信息的响应
            // 临时密钥生成和使用指引参见https://cloud.tencent.com/document/product/436/14048

            // 然后解析响应，获取密钥信息

            val tmpSecretId = "AKIDZtUMakil_6dyvxYuMal-IjNmVG5mminrfxWORUr5qBm3OD54-4_9LpLynHt3fmEK"
            val tmpSecretKey = "QpvXfO4qRZtAbwmfdkoExsFWNrhNI5nHLIH604lO2S4="
            val sessionToken =
                "o68uXnjbFioF5jjr5OWCE5RvXyEOTVxa0598c6140c144a277058e194f46891c2xqfnCyl1o38U7L2OcpccQEXhm9Y9fKOXrctYF3-JczH-b5cdTu3NFIxY5z8SqEgY-R1P-ftrcUeg_PIcVoeT33oEwdv_NN-M_bqBCaWiuOMLJmr5KgUoX3COxZY92O00PgyXjQd8CJupwHOIYK4KAlD03rY1FFCDoewB-46bmqbBwYsLOCV0OUcT7UK475tzku2rr0B7m6fsrfb3L2UUIUqOV9xueivY8we9qYSD_X3xNpnEYLr91PlN0U6rY99AxwmsP87iN-nTRyS1b4lOaMzLGxh9BiquMuCXpKMbvcovH9jOV8JRZTidXpxb2T0iweApfBJj3qd2zY6lrU7S7JVAmXRbBxoU7AFub4R13xs46VEfjlH8nzfk_EfWgBQEG3yPxuLYgPVwQGBoqj5YAeNWcyzwbREz7xblyGdcbMVzA262VVRJEgMNyGcvTYiz-7Fe7Sew_vjHYmWgve8NopgHtqJWi4CJyxI1h2agE3oGtWFqXiFdBeFOfaKKQVh3TxERcBCtAo3sFeEvymKmLFCdDFQ57Egzrtel5uB2b-eXaJio8VK03QxoohngJXd95wS9GQCB2-bhPQ2t5c5QCTQcoBp74pIeWpPTpjMgYzA"
            val expiredTime: Long = 1850838683 //临时密钥有效截止时间戳，单位是秒

            /*强烈建议返回服务器时间作为签名的开始时间，用来避免由于用户手机本地时间偏差过大导致的签名不正确 */
            // 返回服务器时间作为签名的起始时间
            val startTime = 1556182000L //临时密钥有效起始时间，单位是秒

            // 最后返回临时密钥信息对象
            return SessionQCloudCredentials(
                tmpSecretId, tmpSecretKey,
                sessionToken, startTime, expiredTime
            )
        }
    }

    /**
     * 初始化分片上传
     */
    private fun initMultiUpload(result:()->Unit) {
        //.cssg-snippet-body-start:[init-multi-upload]
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
        val bucket = "campus-test-1323116912"
        val cosPath = "exampleobject" //对象在存储桶中的位置标识符，即对象键。 文件名字需要加上后缀  https://campus-test-1323116912.cos.ap-guangzhou.myqcloud.com/exampleobject

        val initMultipartUploadRequest: InitMultipartUploadRequest =
            InitMultipartUploadRequest(bucket, cosPath)
        cosXmlService!!.initMultipartUploadAsync(initMultipartUploadRequest,
            object : CosXmlResultListener {
                override fun onSuccess(cosXmlRequest: CosXmlRequest, result: CosXmlResult) {
                    // 分片上传的 uploadId
                    uploadId = (result as InitMultipartUploadResult)
                        .initMultipartUpload.uploadId
                    result()
                }

                // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
                // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
                override fun onFail(
                    cosXmlRequest: CosXmlRequest,
                    clientException: CosXmlClientException?,
                    serviceException: CosXmlServiceException?
                ) {
                    if (clientException != null) {
                        clientException.printStackTrace()
                    } else {
                        serviceException?.printStackTrace()
                    }
                }
            })

        //.cssg-snippet-body-end
    }

    /**
     * 列出所有未完成的分片上传任务
     */
    private fun listMultiUpload() {
        //.cssg-snippet-body-start:[list-multi-upload]
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
        val bucket = "campus-test-1323116912"
        val listMultiUploadsRequest: ListMultiUploadsRequest =
            ListMultiUploadsRequest(bucket)
        cosXmlService!!.listMultiUploadsAsync(listMultiUploadsRequest,
            object : CosXmlResultListener {
                override fun onSuccess(cosXmlRequest: CosXmlRequest, result: CosXmlResult) {
                    val listMultiUploadsResult: ListMultiUploadsResult =
                        result as ListMultiUploadsResult
                }

                // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
                // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
                override fun onFail(
                    cosXmlRequest: CosXmlRequest,
                    clientException: CosXmlClientException?,
                    serviceException: CosXmlServiceException?
                ) {
                    if (clientException != null) {
                        clientException.printStackTrace()
                    } else {
                        serviceException?.printStackTrace()
                    }
                }
            })

        //.cssg-snippet-body-end
    }

    /**
     * 上传一个分片
     */
    private fun uploadPart(partNumber: Int, fromOffset: Long,to:Long) {
        //.cssg-snippet-body-start:[upload-part]
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
        val bucket = "campus-test-1323116912"
        val cosPath = "exampleobject" //对象在存储桶中的位置标识符，即对象键
        val uploadPartRequest: UploadPartRequest = UploadPartRequest(
            bucket, cosPath,
            partNumber, srcFile!!.path, fromOffset, to, uploadId
        )

        uploadPartRequest.setProgressListener(object : CosXmlProgressListener {
            override fun onProgress(progress: Long, max: Long) {
                // todo Do something to update progress...
            }
        })

        cosXmlService!!.uploadPartAsync(uploadPartRequest, object : CosXmlResultListener {
            override fun onSuccess(cosXmlRequest: CosXmlRequest, result: CosXmlResult) {
                val eTag: String = (result as UploadPartResult).eTag
                eTags[partNumber] = eTag
                Log.e("Roy---","${partNumber}")
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            override fun onFail(
                cosXmlRequest: CosXmlRequest,
                clientException: CosXmlClientException?,
                serviceException: CosXmlServiceException?
            ) {
                if (clientException != null) {
                    clientException.printStackTrace()
                } else {
                    serviceException?.printStackTrace()
                }
            }
        })

        //.cssg-snippet-body-end
    }

    /**
     * 列出已上传的分片
     */
    private fun listParts() {
        //.cssg-snippet-body-start:[list-parts]
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
        val bucket = "campus-test-1323116912"
        val cosPath = "exampleobject" //对象在存储桶中的位置标识符，即对象键。

        val listPartsRequest: ListPartsRequest = ListPartsRequest(
            bucket, cosPath,
            uploadId
        )
        cosXmlService!!.listPartsAsync(listPartsRequest, object : CosXmlResultListener {
            override fun onSuccess(cosXmlRequest: CosXmlRequest, result: CosXmlResult) {
                val listParts: ListParts = (result as ListPartsResult).listParts
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            override fun onFail(
                cosXmlRequest: CosXmlRequest,
                clientException: CosXmlClientException?,
                serviceException: CosXmlServiceException?
            ) {
                if (clientException != null) {
                    clientException.printStackTrace()
                } else {
                    serviceException?.printStackTrace()
                }
            }
        })

        //.cssg-snippet-body-end
    }

    /**
     * 完成分片上传任务
     */
    private fun completeMultiUpload() {
        //.cssg-snippet-body-start:[complete-multi-upload]
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
        val bucket = "campus-test-1323116912"
        val cosPath = "exampleobject" //对象在存储桶中的位置标识符，即对象键。

        val completeMultiUploadRequest: CompleteMultiUploadRequest =
            CompleteMultiUploadRequest(
                bucket,
                cosPath, uploadId, eTags
            )
        cosXmlService!!.completeMultiUploadAsync(completeMultiUploadRequest,
            object : CosXmlResultListener {
                override fun onSuccess(cosXmlRequest: CosXmlRequest, result: CosXmlResult) {
                    val completeMultiUploadResult: CompleteMultiUploadResult =
                        result as CompleteMultiUploadResult
                    this@MultiPartsUploadObject.completeMultiUploadResult?.invoke(completeMultiUploadResult.accessUrl)
                }

                // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
                // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
                override fun onFail(
                    cosXmlRequest: CosXmlRequest,
                    clientException: CosXmlClientException?,
                    serviceException: CosXmlServiceException?
                ) {
                    if (clientException != null) {
                        clientException.printStackTrace()
                    } else {
                        serviceException?.printStackTrace()
                    }
                }
            })

        //.cssg-snippet-body-end
    }

    private fun initService(context: Context) {
        // 存储桶region可以在COS控制台指定存储桶的概览页查看 https://console.cloud.tencent.com/cos5/bucket/ ，关于地域的详情见 https://cloud.tencent.com/document/product/436/6224
        val region = "ap-guangzhou"

        val serviceConfig: CosXmlServiceConfig = CosXmlServiceConfig.Builder()
            .setRegion(region)
            .isHttps(true) // 使用 HTTPS 请求，默认为 HTTP 请求
            .builder()

        this.context = context
        cosXmlService = CosXmlService(
            context, serviceConfig,
            ServerCredentialProvider()
        )
    }


    // .cssg-methods-pragma
    fun testMultiPartsUploadObject(context: Context,completeMultiUploadResult: ((String?)->Unit)? = null) {
        this.completeMultiUploadResult = completeMultiUploadResult
        initService(context)

        // 初始化分片上传
        initMultiUpload{
            // 创建临时文件
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
            val totalSize: Long = srcFile!!.length()
            // 分片数量
            val partCount = ceil(srcFile!!.length() / PART_SIZE.toDouble())
                .toInt()
            val batchSize = PART_SIZE.toLong()
            // 上传分片，下标从1开始
      /*      for (i in 0 until partCount) {

                var partSize = batchSize;
                if (i == partCount - 1) {
                    partSize = totalSize - i * batchSize;
                }
                var from=i * batchSize;
                var to=   partSize + (i * batchSize);


                uploadPart(i, from,to)
            }*/

            for (partNumber in 1..partCount) { // partNumber 从 1 到 partCount
                var currentPartSize = batchSize
                val from = (partNumber - 1) * batchSize // 计算当前分片的起始位置

                if (partNumber == partCount) { // 如果是最后一个分片
                    currentPartSize = totalSize - from // 最后一个分片的大小是总大小减去已处理的大小
                }

                val to = from + currentPartSize

                uploadPart(partNumber, from, to)
            }
        }

        // 列出所有未完成的分片上传任务
      //  listMultiUpload()
        // 列出已上传的分片
     //   listParts()


        // 完成分片上传任务
        object : Thread() {
            override fun run() {
                try {
                    sleep(15000)
                    completeMultiUpload()
                } catch (e: Exception) {
                }
                super.run()
            }
        }.start()


        // .cssg-methods-pragma
    }

    companion object {
        private const val PART_SIZE = 1024 * 1024 // 单个分片大小
    }
}
