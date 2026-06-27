package com.example.myapplication

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.adapter.SingleAvatarAdapter
import com.example.myapplication.model.GridItemData
import com.example.myapplication.view.HorizontalSwipeableLayout
import com.example.myapplication.view.TwoRowGridLayoutManager

class SwipeableCardTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipeable_card_test)
        title = "横向滑动测试"

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        val areaData = listOf(
            AreaData("沙土区", generateGridItems(9)),
            AreaData("草坪区", generateGridItems(12)),
            AreaData("广场区", generateGridItems(16)),
            AreaData("游乐区", generateGridItems(14)),
            AreaData("休息区", generateGridItems(35)),
        )

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(R.layout.item_horizontal_swipe_grid, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val itemView = holder.itemView
                val area = areaData[position]

                itemView.findViewById<TextView>(R.id.tv_area_name).text = area.name
                itemView.findViewById<TextView>(R.id.tv_people_count).text = "${area.items.size}/16人"

                val rvGrid = itemView.findViewById<RecyclerView>(R.id.rv_grid)
                rvGrid.layoutManager = TwoRowGridLayoutManager()
                rvGrid.adapter = SingleAvatarAdapter(area.items)
                rvGrid.isNestedScrollingEnabled = false

                itemView.findViewById<HorizontalSwipeableLayout>(R.id.horizontal_swipe_layout)
                    .onDismissListener = {
                    Toast.makeText(this@SwipeableCardTestActivity,
                        "已移除: ${area.name}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun getItemCount(): Int = areaData.size
        }
        recyclerView.adapter = adapter
    }

    private fun generateGridItems(count: Int): List<GridItemData> {
        return (1..count).map { i ->
            GridItemData(
                id = i.toLong(),
                displayText = i.toString(),
                bgColor = GridItemData.COLORS[(i - 1) % GridItemData.COLORS.size]
            )
        }
    }

    data class AreaData(
        val name: String,
        val items: List<GridItemData>
    )

    companion object {
        fun doIntent(context: android.content.Context) {
            context.startActivity(android.content.Intent(context, SwipeableCardTestActivity::class.java))
        }
    }
}
