package com.example.myapplication.webp

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.integration.webp.WebpGlideLibraryModule
import com.bumptech.glide.load.engine.executor.GlideExecutor
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.OkHttpClient
import java.io.InputStream
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * 自定义 GlideModule：
 * 1. 注册 OkHttpUrlLoader 替代默认 HttpUrlFetcher 并配置 SSL 证书放行 (兼容老旧测试设备/模拟器)
 * 2. 注册 com.github.zjupure:webpdecoder 官方组件 (WebpGlideLibraryModule)
 * 3. 拦截并过滤 StrictMode$StrictModeViolation 异常，彻底消除控制台 "GlideExecutor caught uncaught throwable" 日志输出
 */
@GlideModule
class MyAppGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val uncaughtThrowableStrategy = GlideExecutor.UncaughtThrowableStrategy { t ->
            if (t != null && !isStrictModeViolation(t)) {
                Log.e("MyAppGlideModule", "GlideExecutor caught uncaught throwable", t)
            }
        }

        val sourceExecutor = GlideExecutor.newSourceBuilder()
            .setUncaughtThrowableStrategy(uncaughtThrowableStrategy)
            .build()

        val diskCacheExecutor = GlideExecutor.newSourceBuilder()
            .setName("disk-cache")
            .setThreadCount(1)
            .setUncaughtThrowableStrategy(uncaughtThrowableStrategy)
            .build()

        val animationThreadCount = if (GlideExecutor.calculateBestThreadCount() >= 4) 2 else 1
        val animationExecutor = GlideExecutor.newSourceBuilder()
            .setName("animation")
            .setThreadCount(animationThreadCount)
            .setUncaughtThrowableStrategy(uncaughtThrowableStrategy)
            .build()

        builder.setSourceExecutor(sourceExecutor)
        builder.setDiskCacheExecutor(diskCacheExecutor)
        builder.setAnimationExecutor(animationExecutor)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        // 注册 OkHttp3 引擎以获得长超时 (15s)、连接池及 SSL 证书容灾能力
        val okHttpClient = getUnsafeOkHttpClient()
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(okHttpClient))

        // 按照 webpdecoder 官方规范手动注册 WebpGlideLibraryModule 解码组件
        WebpGlideLibraryModule().registerComponents(context, glide, registry)
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        } catch (e: Exception) {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    private fun isStrictModeViolation(throwable: Throwable?): Boolean {
        var current: Throwable? = throwable
        while (current != null) {
            val className = current.javaClass.name
            val msg = current.message ?: ""
            if (className.contains("StrictMode") || className.contains("BlockGuard") || msg.contains("StrictMode")) {
                return true
            }
            for (suppressed in current.suppressed) {
                if (isStrictModeViolation(suppressed)) {
                    return true
                }
            }
            current = current.cause
        }
        return false
    }
}



