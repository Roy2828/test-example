package com.example.myapplication.textView

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.IntRange
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.TypefaceCompat

import com.example.myapplication.R

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2024/10/23 9:05
 *    author : Roy
 *    version: 1.0
 */
open class FontWeightTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    companion object {
        private const val FONT_WEIGHT_UNSPECIFIED = -1
    }

    private var mFontWeight = FONT_WEIGHT_UNSPECIFIED

    init {
        context.obtainStyledAttributes(attrs, R.styleable.FontWeightTextView).run {
            if (hasValue(R.styleable.FontWeightTextView_android_textFontWeight)) {
                mFontWeight = getInt(R.styleable.FontWeightTextView_android_textFontWeight, FONT_WEIGHT_UNSPECIFIED)
            } else if (hasValue(R.styleable.FontWeightTextView_textFontWeight)) {
                mFontWeight = getInt(R.styleable.FontWeightTextView_textFontWeight, FONT_WEIGHT_UNSPECIFIED)
            }
            recycle()
        }

        innerSetFontWeight()
    }

    private fun innerSetFontWeight() {
        if (mFontWeight == FONT_WEIGHT_UNSPECIFIED)
            return

        setFontWeight(mFontWeight)
    }

    fun getFontWeight() = mFontWeight

    fun setFontWeight(
        @IntRange(from = 1, to = 1000) weight: Int
    ) {
        mFontWeight = weight
        TypefaceCompat.create(context, typeface, weight).let {
            typeface = it
        }
    }
}