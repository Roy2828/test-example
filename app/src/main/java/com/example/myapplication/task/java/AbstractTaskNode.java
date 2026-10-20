package com.example.myapplication.task.java;

import android.os.Handler;
import android.os.Looper;

/**
 * 抽象任务节点基类
 */
public abstract class AbstractTaskNode implements TaskNode {
    private final String id;
    private final String name;
    private final boolean isAsync;
    protected volatile boolean isCancelled = false;
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AbstractTaskNode(String id, String name, boolean isAsync) {
        this.id = id;
        this.name = name;
        this.isAsync = isAsync;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isAsync() {
        return isAsync;
    }

    @Override
    public void cancel() {
        this.isCancelled = true;
    }

    @Override
    public void reset() {
        this.isCancelled = false;
    }

    public boolean isCancelled() {
        return isCancelled;
    }
}
