package com.example.myapplication.matrix

import android.app.Application


object AppMatrix {
    fun initMatrix(application: Application?) {
        MatrixUtils.getInstance().initPlugin(application, "com.example.myapplication.matrix.MatrixActivity;")
    }
}
