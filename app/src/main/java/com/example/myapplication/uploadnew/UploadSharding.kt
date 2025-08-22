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
    val partSize: Int,
    val bucket: String,   // 存储桶名称
    val cosPath: String,   //对象在存储桶中的位置标识符，即对象键。 文件名字需要加上后缀  https://campus-test-1323116912.cos.ap-guangzhou.myqcloud.com/exampleobject
    val context: Context?,
    val region: String?,
    val credentialProvider: ServerCredentialProvider // 临时密钥提供者
) {

    companion object {
        private const val DEFAULT_PART_SIZE = 1024 * 1024 // 单个分片大小

        fun builder(): Builder {
            return Builder()
        }
    }

    private val splitUpload: IUpload? = null

    class Builder {
        private var partSize: Int = DEFAULT_PART_SIZE
        private var bucket: String = ""
        private var cosPath: String = ""
        private var context: Context? = null
        private var region: String? = null
        private lateinit var credentialProvider: ServerCredentialProvider




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
            credentialProvider: ServerCredentialProvider,
        ): Builder {
            this.region = region
            this.credentialProvider = credentialProvider

            return this
        }

        fun build(): UploadSharding {
            return UploadSharding(
                partSize,
                bucket,
                cosPath,
                context,
                region,
                credentialProvider
            ).apply {
                initializeServiceEnvironment()
            }
        }
    }



    private fun initializeServiceEnvironment() {
        UploadConfig.getInstance().init(context)
        UploadConfig.getInstance().initServiceEnvironment(
            region, credentialProvider
        )
    }

}