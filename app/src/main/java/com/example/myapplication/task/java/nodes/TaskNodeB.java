package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCallback;
import com.example.myapplication.task.java.TaskContext;

/**
 * 业务节点 B：计算与加密组装网络请求参数 Header 和 Body
 */
public class TaskNodeB extends AbstractTaskNode {
    private static final String TAG = "TaskNodeB";
    private Runnable runnable;

    public TaskNodeB() {
        super("B", "方法 B (组装网络请求参数)", false);
    }

    @Override
    public void execute(TaskContext context, TaskCallback callback) {
        String configA = context.get("config_a");
        Log.d(TAG, "【节点 B】开始执行业务：基于 A 的配置 (" + configA + ") 加密组装请求 Token 与 Body...");

        runnable = () -> {
            if (!isCancelled()) {
                context.put("request_header_b", "BearerToken_Encrypted_From_A");
                Log.d(TAG, "【节点 B】业务处理完成！");
                callback.onSuccess("B 参数计算完成");
            } else {
                Log.w(TAG, "【节点 B】计算完成，但节点已被取消/中断");
            }
        };
        mainHandler.postDelayed(runnable, 1200);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 B】触发业务取消：中止参数加密计算");
        if (runnable != null) {
            mainHandler.removeCallbacks(runnable);
            runnable = null;
        }
    }
}
