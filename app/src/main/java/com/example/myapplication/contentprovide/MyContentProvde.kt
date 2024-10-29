package com.example.myapplication.contentprovide

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2024/10/27 14:57
 *    author : Roy
 *    version: 1.0
 */
class MyContentProvde :ContentProvider() {
    override fun onCreate(): Boolean {
        Log.e("Roy","执行了")
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return ""
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
       return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}