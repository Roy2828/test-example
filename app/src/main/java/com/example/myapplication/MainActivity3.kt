package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.recycler.MyAdapter

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2024/9/21 16:29
 *    author : Roy
 *    version: 1.0
 */
class MainActivity3:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

      var re =  findViewById<RecyclerView>(R.id.rv);
        var arrayList = ArrayList<String>()
        for (i in 1 .. 100){
            arrayList.add("$i")
        }
        re.adapter = MyAdapter(arrayList)
        re.layoutManager = LinearLayoutManager(this)
    }
}