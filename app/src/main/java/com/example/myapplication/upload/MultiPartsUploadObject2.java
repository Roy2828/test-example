package com.example.myapplication.upload;


import android.content.Context;

import androidx.annotation.Nullable;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.bucket.ListMultiUploadsRequest;
import com.tencent.cos.xml.model.bucket.ListMultiUploadsResult;
import com.tencent.cos.xml.model.object.CompleteMultiUploadRequest;
import com.tencent.cos.xml.model.object.CompleteMultiUploadResult;
import com.tencent.cos.xml.model.object.InitMultipartUploadRequest;
import com.tencent.cos.xml.model.object.InitMultipartUploadResult;
import com.tencent.cos.xml.model.object.ListPartsRequest;
import com.tencent.cos.xml.model.object.ListPartsResult;
import com.tencent.cos.xml.model.object.UploadPartRequest;
import com.tencent.cos.xml.model.object.UploadPartResult;
import com.tencent.cos.xml.model.tag.ListParts;
import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;
import com.tencent.qcloud.core.common.QCloudClientException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;


public class MultiPartsUploadObject2 {

    private static final int PART_SIZE = 1024 * 1024; // 单个分片大小

    private Context context;
    private CosXmlService cosXmlService;
    private String uploadId;
    private File srcFile;
    private Map<Integer, String> eTags = new HashMap<>();

    public static class ServerCredentialProvider extends BasicLifecycleCredentialProvider {

        @Override
        protected QCloudLifecycleCredentials fetchNewCredentials() throws QCloudClientException {

            // 首先从您的临时密钥服务器获取包含了密钥信息的响应
			// 临时密钥生成和使用指引参见https://cloud.tencent.com/document/product/436/14048

            // 然后解析响应，获取密钥信息
            String tmpSecretId = "AKIDPiSp-CIrPQKsAqViGPZrNxGlYUslhCvaa_lGGUdtIxaNCri5u9ukDL6oSdgYc9Qm";
            String tmpSecretKey = "nPnFVtG8/IpCcVAUwPVfzr/2TLqnOk08biS5QIwr5SQ=";
            String sessionToken = "oNlk4wjr2R5xcHCTtNRdI2WpOzhPUJIab5008100f4b049866312b98173fe99c1C0wMcpm-Egb4vGbK_7JE9bBwRcV2i_ZWP6JgyhgEVRr30k9rQlz1ghJqy1tMCDmyCO4_gP55ewnMR7Cde44yaEQupcbPp7nzvCrFkmiDcCUzx_vak2xQQ30ur1RSm_gH9Jeqegty4RMREtrly-beD69lqnJVXUtHw8r-tLsU6NKpZh9EtGt6J5W-h-sUweuU8AZUSHdYsxAJ-7N4-aEIBcZLz7B1EEuFm4iSow_Vq5LS9-aQk4JVgXqVMwcKutyslh5KtmhRMWr_T9brMIhqcFXcj3EdAKwv2eIu09qIDjiSaZ5GQ5wi6Z_L5hs-lIRhJix-odqfg4DRfAFeRK_sLcYYRZbCYlCLNW0xtXqmbwxEsLQOVMGhHePowWSpwt9SLdjV9ZGTMauMypINLquuArLCcceDNtdUX5cMSMlQlzfpNRvRrO-A85-X5kXJtqsIXA1CXxqMhew7J1u4hdu6RTRH-CAUOhJVTYQxWnDjfpLiXyjwK6L1By5BHGOy85llfAtpUZpDIqXfeRgGjCGF9ypcNNZGg6TVNhmrg8PpsLTFAu3JT_z32R51PuN1StGBVqSFYhE6foNA_9LIEWYN37XVdE57c9z6WlPJmlpcE-c";
            long expiredTime = 1850838683;//临时密钥有效截止时间戳，单位是秒

            /*强烈建议返回服务器时间作为签名的开始时间，用来避免由于用户手机本地时间偏差过大导致的签名不正确 */
            // 返回服务器时间作为签名的起始时间
            long startTime = 1556182000L; //临时密钥有效起始时间，单位是秒

            // 最后返回临时密钥信息对象
            return new SessionQCloudCredentials(tmpSecretId, tmpSecretKey,
                    sessionToken, startTime, expiredTime);
        }
    }

