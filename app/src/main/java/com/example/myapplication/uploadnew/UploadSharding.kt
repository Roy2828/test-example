package com.example.myapplication.uploadnew


import android.content.Context
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import java.io.File

/**
 *    desc   :
 *    date   : 2025/6/27 09:19
 *    author : Roy
 *    version: 1.0
 */
class UploadSharding private constructor(
    private val srcFile: File?,
    private val partSize: Int,
    private val bucket: String?,
    private val cosPath: String?,
    private val context: Context?,
    private val region: String?,
    private val tmpSecretId: String?,
    private val tmpSecretKey: String?,
    private val sessionToken: String?,
    private val expiredTime: Long,
    private val startTime: Long
) {

    companion object {
        private const val DEFAULT_PART_SIZE = 1024 * 1024 // 单个分片大小

        fun builder(): Builder {
            return Builder()
        }
    }

    private val splitUpload: IUpload? = null

    class Builder {
        private var srcFile: File? = null
        private var partSize: Int = DEFAULT_PART_SIZE
        private var bucket: String? = null
        private var cosPath: String? = null
        private var context: Context? = null
        private var region: String? = null
        private var tmpSecretId: String? = null
        private var tmpSecretKey: String? = null
        private var sessionToken: String? = null
        private var expiredTime: Long = 0
        private var startTime: Long = 0

        fun setSrcFile(srcFile: File): Builder {
            this.srcFile = srcFile
            return this
        }

        fun setPartSize(partSize: Int): Builder {
            if (partSize > DEFAULT_PART_SIZE) {
                this.partSize = partSize
            }
            return this
        }

        fun setBucket(bucket: String): Builder {
            this.bucket = bucket
            return this
        }

        fun setCosPath(cosPath: String): Builder {
            this.cosPath = cosPath
            return this
        }

        fun setContext(context: Context): Builder {
            this.context = context
            return this
        }

        fun setServiceEnvironment(
            region: String?,
            tmpSecretId: String?,
            tmpSecretKey: String?,
            sessionToken: String?,
            expiredTime: Long,
            startTime: Long
        ): Builder {
            this.region = region
            this.tmpSecretId = tmpSecretId
            this.tmpSecretKey = tmpSecretKey
            this.sessionToken = sessionToken
            this.expiredTime = expiredTime
            this.startTime = startTime
            return this
        }

        fun build(): UploadSharding {
            return UploadSharding(
                srcFile,
                partSize,
                bucket,
                cosPath,
                context,
                region,
                tmpSecretId,
                tmpSecretKey,
                sessionToken,
                expiredTime,
                startTime
            ).apply {
                initializeServiceEnvironment()
            }
        }
    }


    fun upload(
        onProgress: ((progress: Long, max: Long) -> Unit)? = null,
        onSuccess: ((cosXmlRequest: CosXmlRequest, result: CosXmlResult) -> Unit)? = null,
        onFail: ((
            cosXmlRequest: CosXmlRequest,
            clientException: CosXmlClientException?,
            serviceException: CosXmlServiceException?
        ) -> Unit)? = null
    ) {
        val splitUpload = getSplitUpload()
        splitUpload.startMultiUpload(onProgress, onSuccess, onFail)
    }

    private fun getSplitUpload(): IUpload {
        return splitUpload ?: SplitUpload(srcFile!!, partSize, bucket!!, cosPath!!)
    }

    private fun initializeServiceEnvironment() {
        UploadConfig.getInstance().init(context)
        UploadConfig.getInstance().initServiceEnvironment(
            region, tmpSecretId, tmpSecretKey, sessionToken, expiredTime, startTime
        )
    }

}