package com.example.myapplication.view

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class GameRegionAddView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.dpToPx2()
        color = Color.parseColor("#FF9800")
        pathEffect = DashPathEffect(floatArrayOf(4.dpToPx2(), 3.dpToPx2()), 0f)
    }

    private val plusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.dpToPx2()
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#FF9800")
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFF3E0")
    }

    fun setColors(dashColor: Int, bgColor: Int, plusColor: Int) {
        dashPaint.color = dashColor
        bgPaint.color = bgColor
        plusPaint.color = plusColor
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = min(width, height)
        val cx = width / 2f
        val cy = height / 2f
        val radius = size / 2f - dashPaint.strokeWidth

        // 背景圆
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // 虚线边框圆
        canvas.drawCircle(cx, cy, radius, dashPaint)

        // 加号横线
        val plusLength = radius * 0.25f
        canvas.drawLine(cx - plusLength, cy, cx + plusLength, cy, plusPaint)

        // 加号竖线
        canvas.drawLine(cx, cy - plusLength, cx, cy + plusLength, plusPaint)
    }
}

private fun Float.dpToPx2(): Float =
    (this * Resources.getSystem().displayMetrics.density + 0.5f)

private fun Int.dpToPx2(): Float =
    (this * Resources.getSystem().displayMetrics.density + 0.5f)
