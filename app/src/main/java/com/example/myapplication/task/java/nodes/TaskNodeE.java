package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCallback;
import com.example.myapplication.task.java.TaskContext;

/**
 * 业务节点 E：数据持久化与磁盘/内存缓存
 */
public class TaskNodeE extends AbstractTaskNode {
    private static final String TAG = "TaskNodeE";
    private Runnable cacheRunnable;

    public TaskNodeE() {
        super("E", "方法 E (数据持久化缓存)", false);
    }

    @Override
    public void execute(TaskContext context, TaskCallback callback) {
        String dataD = context.get("parsed_data_d");
        Log.d(TAG, "【节点 E】开始执行业务：将 D 的数据 (" + dataD + ") 写入 DiskLruCache 和 Memory Cache...");

        cacheRunnable = () -> {
            if (!isCancelled()) {
                context.put("cache_status_e", "Disk_Memory_Cache_OK");
                Log.d(TAG, "【节点 E】业务处理完成！");
                callback.onSuccess("E 数据缓存完成");
            } else {
                Log.w(TAG, "【节点 E】缓存完成，但节点已被取消/中断");
            }
        };

        mainHandler.postDelayed(cacheRunnable, 1200);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 E】触发业务取消：终止磁盘 IO 写入与缓存事务");
        if (cacheRunnable != null) {
            mainHandler.removeCallbacks(cacheRunnable);
            cacheRunnable = null;
        }
    }
}
