package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCancelledException;
import com.example.myapplication.task.java.TaskContext;

public class TaskNodeE extends AbstractTaskNode {
    private static final String TAG = "TaskNodeE";
    private Runnable runnable;

    public TaskNodeE() {
        super("E", "方法 E (数据持久化缓存)", false);
    }

    @Override
    protected void doExecute(TaskContext context) {
        String dataD = context.get("parsed_data_d");
        Log.d(TAG, "【节点 E】开始执行业务：将 D 的数据 (" + dataD + ") 写入缓存...");

        runnable = () -> {
            try {
                checkCancelled();
                context.put("cache_status_e", "Disk_Memory_Cache_OK");
                Log.d(TAG, "【节点 E】业务处理完成！");
                notifySuccess("E 数据缓存完成");
            } catch (TaskCancelledException e) {
                Log.w(TAG, "【节点 E】捕获到取消异常：" + e.getMessage());
            }
        };

        mainHandler.postDelayed(runnable, 1200);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 E】清理资源：移除 Handler 回调");
        if (runnable != null) {
            mainHandler.removeCallbacks(runnable);
            runnable = null;
        }
    }
}
