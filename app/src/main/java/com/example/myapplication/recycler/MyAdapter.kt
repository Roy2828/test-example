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
class MyAdapter constructor(list: ArrayList<String>) :
    BaseQuickAdapter<String, MyViewHolder>(R.layout.item, list) {
    private val animatorMap: MutableMap<MyViewHolder, ObjectAnimator> = HashMap()

    override fun convert(holder: MyViewHolder, item: String) {
        // Check if the item should have animation
        if (holder.img?.tag == item) {
            startAnimation(holder)
        } else {
            stopAnimation(holder)
        }

        holder.tv?.setOnClickListener {
            holder.img?.setTag(item)
            if (holder.img?.tag == item) {
                startAnimation(holder)
            } else {
                stopAnimation(holder)
            }

        }
    }

    private fun startAnimation(holder: MyViewHolder) {
        if (!animatorMap.containsKey(holder)) {
            val animator: ObjectAnimator = ObjectAnimator.ofFloat(holder.img, "translationX", -120f, 1200f)
            animator.duration = 1000
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.RESTART
            animator.start()
            animatorMap[holder] = animator
        }
    }

    private fun stopAnimation(holder: MyViewHolder) {
        holder.img?.translationX = 0f  //恢复初始位置
        animatorMap[holder]?.cancel()
        animatorMap.remove(holder)

    }

}


class MyViewHolder constructor(view: View) : BaseViewHolder(view) {
    var img:ImageView?=null
    var tv:TextView?=null
    init {
       img =   view.findViewById<ImageView>(R.id.iv_img)
       tv =   view.findViewById<TextView>(R.id.tv)
    }
}