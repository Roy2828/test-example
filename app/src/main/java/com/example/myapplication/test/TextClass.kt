package com.example.myapplication.test

/**
 *    desc   :
 *    date   : 2025/8/6 15:12
 *    author : Roy
 *    version: 1.0
 */

class ITestBy:TestBy {
    override val testName: String
        get() = "ITestBy"
}


class TextClass:TestBy by ITestBy()  {

        fun dd(){
            testName
        }

}