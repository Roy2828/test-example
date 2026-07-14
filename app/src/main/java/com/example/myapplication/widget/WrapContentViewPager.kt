package com.example.myapplication.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.viewpager.widget.ViewPager
import kotlin.math.abs

class WrapContentViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var downX = 0f
    private var downY = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var wrapHeightSpec = heightMeasureSpec
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST ||
            MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED
        ) {
            var height = 0
            for (index in 0 until childCount) {
                val child = getChildAt(index)
                child.measure(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                height = height.coerceAtLeast(child.measuredHeight)
            }
            if (height > 0) {
                wrapHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            }
        }
        super.onMeasure(widthMeasureSpec, wrapHeightSpec)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                parent.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - downX
                val dy = ev.y - downY
                if (abs(dx) > touchSlop || abs(dy) > touchSlop) {
                    parent.requestDisallowInterceptTouchEvent(abs(dx) > abs(dy))
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        return super.canScrollHorizontally(direction) || canChildScrollHorizontally(this, direction)
    }

    private fun canChildScrollHorizontally(view: View, direction: Int): Boolean {
        if (view !== this && view.canScrollHorizontally(direction)) {
            return true
        }
        if (view is android.view.ViewGroup) {
            for (index in 0 until view.childCount) {
                if (canChildScrollHorizontally(view.getChildAt(index), direction)) {
                    return true
                }
            }
        }
        return false
    }
}
