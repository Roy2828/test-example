package com.example.myapplication.utils

import android.app.Activity
import android.content.res.Configuration
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.utils.ScreenUtils

/**
 * 微信式大屏适配：
 * 横屏/大屏时按等效竖屏宽度做 density，页面内容再由根布局居中限宽。
 */
object WeChatLargeScreenAdapter {
    private const val DESIGN_WIDTH_IN_DP = 1080
    private const val DESIGN_HEIGHT_IN_DP = 1920

    fun adapt(activity: Activity) {
        val screenSize = ScreenUtils.getScreenSize(activity)
        val screenWidth = screenSize[0]
        val screenHeight = screenSize[1]
        val isLandscape =
            activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        AutoSizeConfig.getInstance()
            .setScreenWidth(if (isLandscape) minOf(screenWidth, screenHeight) else screenWidth)
            .setScreenHeight(if (isLandscape) maxOf(screenWidth, screenHeight) else screenHeight)
            .setDesignWidthInDp(DESIGN_WIDTH_IN_DP)
            .setDesignHeightInDp(DESIGN_HEIGHT_IN_DP)
    }

    fun getContentMaxWidth(width: Int, height: Int): Int {
        return minOf(width, height)
    }
}
