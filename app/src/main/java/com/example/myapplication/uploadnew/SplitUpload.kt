package com.example.myapplication.uploadnew

import android.util.Log
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.listener.CosXmlProgressListener
import com.tencent.cos.xml.listener.CosXmlResultListener
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import com.tencent.cos.xml.model.`object`.CompleteMultiUploadRequest
import com.tencent.cos.xml.model.`object`.CompleteMultiUploadResult
import com.tencent.cos.xml.model.`object`.InitMultipartUploadRequest
import com.tencent.cos.xml.model.`object`.InitMultipartUploadResult
import com.tencent.cos.xml.model.`object`.UploadPartRequest
import com.tencent.cos.xml.model.`object`.UploadPartResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.ceil

/**
 *    desc   :
 *    date   : 2025/6/28 10:44
 *    author : Roy
 *    version: 1.0
 */
class SplitUpload constructor(uploadFile: File,partSize: Int,bucket: String,cosPath: String):IUpload ,CoroutineScope by MainScope(){

    private var srcFile: File
    private  var partSize: Int
    private  var bucket: String?=null   // 存储桶名称
    private  var cosPath: String?=null  //对象在存储桶中的位置标识符，即对象键。 文件名字需要加上后缀  https://campus-test-1323116912.cos.ap-guangzhou.myqcloud.com/exampleobject
    private  var uploadId: String? = null

    private var onProgressListener: ((progress: Long, max: Long) -> Unit)?=null    //进度是所有分片同时上传的总进度
    private var onSuccessListener: ((cosXmlRequest: CosXmlRequest, result: CosXmlResult) -> Unit)?=null  //所有分片上传后完成最后请求的回调
    private var onFailListener: ((cosXmlRequest: CosXmlRequest,
                                  clientException: CosXmlClientException?,
                                  serviceException: CosXmlServiceException?) -> Unit)?=null  //所有分片上传后完成最后请求的回调



    init {
        this.srcFile = uploadFile
        this.partSize = partSize
        this.bucket = bucket
        this.cosPath = cosPath
    }


    private val cosXmlService = UploadConfig.getInstance().getCosXmlService()
    private val context = UploadConfig.getInstance().getContext()
    private val eTags: MutableMap<Int, String> = HashMap()
    private var job: Job? = null



    /**
     * 启动分片上传
     */
    override fun startMultiUpload(onProgress: ((progress: Long, max: Long) -> Unit)?,
                                  onSuccess: ((cosXmlRequest: CosXmlRequest, result: CosXmlResult) -> Unit)?,
                                  onFail: ((cosXmlRequest: CosXmlRequest,
                                          clientException: CosXmlClientException?,
                                          serviceException: CosXmlServiceException?) -> Unit)?
    ) {


        val initMultipartUploadRequest: InitMultipartUploadRequest =
            InitMultipartUploadRequest(bucket, cosPath)
        cosXmlService!!.initMultipartUploadAsync(initMultipartUploadRequest,
            object : CosXmlResultListener {
                override fun onSuccess(cosXmlRequest: CosXmlRequest, result: CosXmlResult) {
                    // 分片上传的 uploadId
                    uploadId = (result as InitMultipartUploadResult)
                        .initMultipartUpload.uploadId
                    start(onProgress, onSuccess, onFail) // 开始分片上传
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
                    onFailListener?.invoke(cosXmlRequest, clientException, serviceException)
                }
            })

        //.cssg-snippet-body-end
    }



    /**
     * 分片上传
     */
    private suspend fun splitUpload(onProgress: ((progress: Long, max: Long) -> Unit)?=null,
                            onSuccess: ((cosXmlRequest: CosXmlRequest, result: CosXmlResult) -> Unit)?=null,
                            onFail: ((cosXmlRequest: CosXmlRequest,
                                      clientException: CosXmlClientException?,
                                      serviceException: CosXmlServiceException?) -> Unit)?=null) {
        this.onProgressListener = onProgress
        this.onSuccessListener = onSuccess
        this.onFailListener = onFail

        // 创建临时文件
        context?.let {

            val totalSize: Long = srcFile.length()
            // 分片数量
            val partCount = ceil(srcFile.length() / partSize.toDouble())
                .toInt()
            val batchSize = partSize.toLong()

            for (partNumber in 1..partCount) { // partNumber 从 1 到 partCount
                var currentPartSize = batchSize
                val from = (partNumber - 1) * batchSize // 计算当前分片的起始位置

                if (partNumber == partCount) { // 如果是最后一个分片
                    currentPartSize = totalSize - from // 最后一个分片的大小是总大小减去已处理的大小
                }

                val to = from + currentPartSize

                uploadPart(partNumber, from, to)  //需要等待for循环里面的所有分片全部上传  TODO 需要开启协程
            }

            //TODO  所有分片上传完成后，调用 completeMultiUpload 方法完成上传
            completeMultiUpload()
        }

    }



    private fun start(onProgress: ((progress: Long, max: Long) -> Unit)?=null,
                      onSuccess: ((cosXmlRequest: CosXmlRequest, result: CosXmlResult) -> Unit)?=null,
                      onFail: ((cosXmlRequest: CosXmlRequest,
                                clientException: CosXmlClientException?,
                                serviceException: CosXmlServiceException?) -> Unit)?=null){
        job?.cancel()
        job = launch {
            try {
                if (isActive) {
                    splitUpload(onProgress, onSuccess, onFail) // 开始分片上传
                } else {
                    return@launch
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * 上传一个分片
     */
    private fun uploadPart(partNumber: Int, fromOffset: Long,to:Long) {
        //.cssg-snippet-body-start:[upload-part]
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket

        val uploadPartRequest: UploadPartRequest = UploadPartRequest(
            bucket, cosPath,
            partNumber, srcFile.path, fromOffset, to, uploadId
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
     * 完成分片上传任务
     */
    private fun completeMultiUpload() {

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
                    onSuccessListener?.invoke(cosXmlRequest, result)
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

                    onFailListener?.invoke(cosXmlRequest, clientException, serviceException)
                }
            })

        //.cssg-snippet-body-end
    }

}


