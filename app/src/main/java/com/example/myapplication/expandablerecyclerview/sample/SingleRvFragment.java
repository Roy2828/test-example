package com.example.myapplication.expandablerecyclerview.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.expandablerecyclerview.sample.adapter.MyAdapter;
import com.example.myapplication.expandablerecyclerview.sample.model.MyParent;
import com.example.myapplication.expandablerecyclerview.sample.util.AppUtil;
import com.example.myapplication.expandablerecyclerview.util.Logger;
import com.example.myapplication.expandablerecyclerview.widget.ExpandableAdapter;
import com.example.myapplication.expandablerecyclerview.widget.ExpandableRecyclerView;
import com.example.myapplication.expandablerecyclerview.widget.ParentViewHolder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by jhj_Plus on 2016/9/2.
 */
public class SingleRvFragment extends Fragment {
    public static final int REQUEST_RESULT = 1;
    private static final String TAG = "SingleRvFragment";
    public static final String KEY_DATA = "data";
    private ExpandableRecyclerView mRv;
    private MyAdapter mAdapter;
    private RecyclerView.ItemAnimator mItemAnimator;
    private PresenterImpl mIPresenter;
    private ArrayList<MyParent> mData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_single_rv, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 保存 ExpandableRecyclerView 状态
        if (savedInstanceState != null) {
            Logger.e(TAG, "<<<<<<<<<<<<<<<<<< Restore Data >>>>>>>>>>>>>>>>>");
            mData = savedInstanceState.getParcelableArrayList(KEY_DATA);
        } else {
            mData = AppUtil.getListData();
        }
        init(getView());
        mAdapter.onRestoreInstanceState(savedInstanceState);
    }

    private void init(View rootView) {
        mRv = (ExpandableRecyclerView) rootView.findViewById(R.id.rv);
        mAdapter = new MyAdapter(getActivity(), mData);
        mAdapter.setExpandCollapseMode(ExpandableAdapter.ExpandCollapseMode.MODE_SINGLE_EXPAND);

     /*   mItemAnimator = AppUtil.checkLollipop() ? new CircularRevealItemAnimator()
                                                : new DefaultItemAnimator();*/

        mItemAnimator =   new DefaultItemAnimator();

        mAdapter.addParentExpandableStateChangeListener(new ParentExpandableStateChangeListener());
        mAdapter.addParentExpandCollapseListener(new ParentExpandCollapseListener());
        mRv.setAdapter(mAdapter);
        mRv.addItemDecoration(mAdapter.getItemDecoration());
        mRv.setItemAnimator(mItemAnimator);

        //        mRv.setPadding(0, 0, 0, Res.getNavigationBarHeight(getContext()));

        mAdapter.parentLongClickTargets(R.id.parent)
                .listenParentLongClick(new ExpandableAdapter.OnParentLongClickListener() {
                    @Override
                    public boolean onParentLongClick(RecyclerView parent, View view) {
                        final int childAdapterPos =
                                parent.getChildAdapterPosition(parent.findContainingItemView(view));
                        AppUtil.showToast(getContext(), "Parent LongClick =>" + "pos=" +
                                                        mAdapter.getParentPosition(
                                                                childAdapterPos) + ",adapterPos=" +
                                                        childAdapterPos);
                        return false;
                    }
                })
                .parentClickTargets(R.id.android)
                .listenParentClick(new ExpandableAdapter.OnParentClickListener() {
                    @Override
                    public void onParentClick(RecyclerView parent, View view) {
                        if (view.getId() == R.id.android) {
                            AppUtil.showToast(getContext(), view.getTag().toString());
                        }
                    }
                });

        mAdapter.childLongClickTargets(R.id.child)
                .listenChildLongClick(new ExpandableAdapter.OnChildLongClickListener() {
                    @Override
                    public boolean onChildLongClick(RecyclerView parent, View view) {
                        AppUtil.showToast(getContext(), "Child LongClick");
                        return false;
                    }
                })
                .childClickTargets(R.id.android)
                .listenChildClick(new ExpandableAdapter.OnChildClickListener() {
                    @Override
                    public void onChildClick(RecyclerView parent, View view) {
                        if (view.getId() == R.id.android) {
                            AppUtil.showToast(getContext(), view.getTag().toString());
                        }
                    }
                });

        mIPresenter = new PresenterImpl(mAdapter, mData);

        registerForContextMenu(mRv);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        getActivity().getMenuInflater().inflate(R.menu.context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableRecyclerView.ExpandableRecyclerViewContextMenuInfo menuInfo =
                (ExpandableRecyclerView.ExpandableRecyclerViewContextMenuInfo) item.getMenuInfo();
        Logger.e(TAG, menuInfo.toString());
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MyAdapter adapter = (MyAdapter) mRv.getAdapter();
        int id = item.getItemId();
        switch (id) {
            case R.id.action_test:
                DialogFragment dialog =
                        (DialogFragment) getChildFragmentManager().findFragmentByTag("dialog");
                if (dialog == null) dialog = new MyDialog();
                dialog.setTargetFragment(this, REQUEST_RESULT);
                dialog.show(getChildFragmentManager(), "dialog");
                break;
            case R.id.action_refresh:
                adapter.notifyAllChanged();
                break;
            case R.id.action_toggle_expandable_1:
                adapter.toggleExpandable(1);
                break;
            case R.id.action_expand_all:
                adapter.expandAllParents();
                break;
            case R.id.action_collapse_all:
                adapter.collapseAllParents();
                break;
            case R.id.action_expand_1:
                adapter.expandParent(1);
                break;
            case R.id.action_collapse_1:
                adapter.collapseParent(1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_RESULT) {
            ArrayList<String> requests = data.getStringArrayListExtra(MyDialog.REQUEST);
            Log.e(TAG, "requests=" + requests.toString());
            final int requestCount = requests.size();
            for (int i = 0; i < requestCount; i++) {
                String request = requests.get(i);
                String[] requestSplit = request.split(",");
                String method = requestSplit[0];
                final int argsCount = requestSplit.length - 1;
                try {
                    Object[] args = new Object[argsCount];
                    Class<?>[] argTypes = new Class<?>[argsCount];
                    for (int k = 0; k < argsCount; k++) {
                        argTypes[k] = int.class;
                        try {
                            args[k] = Integer.valueOf(requestSplit[k + 1]);
                        } catch (NumberFormatException e) {
                            AppUtil.showToast(getContext(),
                                              "Test failed,please check input format");
                            return;
                        }
                    }
                    Method m = IPresenter.class.getDeclaredMethod(method, argTypes);
                    Log.e(TAG, "method=" + m.toString());
                    m.setAccessible(true);
                    m.invoke(mIPresenter, args);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    AppUtil.showToast(getContext(), "Test failed,please check input format");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    AppUtil.showToast(getContext(), "Test failed,please check input format");
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    AppUtil.showToast(getContext(), "Test failed,please check input format");
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.e(TAG, "<<<<<<<<<<<<<<<<<< Save Data >>>>>>>>>>>>>>>>>");
        outState.putParcelableArrayList(KEY_DATA, mData);
        mAdapter.onSaveInstanceState(outState);
    }

    private class ParentExpandableStateChangeListener
            implements ExpandableAdapter.OnParentExpandableStateChangeListener
    {
        @Override
        public void onParentExpandableStateChanged(RecyclerView rv, ParentViewHolder pvh,
                int position, boolean expandable)
        {
            Logger.e(TAG, "onParentExpandableStateChanged=" + position + "," + rv.getTag());
            if (pvh == null) return;
            final ImageView arrow = pvh.getView(R.id.arrow);
            if (expandable && arrow.getVisibility() != View.VISIBLE) {
                arrow.setVisibility(View.VISIBLE);
                arrow.setRotation(pvh.isExpanded() ? 180 : 0);
            } else if (!expandable && arrow.getVisibility() == View.VISIBLE) {
                arrow.setVisibility(View.GONE);
            }
        }
    }

    private class ParentExpandCollapseListener
            implements ExpandableAdapter.OnParentExpandCollapseListener
    {
        @Override
        public void onParentExpanded(RecyclerView rv, ParentViewHolder pvh, int position,
                boolean pendingCause, boolean byUser)
        {
            Logger.e(TAG, "onParentExpanded=" + position + "," + rv.getTag() + ",byUser=" + byUser);
            if (pvh == null) return;
            ImageView arrow = pvh.getView(R.id.arrow);
            if (arrow.getVisibility() != View.VISIBLE) return;
            float currRotate = arrow.getRotation();
            //重置为从0开始旋转
            if (currRotate == 360) {
                arrow.setRotation(0);
            }
            if (pendingCause) {
                arrow.setRotation(180);
            } else {
                arrow.animate()
                     .rotation(180)
                     .setDuration(mItemAnimator.getAddDuration() + 180)
                     .start();
            }

            //            if (byUser) {
            //                int scrollToPos =
            //                        pvh.getAdapterPosition() + ((MyParent) pvh.getParent()).getChildCount();
            //                rv.scrollToPosition(scrollToPos);
            //            }
        }

        @Override
        public void onParentCollapsed(RecyclerView rv, ParentViewHolder pvh, int position,
                boolean pendingCause, boolean byUser)
        {
            Logger.e(TAG,
                     "onParentCollapsed=" + position + ",tag=" + rv.getTag() + ",byUser=" + byUser);

            if (pvh == null) return;
            ImageView arrow = pvh.getView(R.id.arrow);
            if (arrow.getVisibility() != View.VISIBLE) return;
            float currRotate = arrow.getRotation();
            float rotate = 360;
            //未展开完全并且当前旋转角度小于180，逆转回去
            if (currRotate < 180) {
                rotate = 0;
            }
            if (pendingCause) {
                arrow.setRotation(rotate);
            } else {
                arrow.animate()
                     .rotation(rotate)
                     .setDuration(mItemAnimator.getRemoveDuration() + 180)
                     .start();
            }
        }
    }

}
