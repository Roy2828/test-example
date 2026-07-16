package com.example.myapplication.plugin.demo

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.myapplication.utils.WeChatLargeScreenAdapter

/**
 * 横屏限制宽度 = 竖屏宽度，居中显示
 * density 适配由 AutoSize 负责，这里只做宽度限制
 */
class AdaptiveRootLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec).takeIf { it > 0 }
            ?: resources.displayMetrics.heightPixels
        val contentMaxWidth = WeChatLargeScreenAdapter.getContentMaxWidth(width, height)

        var newWidthSpec = widthMeasureSpec
        if (isLandscape && width > contentMaxWidth) {
            newWidthSpec = MeasureSpec.makeMeasureSpec(contentMaxWidth, MeasureSpec.EXACTLY)
            val lp = layoutParams
            if (lp is FrameLayout.LayoutParams) {
                lp.gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = lp
            }
        } else if (!isLandscape) {
            val lp = layoutParams
            if (lp is FrameLayout.LayoutParams && lp.gravity != Gravity.NO_GRAVITY) {
                lp.gravity = Gravity.NO_GRAVITY
                layoutParams = lp
            }
        }

        super.onMeasure(newWidthSpec, heightMeasureSpec)
    }
}
