package com.example.myapplication.uploadnew

import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.listener.CosXmlResultListener
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import com.tencent.cos.xml.model.`object`.CompleteMultiUploadRequest
import com.tencent.cos.xml.model.`object`.CompleteMultiUploadResult
import com.tencent.cos.xml.model.`object`.InitMultipartUploadRequest
import com.tencent.cos.xml.model.`object`.InitMultipartUploadResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 *    desc   :
 *    date   : 2025/6/28 10:44
 *    author : Roy
 *    version: 1.0
 */
class SplitUpload(private val uploadSharding: UploadSharding, private val uploadTask: UploadTask):IUpload ,CoroutineScope by MainScope(){

    private  var uploadId: String? = null

    private var onProgressListener: ((progress: Long, max: Long,uploadTask:UploadTask) -> Unit)?=null    //进度是所有分片同时上传的总进度
    private var onSuccessListener: ((cosXmlRequest: CosXmlRequest, result: CosXmlResult,uploadTask:UploadTask) -> Unit)?=null  //所有分片上传后完成最后请求的回调
    private var onFailListener: ((cosXmlRequest: CosXmlRequest?,
                                  clientException: CosXmlClientException?,
                                  serviceException: CosXmlServiceException?,uploadTask:UploadTask) -> Unit)?=null  //所有分片上传后完成最后请求的回调



    private val cosXmlService = UploadConfig.getInstance().getCosXmlService()
    private val context = UploadConfig.getInstance().getContext()

    private var job: Job? = null

    private var split: Split? = null



    /**
     * 启动分片上传
     */
    override fun startMultiUpload(onProgress: ((progress: Long, max: Long,uploadTask:UploadTask) -> Unit)?,
                                  onSuccess: ((cosXmlRequest: CosXmlRequest, result: CosXmlResult,uploadTask:UploadTask) -> Unit)?,
                                  onFail: ((cosXmlRequest: CosXmlRequest?,
                                          clientException: CosXmlClientException?,
                                          serviceException: CosXmlServiceException?,uploadTask:UploadTask) -> Unit)?
    ) {

        this.onProgressListener = onProgress
        this.onSuccessListener = onSuccess
        this.onFailListener = onFail


        val initMultipartUploadRequest: InitMultipartUploadRequest =
            InitMultipartUploadRequest(uploadSharding.bucket, uploadSharding.cosPath)
        cosXmlService!!.initMultipartUploadAsync(initMultipartUploadRequest,
            object : CosXmlResultListener {
                override fun onSuccess(cosXmlRequest: CosXmlRequest, result: CosXmlResult) {
                    // 分片上传的 uploadId
                    uploadId = (result as InitMultipartUploadResult)
                        .initMultipartUpload.uploadId
                    start(onProgress) // 开始分片上传
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
                    onFailListener?.invoke(cosXmlRequest, clientException, serviceException,uploadTask)
                }
            })

        //.cssg-snippet-body-end
    }







    private fun start(onProgress: ((progress: Long, max: Long,uploadTask:UploadTask) -> Unit)?=null){
        cancel()
        job = launch {
            try {
                if (isActive) {
                    splitUpload(onProgress) // 开始分片上传
                } else {
                    return@launch
                }
            } catch (e: Exception) {
                e.printStackTrace()
                split?.cancelAllSplitUploads()
            }
        }
    }



    private suspend fun splitUpload(onProgress: ((progress: Long, max: Long,uploadTask:UploadTask) -> Unit)?=null){
        split = Split(uploadSharding,uploadTask)
        split!!.splitUpload(uploadId,onProgress, completeMultiUpload = {
                // 所有分片上传完成后，调用 completeMultiUpload 方法来完成分片上传任务
                completeMultiUpload(it)
            }, errorMultiUpload = {
                onFailListener?.invoke(null,null,null,uploadTask)
            })
    }



    /**
     * 完成分片上传任务
     */
    private fun completeMultiUpload(eTags: MutableMap<Int, String> ) {

        val completeMultiUploadRequest: CompleteMultiUploadRequest =
            CompleteMultiUploadRequest(
                uploadSharding.bucket,
                uploadSharding.cosPath, uploadId, eTags
            )
        cosXmlService!!.completeMultiUploadAsync(completeMultiUploadRequest,
            object : CosXmlResultListener {
                override fun onSuccess(cosXmlRequest: CosXmlRequest, result: CosXmlResult) {
                    val completeMultiUploadResult: CompleteMultiUploadResult =
                        result as CompleteMultiUploadResult
                    onSuccessListener?.invoke(cosXmlRequest, result,uploadTask)
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

                    onFailListener?.invoke(cosXmlRequest, clientException, serviceException,uploadTask)
                }
            })

        //.cssg-snippet-body-end
    }



    override fun cancel(){
        job?.cancel()
    }

}


