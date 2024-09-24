package com.example.myapplication.expandablerecyclerview.sample.viewholder;

import android.view.View;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.expandablerecyclerview.sample.model.MyChild;
import com.example.myapplication.expandablerecyclerview.widget.ChildViewHolder;


/**
 * Created by jhj_Plus on 2016/9/2.
 */
public class MyChildViewHolder extends ChildViewHolder<MyChild> {
    private static final String TAG = "MyChildViewHolder";

    public MyChildViewHolder(View itemView) {
        super(itemView);
    }

    public void bind(MyChild data) {
        String info = data.getInfo();
        TextView tv_info = getView(R.id.info);
        tv_info.setText(info);
        getView(R.id.dot).setBackgroundColor(data.getDot());
    }

}
