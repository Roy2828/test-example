package com.example.myapplication.uploadnew

import android.util.Log
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.listener.CosXmlProgressListener
import com.tencent.cos.xml.listener.CosXmlResultListener
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import com.tencent.cos.xml.model.`object`.UploadPartRequest
import com.tencent.cos.xml.model.`object`.UploadPartResult
import java.util.concurrent.atomic.AtomicInteger

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2025/6/28 15:55
 *    author : Roy
 *    version: 1.0
 */
class RequestSplitUpload constructor(
    private val uploadSharding: UploadSharding,
    private val eTags: MutableMap<Int, String>,
    private val onSuccessListener:((cosXmlRequest: CosXmlRequest, result: CosXmlResult)->Unit)?=null,
    private val onFailListener:((cosXmlRequest: CosXmlRequest,
                     clientException: CosXmlClientException?,
                     serviceException: CosXmlServiceException?)->Unit)?=null
) {


    private val cosXmlService = UploadConfig.getInstance().getCosXmlService()

    private  var uploadPartRequest: UploadPartRequest?=null

    private var retryCount = 3



    private  var atomicIntegerErrorCount: AtomicInteger = AtomicInteger(0);

    /**
     * 上传一个分片
     */

    fun uploadPartRequest(partNumber: Int, fromOffset: Long, to: Long,  uploadId: String?,){
        atomicIntegerErrorCount.getAndSet(0)
        uploadPart(partNumber, fromOffset, to,uploadId)
    }

    private fun uploadPart(partNumber: Int, fromOffset: Long, to: Long,  uploadId: String?,) {
        //.cssg-snippet-body-start:[upload-part]
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket

          uploadPartRequest   = UploadPartRequest(
            uploadSharding.bucket, uploadSharding.cosPath,
            partNumber, uploadSharding.srcFile.path, fromOffset, to, uploadId
        )

        uploadPartRequest?.setProgressListener(object : CosXmlProgressListener {
            override fun onProgress(progress: Long, max: Long) {
                // todo Do something to update progress...
            }
        })

        cosXmlService?.uploadPartAsync(uploadPartRequest, object : CosXmlResultListener {
            override fun onSuccess(cosXmlRequest: CosXmlRequest, result: CosXmlResult) {
                val eTag: String = (result as UploadPartResult).eTag
                eTags[partNumber] = eTag
                Log.e("Roy---", "${partNumber}")
                reset()
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

               val errorCount = atomicIntegerErrorCount.incrementAndGet() //相当于 ++i

                if(errorCount < retryCount){
                    uploadPart(partNumber, fromOffset, to,uploadId) // 重试上传分片
                }else{
                    //失败回调
                    onFailListener?.invoke(cosXmlRequest, clientException, serviceException)
                }



            }
        })

        //.cssg-snippet-body-end
    }


    fun reset(){
        atomicIntegerErrorCount.getAndSet(0) //重置 //获取旧值设置新值
    }

    fun cancelRequest(){
        uploadPartRequest?.apply {
            cosXmlService?.cancel(this)
        }

    }



}