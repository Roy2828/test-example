package com.example.myapplication.task.java;

/**
 * 任务节点执行回调
 */
public interface TaskCallback {
    /**
     * 节点任务执行成功
     *
     * @param result 节点输出结果
     */
    void onSuccess(Object result);

    /**
     * 节点任务执行失败/发生异常
     *
     * @param throwable 异常原因
     */
    void onError(Throwable throwable);
}
