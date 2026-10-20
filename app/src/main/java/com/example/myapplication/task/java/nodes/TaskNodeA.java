package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCallback;
import com.example.myapplication.task.java.TaskContext;

/**
 * 业务节点 A：读取与初始化本地配置
 */
public class TaskNodeA extends AbstractTaskNode {
    private static final String TAG = "TaskNodeA";
    private Runnable runnable;

    public TaskNodeA() {
        super("A", "方法 A (初始化本地配置)", false);
    }

    @Override
    public void execute(TaskContext context, TaskCallback callback) {
        Log.d(TAG, "【节点 A】开始执行业务：读取本地文件并初始化配置...");
        runnable = () -> {
            if (!isCancelled()) {
                context.put("config_a", "LocalConfig_Loaded_OK");
                Log.d(TAG, "【节点 A】业务处理完成！");
                callback.onSuccess("A 配置初始化完成");
            } else {
                Log.w(TAG, "【节点 A】处理完成，但节点已被取消/中断");
            }
        };
        mainHandler.postDelayed(runnable, 1200);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 A】触发业务取消：清除本地配置初始化任务");
        if (runnable != null) {
            mainHandler.removeCallbacks(runnable);
            runnable = null;
        }
    }
}
