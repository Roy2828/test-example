package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCallback;
import com.example.myapplication.task.java.TaskContext;

/**
 * 业务节点 F：第二个异步网络耗时任务 (下载图片/皮肤资源包)
 * 支持在 cancel() 时关闭下载 Task 并释放文件资源
 */
public class TaskNodeF extends AbstractTaskNode {
    private static final String TAG = "TaskNodeF";
    private Runnable downloadRunnable;

    public TaskNodeF() {
        super("F", "方法 F (网络资源下载)", true);
    }

    @Override
    public void execute(TaskContext context, TaskCallback callback) {
        Log.d(TAG, "【节点 F】开始执行业务：发起第二个网络 Task，下载远程皮肤图片资源...");

        downloadRunnable = () -> {
            if (!isCancelled()) {
                Log.d(TAG, "【节点 F】网络资源下载完成！");
                context.put("resource_f", "Downloaded_Skin_Resource_File");
                callback.onSuccess("F 资源下载完成");
            } else {
                Log.w(TAG, "【节点 F】资源下载完毕，但任务已被取消/跳过，丢弃文件句柄");
            }
        };

        // 模拟 3 秒网络下载耗时
        mainHandler.postDelayed(downloadRunnable, 3000);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 F】触发业务取消：正在终止下载线程、关闭临时文件输出流...");
        if (downloadRunnable != null) {
            mainHandler.removeCallbacks(downloadRunnable);
            downloadRunnable = null;
        }
    }
}
