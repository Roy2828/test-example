package com.example.myapplication.textView

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R
import com.example.myapplication.textView.FontWeightTextView.Companion.FONT_WEIGHT_UNSPECIFIED


/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2025/1/11 14:18
 *    author : Roy
 *    version: 1.0
 */
class FTextView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) :TextView(context,attrs) {

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
        initView()
    }

    fun initView(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 高版本使用 fontVariationSettings
            val type =   when(mFontWeight){
                100 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_100)
                200 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_200)
                300 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_300)
                400 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_400)
                500 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_500)
                600 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_600)
                700 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_700)
                800 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_800)
                900 ->   ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_900)
                else -> ResourcesCompat.getFont(context, R.font.ali_mama_fangyuan_500)
            }
            typeface = type
        }

    }
}