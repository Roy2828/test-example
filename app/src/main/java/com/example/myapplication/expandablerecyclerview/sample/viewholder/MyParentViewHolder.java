package com.example.myapplication.expandablerecyclerview.sample.viewholder;

import android.view.View;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.expandablerecyclerview.sample.model.MyParent;
import com.example.myapplication.expandablerecyclerview.widget.ParentViewHolder;


/**
 * Created by jhj_Plus on 2016/9/2.
 */
public class MyParentViewHolder extends ParentViewHolder<MyParent> {
    private static final String TAG = "MyParentViewHolder";

    public MyParentViewHolder(View itemView) {
        super(itemView);
    }

    public void bind(MyParent data) {
        String info = data.getInfo();
        TextView tv_info = getView(R.id.info);
        tv_info.setText(info);
        getView(R.id.dot).setBackgroundColor(data.getDot());
        View arrow = getView(R.id.arrow);
        arrow.setVisibility(isExpandable() ? View.VISIBLE : View.GONE);
        if (isExpandable()) {
            arrow.setRotation(isExpanded() ? 180 : 0);
        }
    }
}
