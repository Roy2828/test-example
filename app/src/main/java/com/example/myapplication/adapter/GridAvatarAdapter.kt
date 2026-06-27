package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

data class GridAvatarItem(
    val id: Int,
    val avatarRes: Int = R.drawable.ic_robot_avatar,
    val isAddButton: Boolean = false
)

class GridAvatarAdapter(
    private val items: List<GridAvatarItem>
) : RecyclerView.Adapter<GridAvatarAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grid_avatar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)

        fun bind(item: GridAvatarItem) {
            ivAvatar.setImageResource(item.avatarRes)
        }
    }
}
