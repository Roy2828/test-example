package com.example.myapplication.spi

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2025/4/25 09:08
 *    author : Roy
 *    version: 1.0
 */
interface JSMethod {
    fun getMethodName(): String?

    fun invokeMethod(ethodUrl: String?): String?
}