package com.example.myapplication.task.java;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务链共享上下文数据，支持线程安全存取
 */
public class TaskContext {
    private final Map<String, Object> dataMap = new ConcurrentHashMap<>();

    public void put(String key, Object value) {
        if (key != null && value != null) {
            dataMap.put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) dataMap.get(key);
    }

    public boolean containsKey(String key) {
        return dataMap.containsKey(key);
    }

    public Map<String, Object> getAllData() {
        return dataMap;
    }

    public void clear() {
        dataMap.clear();
    }
}
