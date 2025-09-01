package com.example.myapplication.web

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebView

/**
 *    desc   :
 *    date   : 2025/8/27 16:59
 *    author : Roy
 *    version: 1.0
 */
class MyWebView : WebView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.e("Roy","MyWebView onDetachedFromWindow"  )
    }
}

