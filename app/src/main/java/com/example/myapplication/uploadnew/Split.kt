package com.example.myapplication.uploadnew

import android.util.Log
import android.util.SparseArray
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ceil

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2025/6/28 16:11
 *    author : Roy
 *    version: 1.0
 */
class Split constructor(private val uploadSharding: UploadSharding) {

    private val eTags: MutableMap<Int, String> = HashMap()


    private var onProgressListener: ((progress: Long, max: Long) -> Unit)? =
        null    //进度是所有分片同时上传的总进度

    private val countDownLatch = CountDownLatch(1)  //用于阻塞主线程

    private val context = UploadConfig.getInstance().getContext()

    private val atomicIntegerSuccess: AtomicInteger = AtomicInteger(0);
    private val atomicIntegerError: AtomicInteger = AtomicInteger(0);

    private val recordAllRequestsCompleted = AtomicInteger(0) //记录所有请求是否都完成不管成功失败

    private val requestSplitUploads :SparseArray<RequestSplitUpload> = SparseArray<RequestSplitUpload>() //存储每个分片的上传请求

    private fun reset() {
        atomicIntegerSuccess.getAndSet(0) //重置
        atomicIntegerError.getAndSet(0) //重置
        recordAllRequestsCompleted.getAndSet(0)
    }

    private fun successCount() {
        atomicIntegerSuccess.incrementAndGet()  //先自增再获取当前值
    }

    private fun errorCount() {
        atomicIntegerError.incrementAndGet()  //先自增再获取当前值
    }

    /**
     * 分片上传
     */
    suspend fun splitUpload(
        uploadId: String?,
        onProgress: ((progress: Long, max: Long) -> Unit)? = null,
        completeMultiUpload: (MutableMap<Int, String>) -> Unit,
        errorMultiUpload: () -> Unit
    ) {
        this.onProgressListener = onProgress
        reset()
        // 创建临时文件
        context?.let {

            val totalSize: Long = uploadSharding.srcFile.length()
            // 分片数量
            val partCount =
                ceil(uploadSharding.srcFile.length() / uploadSharding.partSize.toDouble())
                    .toInt()
            val batchSize = uploadSharding.partSize.toLong()

            recordAllRequestsCompleted.getAndSet(partCount)//初始化记录

            for (partNumber in 1..partCount) { // partNumber 从 1 到 partCount
                var currentPartSize = batchSize
                val from = (partNumber - 1) * batchSize // 计算当前分片的起始位置

                if (partNumber == partCount) { // 如果是最后一个分片
                    currentPartSize = totalSize - from // 最后一个分片的大小是总大小减去已处理的大小
                }

                val to = from + currentPartSize

                uploadPart(partNumber, from, to,uploadId)  //需要等待for循环里面的所有分片全部上传  TODO 需要开启协程
            }

            await(waitEnd = {
                //TODO  所有分片上传完成后，调用 completeMultiUpload 方法完成上传
                if (atomicIntegerSuccess.get() == partCount) {
                    completeMultiUpload(eTags) //TODO eTags需要进行处理
                }else{
                    errorMultiUpload()
                }
            }, waitError = {
                errorMultiUpload()
            })

        }

    }


    fun await(waitEnd: () -> Unit, waitError: () -> Unit) {
        try {
            countDownLatch.await()
            waitEnd()
        } catch (e: Exception) {
            e.printStackTrace()
            waitError()
        }
        //都执行完毕了
        Log.e("roy", "初始化完毕")
    }

    private fun uploadPart(partNumber: Int, fromOffset: Long, to: Long,  uploadId: String?,) {
       val requestUpload =   RequestSplitUpload(
            uploadSharding,
            eTags,
            onSuccessListener = { cosXmlRequest, result ->
                successCount()
                outcome()
            },
            onFailListener = { cosXmlRequest, clientException, serviceException ->
                errorCount()
                outcome()
            })
        requestUpload.uploadPartRequest(partNumber, fromOffset, to,uploadId)
        requestSplitUploads.put(partNumber,requestUpload) //存储每个分片的上传请求
    }


    private fun outcome() {
        val count = recordAllRequestsCompleted.decrementAndGet()  //先自减再获取当前值
        if (count == 0) {
            countDownLatch.countDown()
        }
    }


    //取消当前文件上传的所有分片上传请求
      fun cancelAllSplitUploads() {
        for (i in 0 until requestSplitUploads.size()) {
            val requestSplitUpload = requestSplitUploads.valueAt(i)
            requestSplitUpload.cancelRequest()
        }
        requestSplitUploads.clear() //清空所有请求
    }


}