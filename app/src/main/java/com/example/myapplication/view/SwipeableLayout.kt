package com.example.myapplication.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout

/**
 * 可横向滑出屏幕的布局
 * 支持左滑显示背景内容（如删除按钮），继续滑动则移出屏幕
 */
class SwipeableLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var contentView: View? = null     // 前景内容
    private var backgroundView: View? = null  // 背景层

    private var lastX = 0f
    private var lastY = 0f
    private var downX = 0f
    private var isSwiping = false
    private var isContentVisible = true       // 内容是否完全可见
    private var contentOffset = 0f            // 当前偏移量

    /** 滑动移出屏幕的监听 */
    var onDismissListener: (() -> Unit)? = null

    /** 滑动状态变化的监听 */
    var onSwipeStateListener: ((isSwipeOpen: Boolean) -> Unit)? = null

    // 内容移出屏幕的阈值（内容宽度的 40%）
    private val dismissThresholdPercent = 0.4f

    override fun onFinishInflate() {
        super.onFinishInflate()
        // 获取子 View：第0个是背景层，第1个是内容层
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

                // 判断是否是横向滑动 (水平移动 > 垂直移动)
                if (!isSwiping) {
                    val totalDx = event.x - downX
                    if (Math.abs(totalDx) > Math.abs(dy) && Math.abs(totalDx) > 10) {
                        isSwiping = true
                        backgroundView?.visibility = View.VISIBLE
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }

                if (isSwiping) {
                    // 只允许向左滑动（负方向），不允许向右拉
                    val newOffset = contentOffset + dx
                    contentOffset = if (newOffset > 0) 0f else newOffset.coerceAtLeast(-width.toFloat())
                    updateContentPosition()
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

    private fun updateContentPosition() {
        contentView?.translationX = contentOffset
        // 背景层可见度随滑动距离变化
        backgroundView?.alpha = if (contentOffset < 0) {
            Math.min(1f, -contentOffset / (width * 0.3f))
        } else 0f
    }

    private fun handleRelease() {
        val threshold = (width * dismissThresholdPercent).toInt()

        when {
            // 滑出屏幕
            contentOffset < -threshold -> animateDismiss()

            // 回到原位
            contentOffset < -threshold / 2 -> animateSnapToDelete()

            else -> animateReset()
        }
    }

    /** 完全滑出屏幕（删除） */
    private fun animateDismiss() {
        val animator = ValueAnimator.ofFloat(contentOffset, -width.toFloat())
        animator.addUpdateListener {
            contentOffset = it.animatedValue as Float
            contentView?.translationX = contentOffset
        }
        animator.duration = 250L
        animator.interpolator = DecelerateInterpolator()
        animator.start()
        isContentVisible = false
        postDelayed({
            onDismissListener?.invoke()
        }, 280L)
    }

    /** 吸附到删除按钮位置 */
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
        isContentVisible = true
        onSwipeStateListener?.invoke(true)
    }

    /** 复位动画 */
    private fun animateReset() {
        val animator = ValueAnimator.ofFloat(contentOffset, 0f)
        animator.addUpdateListener {
            contentOffset = it.animatedValue as Float
            contentView?.translationX = contentOffset
        }
        animator.addUpdateListener { _ ->
            backgroundView?.alpha = Math.max(0f, Math.min(1f, -contentOffset / (width * 0.3f)))
        }
        animator.duration = 200L
        animator.interpolator = DecelerateInterpolator()
        animator.start()
        isContentVisible = true
        backgroundView?.visibility = View.GONE
        onSwipeStateListener?.invoke(false)
    }

    /** 外部调用：主动复位 */
    fun reset() {
        contentOffset = 0f
        contentView?.translationX = 0f
        backgroundView?.visibility = View.GONE
        isContentVisible = true
    }

    /** 外部调用：主动滑出 */
    fun dismiss() {
        animateDismiss()
    }
}
