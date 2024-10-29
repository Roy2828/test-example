package com.example.myapplication.textView

import android.content.Context
import android.util.AttributeSet
import com.example.myapplication.R

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2024/10/23 9:04
 *    author : Roy
 *    version: 1.0
 */
open class FontWeightValueTextView(
    context: Context,
    attrs: AttributeSet? = null
) : FontWeightTextView(context, attrs) {

    init {
        context.obtainStyledAttributes(attrs, R.styleable.FontWeightValueTextView).run {
            if (hasValue(R.styleable.FontWeightValueTextView_fontWeightValue)) {
                val fontWeightValue = getInt(R.styleable.FontWeightValueTextView_fontWeightValue, 400)
                setFontWeight(fontWeightValue)
            }
            recycle()
        }
    }
}