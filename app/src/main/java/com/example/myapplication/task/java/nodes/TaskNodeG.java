package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCancelledException;
import com.example.myapplication.task.java.TaskContext;

public class TaskNodeG extends AbstractTaskNode {
    private static final String TAG = "TaskNodeG";
    private Runnable runnable;

    public TaskNodeG() {
        super("G", "方法 G (UI绑定与状态准备)", false);
    }

    @Override
    protected void doExecute(TaskContext context) {
        String skinF = context.get("resource_f");
        Log.d(TAG, "【节点 G】开始执行业务：准备构建界面 ViewState...");

        runnable = () -> {
            try {
                checkCancelled();
                context.put("ui_state_g", "ViewState_Render_Ready");
                Log.d(TAG, "【节点 G】业务处理完成！");
                notifySuccess("G UI准备完成");
            } catch (TaskCancelledException e) {
                Log.w(TAG, "【节点 G】捕获到取消异常：" + e.getMessage());
            }
        };

        mainHandler.postDelayed(runnable, 1200);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 G】清理资源：移除 Handler 回调");
        if (runnable != null) {
            mainHandler.removeCallbacks(runnable);
            runnable = null;
        }
    }
}
