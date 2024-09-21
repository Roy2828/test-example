package com.example.myapplication.recycler

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
class MyAdapter(private val itemList: List<String>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val playingItems = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(itemList[position], playingItems.contains(position))
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView = itemView.findViewById(R.id.iv_img)
        private val tv: TextView = itemView.findViewById(R.id.tv)
        private var animator: ObjectAnimator? = null

        fun bind(item: String, isPlaying: Boolean) {
            // 停止动画，如果当前项被复用
            animator?.cancel()

            // 根据状态启动动画
            if (isPlaying) {
                startAnimator()
            }

            tv.setOnClickListener {
                toggleAnimation(adapterPosition)
            }
        }

        private fun startAnimator() {
            // 确保动画只有在未播放时启动
            if (img.translationX == 0f) {

            }

            animator = ObjectAnimator.ofFloat(img, "translationX", 0f, 1200f)
            animator?.duration = 1000
            animator?.repeatCount = ValueAnimator.INFINITE
            animator?.repeatMode = ValueAnimator.REVERSE
            animator?.start()
        }

        fun stopAnimator() {
            // 停止动画并重置位置
            animator?.cancel()
            img.translationX = 0f
        }
    }

    private fun toggleAnimation(position: Int) {
        if (playingItems.contains(position)) {
            playingItems.remove(position)
        } else {
            playingItems.add(position)
        }
        notifyItemChanged(position) // 刷新当前项
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        super.onViewRecycled(holder)
        holder.stopAnimator() // 停止动画以释放资源
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}