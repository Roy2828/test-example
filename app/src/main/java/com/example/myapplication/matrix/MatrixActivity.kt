package com.example.myapplication.matrix

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

/**
 *    desc   :
 *    date   : 2025/9/1 15:17
 *    author : Roy
 *    version: 1.0
 */
class MatrixActivity : AppCompatActivity() {


    val TAG: String = "MatrixLog"
    companion object{
        fun doIntent(context: Context){
            context.startActivity(Intent(context, MatrixActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.matrix_activity_main)
    }


    fun testThreadAnr(view: View) {
        try {
            var number = 0
            while (number++ < 5) {
                Log.e(
                    TAG,
                    "主线程睡眠导致的ANR:次数$number/5"
                )
                try {
                    Thread.sleep(5000L)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    Log.e( TAG, "异常信息为:" + e.message)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e( TAG, "异常信息为:" + e.message)
        }
    }


}