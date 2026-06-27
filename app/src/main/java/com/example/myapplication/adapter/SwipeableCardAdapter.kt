package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.view.SwipeableLayout

data class SwipeableItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val time: String,
    val avatarRes: Int? = null
)

class SwipeableCardAdapter(
    private val onItemClick: (SwipeableItem) -> Unit = {},
    private val onDismiss: (SwipeableItem) -> Unit = {}
) : ListAdapter<SwipeableItem, SwipeableCardAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_swipeable_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val swipeableLayout: SwipeableLayout = itemView.findViewById(R.id.swipeable_layout)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_subtitle)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_time)
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)

        fun bind(item: SwipeableItem) {
            tvTitle.text = item.title
            tvSubtitle.text = item.subtitle
            tvTime.text = item.time

            // 设置滑动移出监听
            swipeableLayout.onDismissListener = {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDismiss(getItem(pos))
                }
            }

            // 点击事件
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(pos))
                }
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<SwipeableItem>() {
        override fun areItemsTheSame(oldItem: SwipeableItem, newItem: SwipeableItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SwipeableItem, newItem: SwipeableItem): Boolean {
            return oldItem == newItem
        }
    }
}
