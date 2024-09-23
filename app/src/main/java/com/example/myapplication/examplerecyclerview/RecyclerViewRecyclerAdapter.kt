package com.example.myapplication.examplerecyclerview

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorListenerAdapter
import com.example.myapplication.examplerecyclerview.ItemModel
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.example.myapplication.R
import android.annotation.SuppressLint
import androidx.core.content.ContextCompat
import com.example.myapplication.expandablelayout.ExpandableLayoutListenerAdapter
import com.example.myapplication.expandablelayout.ExpandableLayout
import android.widget.TextView
import android.widget.RelativeLayout
import com.example.myapplication.expandablelayout.ExpandableLinearLayout
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import com.example.myapplication.expandablelayout.Utils
import java.util.HashMap

class RecyclerViewRecyclerAdapter(private val data: List<ItemModel>) :
    RecyclerView.Adapter<RecyclerViewRecyclerAdapter.ViewHolder>() {
    private var context: Context? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.recycler_view_list_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = data[position]
        holder.setIsRecyclable(false)
        holder.textView.text = item.description
        holder.descriptionText.text ="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        holder.itemView.setBackgroundColor(ContextCompat.getColor(context!!, item.colorId1))
        holder.expandableLayout.setInRecyclerView(true)
        holder.expandableLayout.setBackgroundColor(ContextCompat.getColor(context!!, item.colorId2))
        holder.expandableLayout.setInterpolator(item.interpolator)
        holder.expandableLayout.isExpanded = item.isExpanded


        holder.expandableLayout.setListener(object : ExpandableLayoutListenerAdapter() {
            override fun onPreOpen() {
               // createRotateAnimator(holder.buttonLayout, 0f, 180f).start()
                data.forEachIndexed{index,item ->
                    if(position == index){
                        item.isExpanded = true
                    }else{
                        item.isExpanded = false
                    }
               }

            var animator =    createRotateAnimator(holder.buttonLayout, 0f, 180f)
                animator.addListener(object : AnimatorListener{
                    override fun onAnimationStart(animation: Animator?) {

                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        notifyDataSetChanged()
                    }

                    override fun onAnimationCancel(animation: Animator?) {

                    }

                    override fun onAnimationRepeat(animation: Animator?) {

                    }
                })
                animator.start()


            }

            override fun onPreClose() {
                createRotateAnimator(holder.buttonLayout, 180f, 0f).start()
                data[position].isExpanded = false
            }
        })
        holder.buttonLayout.rotation = if (item.isExpanded) 180f else 0f
        holder.buttonLayout.setOnClickListener {
            onClickButton(holder.expandableLayout)
        }
    }

    private fun onClickButton(expandableLayout: ExpandableLayout) {
        expandableLayout.toggle()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var textView: TextView
        var descriptionText: TextView
        var buttonLayout: RelativeLayout

        /**
         * You must use the ExpandableLinearLayout in the recycler view.
         * The ExpandableRelativeLayout doesn't work.
         */
        var expandableLayout: ExpandableLinearLayout

        init {
            textView = v.findViewById<View>(R.id.titleText) as TextView
            descriptionText = v.findViewById<View>(R.id.descriptionText) as TextView
            buttonLayout = v.findViewById<View>(R.id.button) as RelativeLayout
            expandableLayout = v.findViewById<View>(R.id.expandableLayout) as ExpandableLinearLayout
        }
    }

    fun createRotateAnimator(target: View?, from: Float, to: Float): ObjectAnimator {
        val animator = ObjectAnimator.ofFloat(target, "rotation", from, to)
        animator.duration = 300
        animator.interpolator =
            Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR)
        return animator
    }
}