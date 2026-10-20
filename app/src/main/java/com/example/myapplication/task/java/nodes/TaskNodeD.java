package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCancelledException;
import com.example.myapplication.task.java.TaskContext;

public class TaskNodeD extends AbstractTaskNode {
    private static final String TAG = "TaskNodeD";
    private Runnable runnable;

    public TaskNodeD() {
        super("D", "方法 D (数据解析与接管)", false);
    }

    @Override
    protected void doExecute(TaskContext context) {
        String resultC = context.get("result_c");
        if (resultC != null) {
            Log.d(TAG, "【节点 D】开始执行业务：解析 C 接口返回的数据 => " + resultC);
        } else {
            Log.d(TAG, "【节点 D】开始执行业务：未检测到 C 的数据 (说明C已被停止抢占)，D 直接接管");
        }

        runnable = () -> {
            try {
                checkCancelled();
                context.put("parsed_data_d", "D_Parsed_Model_Object");
                Log.d(TAG, "【节点 D】业务处理完成！");
                notifySuccess("D 数据解析与接管成功");
            } catch (TaskCancelledException e) {
                Log.w(TAG, "【节点 D】捕获到取消异常：" + e.getMessage());
            }
        };

        mainHandler.postDelayed(runnable, 1200);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 D】清理资源：移除 Handler 回调");
        if (runnable != null) {
            mainHandler.removeCallbacks(runnable);
            runnable = null;
        }
    }
}
