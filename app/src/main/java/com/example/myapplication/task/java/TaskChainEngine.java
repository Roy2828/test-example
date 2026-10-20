package com.example.myapplication.task.java;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Java版任务链控制器引擎
 * 支持：
 * 1. 顺序任务推进 (A -> B -> C -> D -> E -> F -> G -> H)
 * 2. 异步网络节点耗时等待与自动下一环节推进
 * 3. 异步网络节点被强行抢占/跳过 (例如: C在网络请求中，外部强行触发D，能够停止C并立即执行D及后续环节)
 * 4. 任意环节 (A/B/C/D/E/F/G/H等) 都可以被中断，中断后不再执行后续环节，并准确回调告知被中断的节点
 */
public class TaskChainEngine {
    private static final String TAG = "TaskChainEngine";

    private final List<TaskNode> nodes = new ArrayList<>();
    private final TaskContext context = new TaskContext();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ChainListener listener;
    private volatile int currentIndex = -1;
    private volatile TaskNode currentNode;
    private final AtomicBoolean isInterrupted = new AtomicBoolean(false);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public TaskChainEngine addNode(TaskNode node) {
        if (node != null) {
            nodes.add(node);
        }
        return this;
    }

    public TaskChainEngine setNodes(List<TaskNode> taskNodes) {
        nodes.clear();
        if (taskNodes != null) {
            nodes.addAll(taskNodes);
        }
        return this;
    }

    public TaskChainEngine setListener(ChainListener listener) {
        this.listener = listener;
        return this;
    }

    public TaskContext getContext() {
        return context;
    }

    public TaskNode getCurrentNode() {
        return currentNode;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public boolean isInterrupted() {
        return isInterrupted.get();
    }

    /**
     * 启动任务链，从第一个节点(Index 0 / Node A)开始顺序执行
     */
    public synchronized void start() {
        if (nodes.isEmpty()) {
            Log.w(TAG, "任务链为空，无法启动");
            return;
        }

        // 重置状态
        isInterrupted.set(false);
        isRunning.set(true);
        currentIndex = 0;
        context.clear();

        for (TaskNode node : nodes) {
            node.reset();
        }

        postToMainThread(() -> {
            if (listener != null) {
                listener.onChainStart();
            }
        });

        executeNode(currentIndex);
    }

    /**
     * 执行指定索引位置的节点
     */
    private synchronized void executeNode(int index) {
        if (isInterrupted.get() || !isRunning.get()) {
            Log.d(TAG, "任务链已被中断或停止，终止推进: index=" + index);
            return;
        }

        if (index < 0 || index >= nodes.size()) {
            isRunning.set(false);
            postToMainThread(() -> {
                if (listener != null) {
                    listener.onChainCompleted(context);
                }
            });
            return;
        }

        currentIndex = index;
        TaskNode node = nodes.get(index);
        currentNode = node;
        node.reset();

        Log.d(TAG, "开始执行节点: [" + node.getId() + "] " + node.getName());

        postToMainThread(() -> {
            if (listener != null) {
                listener.onNodeStart(node);
            }
        });

        node.execute(context, new TaskCallback() {
            @Override
            public void onSuccess(Object result) {
                postToMainThread(() -> {
                    // 如果该节点已取消或任务链已中断，忽略此回调
                    if (isInterrupted.get() || !isRunning.get() || currentNode != node) {
                        Log.d(TAG, "节点 [" + node.getId() + "] 回调成功但已被忽略(已中断/已跳转)");
                        return;
                    }

                    Log.d(TAG, "节点 [" + node.getId() + "] 执行成功，推进到下一个节点");

                    if (listener != null) {
                        listener.onNodeSuccess(node, result);
                    }

                    // 推进到下一个节点
                    executeNode(index + 1);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                postToMainThread(() -> {
                    if (isInterrupted.get() || !isRunning.get() || currentNode != node) {
                        return;
                    }

                    isRunning.set(false);
                    Log.e(TAG, "节点 [" + node.getId() + "] 执行失败: " + throwable.getMessage());

                    if (listener != null) {
                        listener.onChainError(node, throwable);
                    }
                });
            }
        });
    }

    /**
     * 强行推进/跳转到指定节点 ID (例如: 在执行C网络请求时，强行触发执行D方法)
     * 这会先停止当前正在运行的节点(如C)，然后再立即启动目标节点(如D)，并继续后续流程(D -> E -> F...)
     *
     * @param targetNodeId 目标节点ID (例如 "D")
     */
    public synchronized boolean proceedToStep(String targetNodeId) {
        int targetIndex = findNodeIndex(targetNodeId);
        if (targetIndex == -1) {
            Log.e(TAG, "无法抢占/跳转，找不到节点: " + targetNodeId);
            return false;
        }

        TaskNode previousNode = currentNode;
        if (previousNode != null) {
            Log.d(TAG, "抢占跳转：停止当前节点 [" + previousNode.getId() + "]，跳转到 [" + targetNodeId + "]");
            previousNode.cancel();

            postToMainThread(() -> {
                if (listener != null) {
                    listener.onNodePreempted(previousNode, targetNodeId);
                }
            });
        }

        isInterrupted.set(false);
        isRunning.set(true);

        executeNode(targetIndex);
        return true;
    }

    /**
     * 中断任务链
     * 关键条件：任何环节 (A, B, C, D, E, F, G, H等) 都可以被中断。
     * 中断后不能继续下一个环节，并准确回调给出是哪个环节中断了。
     *
     * @param reason 中断原因
     */
    public synchronized void interrupt(String reason) {
        if (!isRunning.get() && isInterrupted.get()) {
            return;
        }

        isInterrupted.set(true);
        isRunning.set(false);

        TaskNode nodeToInterrupt = currentNode;
        if (nodeToInterrupt != null) {
            Log.w(TAG, "任务链在环节 [" + nodeToInterrupt.getId() + "] 被中断，原因: " + reason);
            nodeToInterrupt.cancel();
        } else {
            Log.w(TAG, "任务链被中断，原因: " + reason);
        }

        postToMainThread(() -> {
            if (listener != null) {
                listener.onNodeInterrupted(nodeToInterrupt, reason);
            }
        });
    }

    /**
     * 查找节点的索引
     */
    private int findNodeIndex(String nodeId) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId().equalsIgnoreCase(nodeId)) {
                return i;
            }
        }
        return -1;
    }

    private void postToMainThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }
}
