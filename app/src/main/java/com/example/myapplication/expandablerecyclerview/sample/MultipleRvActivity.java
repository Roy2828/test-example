package com.example.myapplication.expandablerecyclerview.sample;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

/**
 * @author HuaJian Jiang.
 *         Date 2017/1/23.
 */
public class MultipleRvActivity extends BaseActivity {
    private static final String TAG = MultipleRvActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackNaviAction();
        setTitle(R.string.multiple_rv_title);
    }

    @Override
    public Fragment getFragment() {
        return new MultipleRvFragment();
    }
}
