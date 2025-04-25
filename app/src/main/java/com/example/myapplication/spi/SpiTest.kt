package com.example.myapplication.spi

import android.widget.Toast
import com.clife.beautyx.annotations.AndroidSpiManager
import com.example.myapplication.App

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2025/4/25 09:14
 *    author : Roy
 *    version: 1.0
 */
object SpiTest {
    fun test(): String? {
        AndroidSpiManager.getInstance().load(JSMethod::class).map {
            it as JSMethod
        }.firstOrNull {
            it.getMethodName() == "111"
        }?.let {
            return it.invokeMethod("aaa")
        }
        return "aa";

    }

}
