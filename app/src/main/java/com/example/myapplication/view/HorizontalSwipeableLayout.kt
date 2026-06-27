package com.example.myapplication.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * 普通 FrameLayout，不处理任何滑动删除逻辑，
 * 事件完全透传给子 View（内部 RecyclerView 的横向滑动）
 */
class HorizontalSwipeableLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr)
