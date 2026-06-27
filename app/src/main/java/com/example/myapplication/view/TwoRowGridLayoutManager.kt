package com.example.myapplication.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * 两行横向网格 LayoutManager
 * 数据对半拆分，两行数量相同（或第一行多1个）
 * 不自带横向滚动，由外层 HorizontalScrollView 统一处理滑动
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

        val sample = recycler.getViewForPosition(0)
        addView(sample)
        measureChildWithMargins(sample, 0, 0)
        itemWidth = getDecoratedMeasuredWidth(sample)
        itemHeight = getDecoratedMeasuredHeight(sample)
        detachAndScrapView(sample, recycler)

        halfCount = (state.itemCount + 1) / 2
        fillAllItems(recycler, state)
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        halfCount = (state.itemCount + 1) / 2
    }

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        if (state.itemCount == 0) {
            super.onMeasure(recycler, state, widthSpec, heightSpec)
            return
        }

        // 测量一个样本子项以获取尺寸
        val sample = recycler.getViewForPosition(0)
        addView(sample)
        measureChildWithMargins(sample, 0, 0)
        val sampleWidth = getDecoratedMeasuredWidth(sample)
        val sampleHeight = getDecoratedMeasuredHeight(sample)
        detachAndScrapView(sample, recycler)

        val columns = (state.itemCount + 1) / 2
        val contentWidth = paddingLeft + columns * sampleWidth + paddingRight
        val contentHeight = paddingTop + 2 * sampleHeight + paddingBottom

        val widthSize = View.MeasureSpec.getSize(widthSpec)
        val widthMode = View.MeasureSpec.getMode(widthSpec)
        val measuredWidth = when (widthMode) {
            View.MeasureSpec.EXACTLY -> widthSize
            View.MeasureSpec.AT_MOST -> minOf(contentWidth, widthSize)
            else -> contentWidth
        }

        val heightSize = View.MeasureSpec.getSize(heightSpec)
        val heightMode = View.MeasureSpec.getMode(heightSpec)
        val measuredHeight = when (heightMode) {
            View.MeasureSpec.EXACTLY -> heightSize
            View.MeasureSpec.AT_MOST -> minOf(contentHeight, heightSize)
            else -> contentHeight
        }

        setMeasuredDimension(measuredWidth, measuredHeight)

        // 保存到成员变量，供 layout 使用
        itemWidth = sampleWidth
        itemHeight = sampleHeight
    }

    private fun fillAllItems(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        val count = state.itemCount
        for (i in 0 until count) {
            val left = getItemLeft(i)
            val top = getItemTop(i)
            val child = recycler.getViewForPosition(i)
            addView(child)
            measureChildWithMargins(child, 0, 0)
            layoutDecoratedWithMargins(child, left, top, left + itemWidth, top + itemHeight)
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

    override fun canScrollHorizontally() = false

    /**
     * 前 halfCount 个放第一行，剩余的放第二行
     * 例 36: Row0 [0..17], Row1 [18..35]
     */
    private fun rowCol(i: Int): Pair<Int, Int> {
        return if (i < halfCount) 0 to i
        else 1 to (i - halfCount)
    }

    override fun isAutoMeasureEnabled(): Boolean = false
}
