package com.example.myapplication.spi

import com.clife.beautyx.annotations.AndroidSpi

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2025/4/25 09:10
 *    author : Roy
 *    version: 1.0
 */
@AndroidSpi(JSMethod::class, 0, true)
class CustomerInfoJS : JSMethod {
    override fun getMethodName(): String? {
        return  "111"
    }

    override fun invokeMethod(ethodUrl: String?): String? {
       return "CustomerInfoJS"
    }
}