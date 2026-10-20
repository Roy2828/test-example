package com.example.myapplication.task.java;

/**
 * 任务链全生命周期监听器
 */
public interface ChainListener {
    /**
     * 任务链启动
     */
    void onChainStart();

    /**
     * 某个节点开始执行
     *
     * @param node 当前节点
     */
    void onNodeStart(TaskNode node);

    /**
     * 某个节点正常完成
     *
     * @param node   当前节点
     * @param result 输出数据
     */
    void onNodeSuccess(TaskNode node, Object result);

    /**
     * 某个节点被中断/取消
     * 关键需求：任意环节(A/B/C/D/E/F/G/H等)被中断都会回调，并告知具体是哪个环节中断了
     *
     * @param node   被中断的节点
     * @param reason 中断原因说明
     */
    void onNodeInterrupted(TaskNode node, String reason);

    /**
     * 节点被抢占/提前停止 (例如: C在网络请求中被强行停止，直接跳去执行D)
     *
     * @param node         被抢占/停止的节点 (如 C)
     * @param targetNodeId 提前跳转的目标节点 ID (如 "D")
     */
    void onNodePreempted(TaskNode node, String targetNodeId);

    /**
     * 整条任务链正常完成
     *
     * @param context 上下文数据
     */
    void onChainCompleted(TaskContext context);

    /**
     * 任务链发生错误
     *
     * @param node  出错节点
     * @param error 异常信息
     */
    void onChainError(TaskNode node, Throwable error);
}
