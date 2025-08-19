package com.example.myapplication.speak

/**
 *    desc   :
 *    date   : 2025/7/7 19:50
 *    author : Roy
 *    version: 1.0
 */


enum class PlayStatus(val value: Int) {
    //输入文本状态，0:开始，1:中间，2:结束
    START(0), //开始
    MIDDLE(1), //中间
    END(2); //结束

    companion object {
        fun fromValue(value: Int): PlayStatus {
            return values().firstOrNull {
                it.value == value
            } ?: START
        }
    }
}