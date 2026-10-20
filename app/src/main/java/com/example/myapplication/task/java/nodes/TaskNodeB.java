package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCancelledException;
import com.example.myapplication.task.java.TaskContext;

public class TaskNodeB extends AbstractTaskNode {
    private static final String TAG = "TaskNodeB";
    private Runnable runnable;

    public TaskNodeB() {
        super("B", "方法 B (组装网络请求参数)", false);
    }

    @Override
    protected void doExecute(TaskContext context) {
        String configA = context.get("config_a");
        Log.d(TAG, "【节点 B】开始执行业务：基于 A 的配置 (" + configA + ") 加密组装请求参数...");

        runnable = () -> {
            try {
                checkCancelled();
                context.put("request_header_b", "BearerToken_Encrypted_From_A");
                Log.d(TAG, "【节点 B】业务处理完成！");
                notifySuccess("B 参数计算完成");
            } catch (TaskCancelledException e) {
                Log.w(TAG, "【节点 B】捕获到取消异常：" + e.getMessage());
            }
        };

        mainHandler.postDelayed(runnable, 1200);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 B】清理资源：移除 Handler 回调");
        if (runnable != null) {
            mainHandler.removeCallbacks(runnable);
            runnable = null;
        }
    }
}
