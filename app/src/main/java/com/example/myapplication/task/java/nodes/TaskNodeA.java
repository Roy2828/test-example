package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCancelledException;
import com.example.myapplication.task.java.TaskContext;

public class TaskNodeA extends AbstractTaskNode {
    private static final String TAG = "TaskNodeA";
    private Runnable runnable;

    public TaskNodeA() {
        super("A", "方法 A (初始化本地配置)", false);
    }

    @Override
    protected void doExecute(TaskContext context) {
        Log.d(TAG, "【节点 A】开始执行业务：读取本地文件并初始化配置...");
        
        runnable = () -> {
            try {
                checkCancelled(); // 抛异常
                context.put("config_a", "LocalConfig_Loaded_OK");
                Log.d(TAG, "【节点 A】业务处理完成！");
                notifySuccess("A 配置初始化完成");
            } catch (TaskCancelledException e) {
                Log.w(TAG, "【节点 A】捕获到取消异常：" + e.getMessage());
            }
        };
        
        mainHandler.postDelayed(runnable, 1200);
    }

    @Override
    public void cancel() {
        super.cancel(); // 抛出异常立即通知引擎
        Log.w(TAG, "【节点 A】清理资源：移除 Handler 回调");
        if (runnable != null) {
            mainHandler.removeCallbacks(runnable);
            runnable = null;
        }
    }
}
