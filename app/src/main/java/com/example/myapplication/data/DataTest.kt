package com.example.myapplication.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2025/1/5 18:34
 *    author : Roy
 *    version: 1.0
 */

@Parcelize
data class DataTest(  val firstName: String,
                 val lastName: String,
                 val age: Int):Parcelable
@Parcelize
data class User(
    val firstName: String,
    val lastName: String) : Parcelable