    /**
     * 初始化分片上传
     */
    private void initMultiUpload() {
        //.cssg-snippet-body-start:[init-multi-upload]
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
		String bucket = "campus-test-1323116912";
        String cosPath = "exampleobject"; //对象在存储桶中的位置标识符，即对象键。

        InitMultipartUploadRequest initMultipartUploadRequest =
                new InitMultipartUploadRequest(bucket, cosPath);
        cosXmlService.initMultipartUploadAsync(initMultipartUploadRequest,
                new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult result) {
                // 分片上传的 uploadId
                uploadId = ((InitMultipartUploadResult) result)
                        .initMultipartUpload.uploadId;
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest cosXmlRequest,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                } else {
                    serviceException.printStackTrace();
                }
            }
        });

        //.cssg-snippet-body-end
    }

    /**
     * 列出所有未完成的分片上传任务
     */
    private void listMultiUpload() {
        //.cssg-snippet-body-start:[list-multi-upload]
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
		String bucket = "campus-test-1323116912";
        ListMultiUploadsRequest listMultiUploadsRequest =
                new ListMultiUploadsRequest(bucket);
        cosXmlService.listMultiUploadsAsync(listMultiUploadsRequest,
                new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult result) {
                ListMultiUploadsResult listMultiUploadsResult =
                        (ListMultiUploadsResult) result;
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest cosXmlRequest,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                } else {
                    serviceException.printStackTrace();
                }
            }
        });

        //.cssg-snippet-body-end
    }

    /**
     * 上传一个分片
     */
    private void uploadPart(final int partNumber, final int offset) {
        //.cssg-snippet-body-start:[upload-part]
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
		String bucket = "campus-test-1323116912";
        String cosPath = "exampleobject"; //对象在存储桶中的位置标识符，即对象键
        UploadPartRequest uploadPartRequest = new UploadPartRequest(bucket, cosPath,
                partNumber, srcFile.getPath(), offset, PART_SIZE, uploadId);

        uploadPartRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                // todo Do something to update progress...
            }
        });

        cosXmlService.uploadPartAsync(uploadPartRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult result) {
                String eTag = ((UploadPartResult) result).eTag;
                eTags.put(partNumber, eTag);
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest cosXmlRequest,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                } else {
                    serviceException.printStackTrace();
                }
            }
        });

        //.cssg-snippet-body-end
    }

    /**
     * 列出已上传的分片
     */
    private void listParts() {
        //.cssg-snippet-body-start:[list-parts]
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
		String bucket = "campus-test-1323116912";
        String cosPath = "exampleobject"; //对象在存储桶中的位置标识符，即对象键。

        ListPartsRequest listPartsRequest = new ListPartsRequest(bucket, cosPath,
                uploadId);
        cosXmlService.listPartsAsync(listPartsRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult result) {
                ListParts listParts = ((ListPartsResult) result).listParts;
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest cosXmlRequest,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                } else {
                    serviceException.printStackTrace();
                }
            }
        });

        //.cssg-snippet-body-end
    }

    /**
     * 完成分片上传任务
     */
    private void completeMultiUpload() {
        //.cssg-snippet-body-start:[complete-multi-upload]
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
		String bucket = "campus-test-1323116912";
        String cosPath = "exampleobject"; //对象在存储桶中的位置标识符，即对象键。

        CompleteMultiUploadRequest completeMultiUploadRequest =
                new CompleteMultiUploadRequest(bucket,
                cosPath, uploadId, eTags);
        cosXmlService.completeMultiUploadAsync(completeMultiUploadRequest,
                new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult result) {
                CompleteMultiUploadResult completeMultiUploadResult =
                        (CompleteMultiUploadResult) result;
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest cosXmlRequest,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                } else {
                    serviceException.printStackTrace();
                }
            }
        });

        //.cssg-snippet-body-end
    }

    private void initService(Context context) {
        // 存储桶region可以在COS控制台指定存储桶的概览页查看 https://console.cloud.tencent.com/cos5/bucket/ ，关于地域的详情见 https://cloud.tencent.com/document/product/436/6224
        String region = "ap-guangzhou";

        CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .setRegion(region)
                .isHttps(true) // 使用 HTTPS 请求，默认为 HTTP 请求
                .builder();

        this.context = context;
        cosXmlService = new CosXmlService(context, serviceConfig,
                new ServerCredentialProvider());
    }
    // .cssg-methods-pragma


    public void testMultiPartsUploadObject(Context context) {
        initService(context);

        // 初始化分片上传
        initMultiUpload();

        // 列出所有未完成的分片上传任务
        listMultiUpload();

        // 创建临时文件
        try {
            srcFile = new File(context.getCacheDir(), "exampleobject");
            if (!srcFile.exists() && srcFile.createNewFile()) {
                RandomAccessFile raf = new RandomAccessFile(srcFile, "rw");
                raf.setLength(3000000);
                raf.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 分片数量
        int partCount = (int) Math.ceil(srcFile.length() / (double) PART_SIZE);
        // 上传分片，下标从1开始
        for (int i = 1; i < partCount + 1; i++) {
            uploadPart(i, (partCount - 1) * PART_SIZE);
        }

        // 列出已上传的分片
        listParts();


        // 完成分片上传任务

        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(15000);
                    completeMultiUpload();
                } catch (Exception e) {

                }
                super.run();
            }
        }.start();


        // .cssg-methods-pragma

    }
}
