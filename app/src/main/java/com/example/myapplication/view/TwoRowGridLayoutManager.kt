package com.example.myapplication.view

import android.graphics.PointF
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

/**
 * 两行横向网格 LayoutManager
 * 数据对半拆分，两行数量相同（或第一行多1个）
 *
 * 例 36 条：
 *   Row 0: [1][2][3]...[18]
 *   Row 1: [19][20][21]...[36]
 *
 * 例 40 条：
 *   Row 0: [1][2][3]...[20]
 *   Row 1: [21][22][23]...[40]
 */
class TwoRowGridLayoutManager : RecyclerView.LayoutManager() {

    private var itemWidth = 0
    private var itemHeight = 0
    private var halfCount = 0
    private var mScrollOffset = 0

    override fun generateDefaultLayoutParams() = RecyclerView.LayoutParams(
        RecyclerView.LayoutParams.WRAP_CONTENT,
        RecyclerView.LayoutParams.WRAP_CONTENT
    )

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }
        detachAndScrapAttachedViews(recycler)
        mScrollOffset = 0

        val sample = recycler.getViewForPosition(0)
        addView(sample)
        measureChildWithMargins(sample, 0, 0)
        itemWidth = getDecoratedMeasuredWidth(sample)
        itemHeight = getDecoratedMeasuredHeight(sample)
        detachAndScrapView(sample, recycler)

        halfCount = (state.itemCount + 1) / 2
        fillVisibleItems(recycler, state)
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        halfCount = (state.itemCount + 1) / 2
    }

    private fun calcTotalColumns(): Int = halfCount

    private fun fillVisibleItems(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        val count = state.itemCount
        for (i in 0 until count) {
            val left = getItemLeft(i)
            val top = getItemTop(i)
            val screenLeft = left - mScrollOffset
            val screenRight = screenLeft + itemWidth
            if (screenRight >= 0 && screenLeft < width && getChildAtPosition(i) == null) {
                val child = recycler.getViewForPosition(i)
                addView(child)
                measureChildWithMargins(child, 0, 0)
                layoutDecoratedWithMargins(child, screenLeft, top, screenRight, top + itemHeight)
            }
        }
    }

    private fun getItemLeft(position: Int): Int {
        val (_, col) = rowCol(position)
        return paddingLeft + col * itemWidth
    }

    private fun getItemTop(position: Int): Int {
        val (row, _) = rowCol(position)
        return paddingTop + row * itemHeight
    }

    override fun canScrollHorizontally() = true

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (childCount == 0) return 0

        val contentWidth = paddingLeft + calcTotalColumns() * itemWidth + paddingRight
        val visibleWidth = width - paddingLeft - paddingRight
        val maxScroll = (contentWidth - visibleWidth).coerceAtLeast(0)
        val newOffset = (mScrollOffset + dx).coerceIn(0, maxScroll)
        val actualDx = newOffset - mScrollOffset

        if (actualDx != 0) {
            mScrollOffset = newOffset
            for (j in 0 until childCount) {
                getChildAt(j)?.offsetLeftAndRight(-actualDx)
            }
            for (j in childCount - 1 downTo 0) {
                val child = getChildAt(j) ?: continue
                if (getDecoratedRight(child) < 0 || getDecoratedLeft(child) > width) {
                    removeAndRecycleView(child, recycler)
                }
            }
            fillVisibleItems(recycler, state)
        }
        return actualDx
    }

    private fun getChildAtPosition(position: Int): View? {
        for (j in 0 until childCount) {
            val child = getChildAt(j) ?: continue
            if (getPosition(child) == position) return child
        }
        return null
    }

    /**
     * 前 halfCount 个放第一行，剩余的放第二行
     * 例 36: Row0 [0..17], Row1 [18..35]
     */
    private fun rowCol(i: Int): Pair<Int, Int> {
        return if (i < halfCount) 0 to i
        else 1 to (i - halfCount)
    }

    override fun computeHorizontalScrollRange(state: RecyclerView.State): Int {
        return paddingLeft + calcTotalColumns() * itemWidth + paddingRight
    }

    override fun computeHorizontalScrollExtent(state: RecyclerView.State): Int {
        return width
    }

    override fun computeHorizontalScrollOffset(state: RecyclerView.State): Int {
        return mScrollOffset
    }

    override fun isAutoMeasureEnabled(): Boolean = false

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        val scroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                val (_, col) = rowCol(targetPosition)
                val targetX = paddingLeft + col * itemWidth
                val dx = targetX - mScrollOffset
                return PointF(dx.toFloat(), 0f)
            }
        }
        scroller.targetPosition = position
        startSmoothScroll(scroller)
    }
}
