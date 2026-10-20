package com.example.myapplication.task.java;

/**
 * 任务节点取消/中断异常
 * 用于在节点执行过程中通过抛出异常的方式实现立即中断与退栈
 */
public class TaskCancelledException extends RuntimeException {
    private final TaskNode node;
    private final String reason;

    public TaskCancelledException(TaskNode node, String reason) {
        super("任务节点 [" + (node != null ? node.getId() : "未知") + "] 立即取消: " + reason);
        this.node = node;
        this.reason = reason;
    }

    public TaskNode getNode() {
        return node;
    }

    public String getReason() {
        return reason;
    }
}
