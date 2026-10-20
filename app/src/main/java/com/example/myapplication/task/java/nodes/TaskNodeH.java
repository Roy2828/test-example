package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCallback;
import com.example.myapplication.task.java.TaskContext;

/**
 * 业务节点 H：业务收尾 (清理临时缓存、发送 APM 性能上报)
 */
public class TaskNodeH extends AbstractTaskNode {
    private static final String TAG = "TaskNodeH";
    private Runnable finishRunnable;

    public TaskNodeH() {
        super("H", "方法 H (业务收尾与指标上报)", false);
    }

    @Override
    public void execute(TaskContext context, TaskCallback callback) {
        Log.d(TAG, "【节点 H】开始执行业务：清理任务链临时变量，向服务端上报耗时性能埋点...");

        finishRunnable = () -> {
            if (!isCancelled()) {
                context.put("finish_status_h", "Entire_Task_Chain_Completed");
                Log.d(TAG, "【节点 H】业务处理完成！");
                callback.onSuccess("H 全部收尾工作完成");
            } else {
                Log.w(TAG, "【节点 H】收尾处理完成，但节点已被取消/中断");
            }
        };

        mainHandler.postDelayed(finishRunnable, 1200);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 H】触发业务取消：终止收尾清理与性能上报");
        if (finishRunnable != null) {
            mainHandler.removeCallbacks(finishRunnable);
            finishRunnable = null;
        }
    }
}
