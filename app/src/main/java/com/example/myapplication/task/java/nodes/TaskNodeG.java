package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCallback;
import com.example.myapplication.task.java.TaskContext;

/**
 * 业务节点 G：UI ViewState 构建与数据绑定准备
 */
public class TaskNodeG extends AbstractTaskNode {
    private static final String TAG = "TaskNodeG";
    private Runnable uiRunnable;

    public TaskNodeG() {
        super("G", "方法 G (UI绑定与状态准备)", false);
    }

    @Override
    public void execute(TaskContext context, TaskCallback callback) {
        String skinF = context.get("resource_f");
        Log.d(TAG, "【节点 G】开始执行业务：准备构建界面 ViewState (包含资源=" + skinF + ")...");

        uiRunnable = () -> {
            if (!isCancelled()) {
                context.put("ui_state_g", "ViewState_Render_Ready");
                Log.d(TAG, "【节点 G】业务处理完成！");
                callback.onSuccess("G UI准备完成");
            } else {
                Log.w(TAG, "【节点 G】构建完成，但节点已被取消/中断");
            }
        };

        mainHandler.postDelayed(uiRunnable, 1200);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 G】触发业务取消：终止 UI ViewState 构建");
        if (uiRunnable != null) {
            mainHandler.removeCallbacks(uiRunnable);
            uiRunnable = null;
        }
    }
}
