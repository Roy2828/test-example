package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCallback;
import com.example.myapplication.task.java.TaskContext;

/**
 * 业务节点 C：异步网络接口请求 (如 API 数据拉取)
 * 支持在 cancel() 时断开网络连接或移除网络回调句柄
 */
public class TaskNodeC extends AbstractTaskNode {
    private static final String TAG = "TaskNodeC";
    private Runnable networkCallRunnable;

    public TaskNodeC() {
        super("C", "方法 C (网络耗时接口请求)", true);
    }

    @Override
    public void execute(TaskContext context, TaskCallback callback) {
        String headerB = context.get("request_header_b");
        Log.d(TAG, "【节点 C】开始执行业务：发起异步 HTTP 网络请求，Header=" + headerB + "，等待响应...");

        networkCallRunnable = () -> {
            if (!isCancelled()) {
                Log.d(TAG, "【节点 C】网络请求成功返回：HTTP 200 OK");
                context.put("result_c", "C_User_Token_Response_JSON");
                callback.onSuccess("C 接口请求成功");
            } else {
                Log.w(TAG, "【节点 C】网络响应到达，但请求已被取消/跳过，忽略返回结果");
            }
        };

        // 模拟 3 秒异步网络延迟
        mainHandler.postDelayed(networkCallRunnable, 3000);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 C】触发业务取消：正在断开 HTTP 连接、注销网络监听回调...");
        if (networkCallRunnable != null) {
            mainHandler.removeCallbacks(networkCallRunnable);
            networkCallRunnable = null;
        }
    }
}
