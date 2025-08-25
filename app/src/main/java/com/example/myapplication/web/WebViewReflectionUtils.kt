package com.example.myapplication.web

/**
 *    desc   :
 *    date   : 2025/8/23 10:51
 *    author : Roy
 *    version: 1.0
 */
import android.webkit.WebView
import java.lang.reflect.Field

object WebViewReflectionUtils {

    /**
     * 检查 WebView 的 AwContents 是否已被销毁
     * @param webView 要检查的 WebView 实例
     * @return true 如果已销毁，false 如果未销毁或无法确定
     */
    fun isWebViewDestroyed(webView: WebView): Boolean {
        return try {
            // 获取 WebView 的 mProvider 字段
            val providerField = getField(WebView::class.java, "mProvider")
            val provider = providerField?.get(webView)

            // 获取 provider 的 mAwContents 字段
            val awContentsField = getField(provider?.javaClass, "mAwContents")
            val awContents = awContentsField?.get(provider)

            // 获取 AwContents 的 mIsDestroyed 字段
            val isDestroyedField = getField(awContents?.javaClass, "mIsDestroyed")
            isDestroyedField?.get(awContents) as? Boolean ?: false
        } catch (e: Exception) {
            // 反射失败，返回默认值
            e.printStackTrace()
            false
        }
    }

    /**
     * 安全地获取类的字段（包括私有字段）
     */
    private fun getField(clazz: Class<*>?, fieldName: String): Field? {
        if (clazz == null) return null

        return try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            field
        } catch (e: NoSuchFieldException) {
            // 尝试从父类查找
            val superClass = clazz.superclass
            if (superClass != null && superClass != Any::class.java) {
                getField(superClass, fieldName)
            } else {
                null
            }
        }
    }

    /**
     * 强制设置 AwContents 的 mIsDestroyed 为 false
     * 注意：这可能会带来风险，请谨慎使用
     */
    fun resetWebViewDestroyedFlag(webView: WebView): Boolean {
        return try {
            // 获取 WebView 的 mProvider 字段
            val providerField = getField(WebView::class.java, "mProvider")
            val provider = providerField?.get(webView)

            // 获取 provider 的 mAwContents 字段
            var awContentsField = getField(provider?.javaClass, "mAwContents")
            if(awContentsField==null){
                awContentsField = getField(provider?.javaClass, "g") //g 等于 mAwContents
            }
            val awContents = awContentsField?.get(provider)

            // 获取 AwContents 的 mIsDestroyed 字段并设置为 false
            val isDestroyedField = getField(awContents?.javaClass, "mIsDestroyed")
            isDestroyedField?.set(awContents, false)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}