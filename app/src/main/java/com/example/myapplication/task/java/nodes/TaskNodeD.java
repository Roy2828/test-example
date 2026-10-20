package com.example.myapplication.task.java.nodes;

import android.util.Log;

import com.example.myapplication.task.java.AbstractTaskNode;
import com.example.myapplication.task.java.TaskCallback;
import com.example.myapplication.task.java.TaskContext;

/**
 * 业务节点 D：数据解析与逻辑接管
 * 既能正常解析 C 节点返回的数据，也能在 C 被强行停止/抢占跳过时接管后续业务
 */
public class TaskNodeD extends AbstractTaskNode {
    private static final String TAG = "TaskNodeD";
    private Runnable parseRunnable;

    public TaskNodeD() {
        super("D", "方法 D (数据解析与接管)", false);
    }

    @Override
    public void execute(TaskContext context, TaskCallback callback) {
        String resultC = context.get("result_c");
        if (resultC != null) {
            Log.d(TAG, "【节点 D】开始执行业务：解析 C 接口返回的数据 => " + resultC);
        } else {
            Log.d(TAG, "【节点 D】开始执行业务：未检测到 C 的数据 (说明C已被停止抢占)，D 直接接管并生成默认兜底数据");
        }

        parseRunnable = () -> {
            if (!isCancelled()) {
                context.put("parsed_data_d", "D_Parsed_Model_Object");
                Log.d(TAG, "【节点 D】业务处理完成！");
                callback.onSuccess("D 数据解析与接管成功");
            } else {
                Log.w(TAG, "【节点 D】解析完成，但节点已被取消/中断");
            }
        };

        mainHandler.postDelayed(parseRunnable, 1200);
    }

    @Override
    public void cancel() {
        super.cancel();
        Log.w(TAG, "【节点 D】触发业务取消：终止数据解析任务");
        if (parseRunnable != null) {
            mainHandler.removeCallbacks(parseRunnable);
            parseRunnable = null;
        }
    }
}
