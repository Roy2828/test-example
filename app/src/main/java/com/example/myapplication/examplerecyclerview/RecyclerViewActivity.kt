package com.example.myapplication.examplerecyclerview

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.R
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.examplerecyclerview.ItemModel
import com.example.myapplication.examplerecyclerview.RecyclerViewRecyclerAdapter
import android.content.Intent
import android.view.View
import com.example.myapplication.examplerecyclerview.RecyclerViewActivity
import com.example.myapplication.expandablelayout.ExpandableLayout
import com.example.myapplication.expandablelayout.Utils
import java.util.ArrayList

class RecyclerViewActivity : AppCompatActivity() {
    val data: MutableList<ItemModel> = ArrayList()
    lateinit var recyclerView:RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)
        //   getSupportActionBar().setTitle(RecyclerViewActivity.class.getSimpleName());
       recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerView.addItemDecoration(DividerItemDecoration(this, 0))
        recyclerView.layoutManager = LinearLayoutManager(this)

        data.add(
            ItemModel(
                "0 ACCELERATE_DECELERATE_INTERPOLATOR",
                R.color.material_blue_grey_200,
                R.color.material_blue_grey_200,
                false,
                Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR)
            )
        )
        data.add(
            ItemModel(
                "1 ACCELERATE_INTERPOLATOR",
                R.color.material_blue_grey_200,
                R.color.material_blue_grey_200,
                false,
                Utils.createInterpolator(Utils.ACCELERATE_INTERPOLATOR)
            )
        )
        data.add(
            ItemModel(
                "2 BOUNCE_INTERPOLATOR",
                R.color.material_blue_grey_200,
                R.color.material_blue_grey_200,
                false,
                Utils.createInterpolator(Utils.BOUNCE_INTERPOLATOR)
            )
        )
        data.add(
            ItemModel(
                "3 DECELERATE_INTERPOLATOR",
                R.color.material_blue_grey_200,
                R.color.material_blue_grey_200,
                false,
                Utils.createInterpolator(Utils.DECELERATE_INTERPOLATOR)
            )
        )
        data.add(
            ItemModel(
                "4 FAST_OUT_LINEAR_IN_INTERPOLATOR",
                R.color.material_blue_grey_200,
                R.color.material_blue_grey_200,
                false,
                Utils.createInterpolator(Utils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
            )
        )
        data.add(
            ItemModel(
                "5 FAST_OUT_SLOW_IN_INTERPOLATOR",
                R.color.material_blue_grey_200,
                R.color.material_blue_grey_200,
                false,
                Utils.createInterpolator(Utils.FAST_OUT_SLOW_IN_INTERPOLATOR)
            )
        )
        data.add(
            ItemModel(
                "6 LINEAR_INTERPOLATOR",
                R.color.material_blue_grey_200,
                R.color.material_blue_grey_200,
                false,
                Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR)
            )
        )
        data.add(
            ItemModel(
                "7 LINEAR_OUT_SLOW_IN_INTERPOLATOR",
                R.color.material_blue_grey_200,
                R.color.material_blue_grey_200,
                false,
                Utils.createInterpolator(Utils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
            )
        )
        var adapter = RecyclerViewRecyclerAdapter(data)
        recyclerView.adapter = adapter
        adapter.itemClick = {layout,position ->
            onClickButton(layout,position)
        }
    }

    private fun onClickButton(expandableLayout: ExpandableLayout, position: Int) {


        fun executeOperate(itemModel: ItemModel,position: Int){
            val holder =
                recyclerView.findViewHolderForAdapterPosition(position) as RecyclerViewRecyclerAdapter.ViewHolder
            if (holder != null) {
               if(itemModel.isExpanded){
                   holder.expandableLayout.expand()
               }else{
                   holder.expandableLayout.collapse()
               }

            }
        }




        data.forEachIndexed{index,item ->
            if(position == index){
                item.isExpanded = !item.isExpanded
            }else{
                item.isExpanded = false
            }
            executeOperate(item,index)
        }



    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, RecyclerViewActivity::class.java))
        }
    }
}