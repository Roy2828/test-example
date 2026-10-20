package com.example.myapplication.task.java;

import android.os.Handler;
import android.os.Looper;

/**
 * 抽象任务节点基类
 * 采用正规的 Callback + Exception 驱动模式，抛弃 raw Thread 控制。
 */
public abstract class AbstractTaskNode implements TaskNode {
    private final String id;
    private final String name;
    private final boolean isAsync;
    protected volatile boolean isCancelled = false;
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());
    private TaskCallback mCallback;

    public AbstractTaskNode(String id, String name, boolean isAsync) {
        this.id = id;
        this.name = name;
        this.isAsync = isAsync;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    @Override
    public boolean isAsync() { return isAsync; }

    @Override
    public final void execute(TaskContext context, TaskCallback callback) {
        this.isCancelled = false;
        this.mCallback = callback;
        try {
            // 将具体业务逻辑下放给子类
            doExecute(context);
        } catch (TaskCancelledException e) {
            notifyError(e);
        } catch (Exception e) {
            notifyError(e);
        }
    }

    /**
     * 子类实现该方法完成具体的业务逻辑
     */
    protected abstract void doExecute(TaskContext context) throws Exception;

    @Override
    public void cancel() {
        this.isCancelled = true;
        // 【核心】正常一点的异常驱动模式：在 cancel() 时直接将取消异常扔给回调，实现立即打断
        notifyError(new TaskCancelledException(this, "节点 [" + id + "] 业务被主动中断"));
    }

    @Override
    public void reset() {
        this.isCancelled = false;
        this.mCallback = null;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * 业务执行过程中随时检查，若已取消则主动抛出异常
     */
    protected void checkCancelled() throws TaskCancelledException {
        if (isCancelled) {
            throw new TaskCancelledException(this, "节点 [" + id + "] 检查到取消标志，抛出异常");
        }
    }

    /**
     * 成功回调保护（防止触发两次或在取消后触发）
     */
    protected void notifySuccess(Object result) {
        if (!isCancelled && mCallback != null) {
            TaskCallback cb = mCallback;
            mCallback = null; // 确保仅回调一次
            cb.onSuccess(result);
        }
    }

    /**
     * 失败/异常回调保护
     */
    protected void notifyError(Throwable error) {
        if (mCallback != null) {
            TaskCallback cb = mCallback;
            mCallback = null; // 确保仅回调一次
            cb.onError(error);
        }
    }
}
