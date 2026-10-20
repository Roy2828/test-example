package com.example.myapplication.task.java;

/**
 * 任务节点接口
 */
public interface TaskNode {
    /**
     * 获取节点唯一标识 (例如: "A", "B", "C", "D", "E", "F")
     */
    String getId();

    /**
     * 获取节点显示名称
     */
    String getName();

    /**
     * 是否为异步/网络耗时任务
     */
    boolean isAsync();

    /**
     * 执行当前节点任务
     *
     * @param context  任务链上下文数据
     * @param callback 任务执行回调
     */
    void execute(TaskContext context, TaskCallback callback);

    /**
     * 取消/停止当前节点任务
     * 当被抢占(如C被停止跳到D)或任务链在任意环节被中断时调用
     */
    void cancel();

    /**
     * 重置节点状态
     */
    void reset();
}
