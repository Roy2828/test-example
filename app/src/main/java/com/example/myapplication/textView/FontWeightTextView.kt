package com.example.myapplication.textView

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.core.content.res.ResourcesCompat
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
) : TextView(context, attrs) {

    companion object {
        const val FONT_WEIGHT_UNSPECIFIED = -1
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
        setFontWeight(mFontWeight)

    }


    fun setFontWeight(
        @IntRange(from = 1, to = 1000) weight: Int
    ) {
        mFontWeight = weight

        // 判断系统版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 高版本使用 fontVariationSettings
         val type =   when(weight){
                  100 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_100)
                  200 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_200)
                  300 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_300)
                  400 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_400)
                  500 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_500)
                  600 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_600)
                  700 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_700)
             else -> ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_500)
         }
            typeface = type
        }
    }



}