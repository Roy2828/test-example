package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCancelledException;
import com.example.myapplication.task.java.TaskContext;

public class TaskNodeF extends AbstractTaskNode {
    private static final String TAG = "TaskNodeF";
    private Runnable downloadRunnable;

    public TaskNodeF() {
        super("F", "方法 F (网络资源下载)", true);
    }

    @Override
    protected void doExecute(TaskContext context) {
        Log.d(TAG, "【节点 F】开始执行业务：发起第二个网络 Task，下载远程资源...");

        downloadRunnable = () -> {
            try {
                checkCancelled();
                Log.d(TAG, "【节点 F】网络资源下载完成！");
                context.put("resource_f", "Downloaded_Skin_Resource_File");
                notifySuccess("F 资源下载完成");
            } catch (TaskCancelledException e) {
                Log.w(TAG, "【节点 F】任务执行中抛出取消异常：" + e.getMessage());
            }
        };

        mainHandler.postDelayed(downloadRunnable, 3000);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 F】清理资源：断开下载连接并移除回调...");
        if (downloadRunnable != null) {
            mainHandler.removeCallbacks(downloadRunnable);
            downloadRunnable = null;
        }
    }
}
