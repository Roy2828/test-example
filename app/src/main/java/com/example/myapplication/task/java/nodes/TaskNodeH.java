package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCancelledException;
import com.example.myapplication.task.java.TaskContext;

public class TaskNodeH extends AbstractTaskNode {
    private static final String TAG = "TaskNodeH";
    private Runnable runnable;

    public TaskNodeH() {
        super("H", "方法 H (业务收尾与指标上报)", false);
    }

    @Override
    protected void doExecute(TaskContext context) {
        Log.d(TAG, "【节点 H】开始执行业务：清理任务链临时变量并上报埋点...");

        runnable = () -> {
            try {
                checkCancelled();
                context.put("finish_status_h", "Entire_Task_Chain_Completed");
                Log.d(TAG, "【节点 H】业务处理完成！");
                notifySuccess("H 全部收尾工作完成");
            } catch (TaskCancelledException e) {
                Log.w(TAG, "【节点 H】捕获到取消异常：" + e.getMessage());
            }
        };

        mainHandler.postDelayed(runnable, 1200);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 H】清理资源：移除 Handler 回调");
        if (runnable != null) {
            mainHandler.removeCallbacks(runnable);
            runnable = null;
        }
    }
}
