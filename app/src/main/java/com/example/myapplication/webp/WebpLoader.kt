package com.example.myapplication.webp

import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.integration.webp.decoder.WebpFrameCacheStrategy
import com.bumptech.glide.integration.webp.decoder.WebpFrameLoader
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

/**
 * 通用 WebP 静态图与动图加载封装工具类
 *
 * 特性：
 * 1. 统一配置 ARGB_8888 透明通道与全帧缓存策略 (WebpFrameCacheStrategy.ALL)
 * 2. 支持网络 WebP 加载及主/备节点双重自动容灾重试
 * 3. 支持 Assets、File、ResId 等多数据源加载
 * 4. 提供统一的动图播放、暂停、开关与状态查询 API
 * 5. 解耦业务逻辑，可直接复用到其他 Android 项目
 */
object WebpLoader {

    /**
     * WebP 加载结果回调接口
     */
    interface Callback {
        fun onSuccess(drawable: Drawable, isFallback: Boolean) {}
        fun onError(throwable: Throwable?, isFallback: Boolean) {}
    }

    /**
     * 创建默认的 WebP 配置选项 (ARGB_8888 + 全帧缓存)
     */
    fun createDefaultOptions(): RequestOptions {
        return RequestOptions()
            .format(DecodeFormat.PREFER_ARGB_8888)
            .set(WebpFrameLoader.FRAME_CACHE_STRATEGY, WebpFrameCacheStrategy.ALL)
    }

    /**
     * 通用加载方法 (支持 URL, Path, ResId, File, Uri)
     */
    fun load(
        imageView: ImageView,
        model: Any?,
        options: RequestOptions = createDefaultOptions(),
        callback: Callback? = null
    ) {
        if (model == null) return

        GlideApp.with(imageView)
            .load(model)
            .apply(options)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    callback?.onError(e, false)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    callback?.onSuccess(resource, false)
                    return false
                }
            })
            .into(imageView)
    }

    /**
     * 带节点容灾重试的网络 WebP 加载
     *
     * @param imageView 目标 ImageView
     * @param primaryUrl 主网络地址
     * @param fallbackUrl 备用网络地址（主节点失败时自动尝试加载）
     * @param headers 自定义 HTTP 请求头
     * @param options 自定义 RequestOptions
     * @param callback 加载监听回调
     */
    fun loadWithFallback(
        imageView: ImageView,
        primaryUrl: String,
        fallbackUrl: String? = null,
        headers: Map<String, String>? = null,
        options: RequestOptions = createDefaultOptions(),
        callback: Callback? = null
    ) {
        loadUrlInternal(imageView, primaryUrl, fallbackUrl, headers, options, callback, isFallback = false)
    }

    private fun loadUrlInternal(
        imageView: ImageView,
        url: String,
        fallbackUrl: String?,
        headers: Map<String, String>?,
        options: RequestOptions,
        callback: Callback?,
        isFallback: Boolean
    ) {
        val loadModel: Any = if (!headers.isNullOrEmpty()) {
            val lazyHeadersBuilder = LazyHeaders.Builder()
            headers.forEach { (key, value) -> lazyHeadersBuilder.addHeader(key, value) }
            GlideUrl(url, lazyHeadersBuilder.build())
        } else {
            url
        }

        GlideApp.with(imageView)
            .load(loadModel)
            .apply(options)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    if (!isFallback && !fallbackUrl.isNullOrEmpty()) {
                        imageView.post {
                            val context = imageView.context
                            if (context != null) {
                                loadUrlInternal(imageView, fallbackUrl, null, headers, options, callback, isFallback = true)
                            }
                        }
                    } else {
                        callback?.onError(e, isFallback)
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    callback?.onSuccess(resource, isFallback)
                    return false
                }
            })
            .into(imageView)
    }

    /**
     * 加载 Assets 目录下的 WebP 资源
     */
    fun loadAssets(
        imageView: ImageView,
        assetFileName: String,
        options: RequestOptions = createDefaultOptions(),
        callback: Callback? = null
    ) {
        val path = if (assetFileName.startsWith("file:///android_asset/")) {
            assetFileName
        } else {
            "file:///android_asset/$assetFileName"
        }
        load(imageView, path, options, callback)
    }

    // ======================== WebP 动图控制 API ========================

    /**
     * 是否支持动画播放
     */
    fun isAnimatable(imageView: ImageView): Boolean {
        return imageView.drawable is Animatable
    }

    /**
     * 开始/播放动画
     */
    fun startAnimation(imageView: ImageView): Boolean {
        val animatable = imageView.drawable as? Animatable ?: return false
        if (!animatable.isRunning) {
            animatable.start()
        }
        return true
    }

    /**
     * 暂停/停止动画
     */
    fun stopAnimation(imageView: ImageView): Boolean {
        val animatable = imageView.drawable as? Animatable ?: return false
        if (animatable.isRunning) {
            animatable.stop()
        }
        return true
    }

    /**
     * 切换动画播放与暂停状态 (播放 ↔ 暂停)
     * @return 切换后的播放状态 (true: 正在播放, false: 已暂停或不支持)
     */
    fun toggleAnimation(imageView: ImageView): Boolean {
        val animatable = imageView.drawable as? Animatable ?: return false
        return if (animatable.isRunning) {
            animatable.stop()
            false
        } else {
            animatable.start()
            true
        }
    }
}
