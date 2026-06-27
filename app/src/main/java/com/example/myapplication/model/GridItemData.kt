package com.example.myapplication.model

/**
 * 网格中每个位置的数据
 * displayText: 头像上显示的文字
 * bgColor: 背景颜色（资源 ID）
 * avatarRes: 头像图片资源（可选）
 */
data class GridItemData(
    val id: Long,
    val displayText: String = "",
    val bgColor: Int = 0,
    val avatarRes: Int? = null
) {
    companion object {
        /** 预定义头像背景色 */
        val COLORS = listOf(
            android.graphics.Color.parseColor("#4CAF50"), // 绿
            android.graphics.Color.parseColor("#2196F3"), // 蓝
            android.graphics.Color.parseColor("#FF9800"), // 橙
            android.graphics.Color.parseColor("#9C27B0"), // 紫
            android.graphics.Color.parseColor("#F44336"), // 红
            android.graphics.Color.parseColor("#00BCD4"), // 青
            android.graphics.Color.parseColor("#FF5722"), // 深橙
            android.graphics.Color.parseColor("#3F51B5"), // 靛蓝
        )
    }
}
