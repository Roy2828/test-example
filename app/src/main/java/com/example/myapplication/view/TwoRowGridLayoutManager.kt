package com.example.myapplication.view

import android.graphics.PointF
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

/**
 * 两行横向网格 LayoutManager
 * 根据 RecyclerView 实际宽度计算每行能放几个 item
 *
 * 例：宽度能放7个，共9个数据
 *   Row 0: [1] [2] [3] [4] [5] [6] [7]     ← 第一行放满
 *   Row 1: [8] [9]                          ← 剩余放第二行
 */
class TwoRowGridLayoutManager : RecyclerView.LayoutManager() {

    private var itemWidth = 0
    private var itemHeight = 0
    private var itemsPerRow = 1
    private var totalColumns = 1

    // 用变量追踪偏移量，不依赖 child 位置计算
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

        val availableWidth = width - paddingLeft - paddingRight
        itemsPerRow = (availableWidth / itemWidth).coerceAtLeast(1)
        totalColumns = calcTotalColumns(state.itemCount)

        fillVisibleItems(recycler, state)
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        totalColumns = calcTotalColumns(state.itemCount)
    }

    private fun calcTotalColumns(count: Int): Int {
        var maxCol = 0
        for (i in 0 until count) {
            val (_, col) = rowCol(i)
            maxCol = maxOf(maxCol, col)
        }
        return maxCol + 1
    }

    /** 填充当前可见区域内的 item */
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

        val contentWidth = paddingLeft + totalColumns * itemWidth + paddingRight
        val visibleWidth = width - paddingLeft - paddingRight
        val maxScroll = (contentWidth - visibleWidth).coerceAtLeast(0)
        val newOffset = (mScrollOffset + dx).coerceIn(0, maxScroll)
        val actualDx = newOffset - mScrollOffset

        if (actualDx != 0) {
            mScrollOffset = newOffset
            // 移动所有 child
            for (j in 0 until childCount) {
                getChildAt(j)?.offsetLeftAndRight(-actualDx)
            }
            // 回收完全不可见的
            for (j in childCount - 1 downTo 0) {
                val child = getChildAt(j) ?: continue
                if (getDecoratedRight(child) < 0 || getDecoratedLeft(child) > width) {
                    removeAndRecycleView(child, recycler)
                }
            }
            // 填充新出现的位置
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
     * 行优先排列：
     *   前 itemsPerRow 个 → 第一行
     *   接下来 itemsPerRow 个 → 第二行
     *   超出部分 → 两行交替排列
     *
     * 例 itemsPerRow=7, 20 个数据：
     *   Row 0: [0][1][2][3][4][5][6][14][16][18]
     *   Row 1: [7][8][9][10][11][12][13][15][17][19]
     */
    private fun rowCol(i: Int): Pair<Int, Int> {
        return when {
            i < itemsPerRow -> 0 to i
            i < 2 * itemsPerRow -> 1 to (i - itemsPerRow)
            else -> {
                val extra = i - 2 * itemsPerRow
                val col = itemsPerRow + extra / 2
                val row = extra % 2
                row to col
            }
        }
    }

    override fun computeHorizontalScrollRange(state: RecyclerView.State): Int {
        return paddingLeft + totalColumns * itemWidth + paddingRight
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
