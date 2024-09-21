package com.example.myapplication.recycler

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.myapplication.R


/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2024/9/21 16:10
 *    author : Roy
 *    version: 1.0
 */
class MyAdapter2 constructor(list: ArrayList<String>) :
    BaseQuickAdapter<String, MyViewHolder>(R.layout.item, list) {
    private val playingItems = hashSetOf<Int>()

    override fun convert(holder: MyViewHolder, item: String) {
        holder?.animator?.cancel()
        if(playingItems.contains(holder.layoutPosition)){ //需要启动动画

             startAnimation(holder)
        }

        holder.tv?.setOnClickListener {
             if(playingItems.contains(holder.layoutPosition)){
                 playingItems.remove(holder.layoutPosition)
             }else{
                 playingItems.add(holder.layoutPosition)
             }
            notifyDataSetChanged()
        }
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        super.onViewRecycled(holder)
        stopAnimation(holder)
    }

    private fun startAnimation(holder: MyViewHolder) {
        holder.animator = ObjectAnimator.ofFloat(holder.img, "translationX", -120f, 1200f)
        holder.animator?.duration = 1000
        holder.animator?.repeatCount = ValueAnimator.INFINITE
        holder.animator?.repeatMode = ValueAnimator.RESTART
        holder.animator?.start()
    }

    private fun stopAnimation(holder: MyViewHolder) {
        holder.img?.translationX = 0f  //恢复初始位置
        holder?.animator?.cancel()

    }

}


class MyViewHolder constructor(view: View) : BaseViewHolder(view) {
    var img:ImageView?=null
    var tv:TextView?=null

    var   animator: ObjectAnimator?=null
    init {
        img =   view.findViewById<ImageView>(R.id.iv_img)
        tv =   view.findViewById<TextView>(R.id.tv)
    }
}