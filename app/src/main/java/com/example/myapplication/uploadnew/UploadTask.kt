package com.example.myapplication.uploadnew

import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import java.io.File

/**
 *    desc   :
 *    date   : 2025/8/20 09:50
 *    author : Roy
 *    version: 1.0
 */
class UploadTask(
    var id: String,
    var srcFile: File
) {

 private var splitUpload: IUpload? = null


   fun upload(
        onProgress: ((progress: Long, max: Long,uploadTask:UploadTask) -> Unit)? = null,
        onSuccess: ((cosXmlRequest: CosXmlRequest, result: CosXmlResult,uploadTask:UploadTask) -> Unit)? = null,
        onFail: ((
            cosXmlRequest: CosXmlRequest?,
            clientException: CosXmlClientException?,
            serviceException: CosXmlServiceException?,
            uploadTask:UploadTask
        ) -> Unit)? = null
    ) {
        val splitUpload = getSplitUpload()
        splitUpload.startMultiUpload(onProgress, onSuccess, onFail)
    }


    fun cancel(){
        getSplitUpload().cancel()
    }

     private fun getSplitUpload(): IUpload {
        return splitUpload ?: SplitUpload(UploadInit.getInstance().uploadSharding?:throw IllegalStateException("UploadInit is not initialized"),this)
    }

}