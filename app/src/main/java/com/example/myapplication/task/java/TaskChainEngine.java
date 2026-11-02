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
 * 4. 基于 TaskCancelledException 抛异常方式的【立即取消与退栈机制】：
 *    任何环节 (A/B/C/D/E/F/G/H等) 都可以抛出 TaskCancelledException 实现立马取消，终止后续环节并精准回调给监听器。
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
     * 启动任务链，按照传入的节点顺序动态编排并执行
     * 传入的节点列表决定了当前这一次任务的执行顺序和节点数量。
     *
     * @param dynamicNodes 想要执行的动态节点集合，例如 Arrays.asList(A, C, F) 或 Arrays.asList(B, D, E)
     */
    public synchronized void start(List<TaskNode> dynamicNodes) {
        if (dynamicNodes == null || dynamicNodes.isEmpty()) {
            Log.w(TAG, "动态任务链为空，无法启动");
            return;
        }

        // 每次启动用全新的节点集合替换旧的，实现完全的动态化顺序调整
        setNodes(dynamicNodes);

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

        try {
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
                        // 防御：如果当前节点已经被抢占跳转过，那么旧节点的任何回调或异常直接被丢弃
                        if (currentNode != node) {
                            Log.d(TAG, "节点 [" + node.getId() + "] 回调失败或抛出异常，但其已被抢占跳转，忽略之。");
                            return;
                        }

                        // 如果抛出 TaskCancelledException 异常，说明触发了立即取消机制
                        if (throwable instanceof TaskCancelledException) {
                            TaskCancelledException cancelExp = (TaskCancelledException) throwable;
                            TaskNode canceledNode = cancelExp.getNode() != null ? cancelExp.getNode() : node;
                            String reason = cancelExp.getReason();

                            Log.w(TAG, "捕获到立即取消异常 TaskCancelledException！节点 [" + canceledNode.getId() + "] 触发立即中断退栈！");
                            isInterrupted.set(true);
                            isRunning.set(false);

                            if (listener != null) {
                                listener.onNodeInterrupted(canceledNode, reason);
                            }
                            return;
                        }

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
        } catch (TaskCancelledException e) {
            Log.w(TAG, "同步捕获到 TaskCancelledException 立即取消异常，中断流程: " + e.getMessage());
            isInterrupted.set(true);
            isRunning.set(false);

            postToMainThread(() -> {
                if (listener != null) {
                    listener.onNodeInterrupted(e.getNode() != null ? e.getNode() : node, e.getReason());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "节点 [" + node.getId() + "] 同步抛出未知异常: " + e.getMessage());
            isRunning.set(false);

            postToMainThread(() -> {
                if (listener != null) {
                    listener.onChainError(node, e);
                }
            });
        }
    }

    /**
     * 强行推进/跳转到指定节点 ID 
     * 如果当前任务链正在执行其他节点，将先停止当前节点并立即启动目标节点。
     * 
     * 注意：如果当前引擎并没有在运行任何任务（即处于空闲/执行完毕状态），
     * 应该拒绝抢占，因为引擎没有“运行时上下文”。如果想要单独执行 G，应该通过 start() 传入节点。
     *
     * @param targetNodeId 目标节点ID (例如 "G")
     */
    public synchronized boolean proceedToStep(String targetNodeId) {
        if (!isRunning.get()) {
            Log.e(TAG, "当前引擎未在运行状态，无法执行抢占跳转！");
            return false;
        }

        int targetIndex = findNodeIndex(targetNodeId);
        if (targetIndex == -1) {
            Log.e(TAG, "无法抢占/跳转，在当前链中找不到节点: " + targetNodeId);
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
            Log.w(TAG, "任务链在环节 [" + nodeToInterrupt.getId() + "] 被主动中断，原因: " + reason);
            nodeToInterrupt.cancel();
        } else {
            Log.w(TAG, "任务链被主动中断，原因: " + reason);
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
