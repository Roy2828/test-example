package com.example.myapplication.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout

/**
 * 整体可横向滑出屏幕的布局
 * 整个内容区域可以向左滑出屏幕
 */
class HorizontalSwipeableLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var contentView: View? = null
    private var backgroundView: View? = null

    private var lastX = 0f
    private var lastY = 0f
    private var downX = 0f
    private var isSwiping = false
    private var contentOffset = 0f

    var onDismissListener: (() -> Unit)? = null
    var onSwipeStateListener: ((isSwipeOpen: Boolean) -> Unit)? = null

    private val dismissThresholdPercent = 0.35f

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount >= 2) {
            backgroundView = getChildAt(0)
            contentView = getChildAt(1)
            backgroundView?.visibility = View.GONE
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (contentView == null) return super.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                downX = event.x
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastX
                val dy = event.y - lastY

                if (!isSwiping) {
                    val totalDx = event.x - downX
                    if (Math.abs(totalDx) > Math.abs(dy) && Math.abs(totalDx) > 10) {
                        isSwiping = true
                        backgroundView?.visibility = View.VISIBLE
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }

                if (isSwiping) {
                    val newOffset = contentOffset + dx
                    contentOffset = if (newOffset > 0) 0f else newOffset.coerceAtLeast(-width.toFloat())
                    contentView?.translationX = contentOffset
                    updateBackgroundAlpha()
                    lastX = event.x
                    lastY = event.y
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isSwiping) {
                    handleRelease()
                    isSwiping = false
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            lastX = event.x
            lastY = event.y
            downX = event.x
        }
        return false
    }

    private fun updateBackgroundAlpha() {
        backgroundView?.alpha = if (contentOffset < 0) {
            Math.min(1f, -contentOffset / (width * 0.3f))
        } else 0f
    }

    private fun handleRelease() {
        val threshold = (width * dismissThresholdPercent).toInt()
        when {
            contentOffset < -threshold -> animateDismiss()
            contentOffset < -threshold / 2 -> animateSnapToDelete()
            else -> animateReset()
        }
    }

    private fun animateDismiss() {
        val animator = ValueAnimator.ofFloat(contentOffset, -width.toFloat())
        animator.addUpdateListener {
            contentOffset = it.animatedValue as Float
            contentView?.translationX = contentOffset
        }
        animator.duration = 250L
        animator.interpolator = DecelerateInterpolator()
        animator.start()
        postDelayed({ onDismissListener?.invoke() }, 280L)
    }

    private fun animateSnapToDelete() {
        val deleteOffset = -(width * 0.2f)
        val animator = ValueAnimator.ofFloat(contentOffset, deleteOffset)
        animator.addUpdateListener {
            contentOffset = it.animatedValue as Float
            contentView?.translationX = contentOffset
        }
        animator.duration = 200L
        animator.interpolator = DecelerateInterpolator()
        animator.start()
        onSwipeStateListener?.invoke(true)
    }

    private fun animateReset() {
        val animator = ValueAnimator.ofFloat(contentOffset, 0f)
        animator.addUpdateListener {
            contentOffset = it.animatedValue as Float
            contentView?.translationX = contentOffset
        }
        animator.duration = 200L
        animator.interpolator = DecelerateInterpolator()
        animator.start()
        updateBackgroundAlpha()
        backgroundView?.visibility = View.GONE
        onSwipeStateListener?.invoke(false)
    }

    fun reset() {
        contentOffset = 0f
        contentView?.translationX = 0f
        backgroundView?.visibility = View.GONE
    }

    fun dismiss() {
        animateDismiss()
    }
}
