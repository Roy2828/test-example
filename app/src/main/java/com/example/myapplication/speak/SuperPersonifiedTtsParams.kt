package com.example.myapplication.speak

/**
 *    desc   :
 *    date   : 2025/7/7 17:07
 *    author : Roy
 *    version: 1.0
 */
data class SuperPersonifiedTtsParams(
    var content:String,
    var vcn: String? = "x5_lingyuyan_flow", //聆玉言
    var pitch: Int = 50,
    var speed: Int = 50,
    var volume: Int = 50,
    var status:PlayStatus = PlayStatus.START //输入文本状态，0:开始，1:中间，2:结束
)