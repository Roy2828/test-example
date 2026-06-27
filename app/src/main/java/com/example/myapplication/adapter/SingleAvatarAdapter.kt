package com.example.myapplication.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.GridItemData

/**
 * 单个头像个独立 item 的适配器
 */
class SingleAvatarAdapter(
    private val items: List<GridItemData>
) : RecyclerView.Adapter<SingleAvatarAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_single_avatar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val bg: FrameLayout = itemView.findViewById(R.id.fl_avatar_bg)
        private val tvText: TextView = itemView.findViewById(R.id.tv_avatar_text)
        private val ivImage: ImageView = itemView.findViewById(R.id.iv_avatar)

        fun bind(data: GridItemData) {
            val shape = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(data.bgColor)
            }
            bg.background = shape

            tvText.text = data.displayText
            tvText.visibility = android.view.View.VISIBLE
            ivImage.visibility = android.view.View.GONE
        }
    }
}
