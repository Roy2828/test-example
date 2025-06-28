package com.example.myapplication.uploadnew

import android.content.Context
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult

/**
 *    desc   :
 *    date   : 2025/6/28 09:50
 *    author : Roy
 *    version: 1.0
 */
interface IUpload {
   fun startMultiUpload(onProgress: ((progress: Long, max: Long) -> Unit)?=null,
                        onSuccess: ((cosXmlRequest: CosXmlRequest, result: CosXmlResult) -> Unit)?=null,
                        onFail: ((cosXmlRequest: CosXmlRequest,
                                  clientException: CosXmlClientException?,
                                  serviceException: CosXmlServiceException?) -> Unit)?=null)
}