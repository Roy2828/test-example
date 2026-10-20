package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCancelledException;
import com.example.myapplication.task.java.TaskContext;

public class TaskNodeC extends AbstractTaskNode {
    private static final String TAG = "TaskNodeC";
    private Runnable networkCallRunnable;

    public TaskNodeC() {
        super("C", "方法 C (网络耗时接口请求)", true);
    }

    @Override
    protected void doExecute(TaskContext context) {
        String headerB = context.get("request_header_b");
        Log.d(TAG, "【节点 C】开始执行业务：发起异步 HTTP 网络请求，Header=" + headerB + "...");

        networkCallRunnable = () -> {
            try {
                checkCancelled();
                Log.d(TAG, "【节点 C】网络请求成功返回：HTTP 200 OK");
                context.put("result_c", "C_User_Token_Response_JSON");
                notifySuccess("C 接口请求成功");
            } catch (TaskCancelledException e) {
                Log.w(TAG, "【节点 C】任务执行中抛出取消异常：" + e.getMessage());
            }
        };

        // 模拟 3 秒异步网络延迟
        mainHandler.postDelayed(networkCallRunnable, 3000);
    }

    @Override
    public void cancel() {
        super.cancel(); // 抛出异常立即通知引擎
        Log.w(TAG, "【节点 C】清理资源：断开 HTTP 连接并移除回调...");
        if (networkCallRunnable != null) {
            mainHandler.removeCallbacks(networkCallRunnable);
            networkCallRunnable = null;
        }
    }
}
