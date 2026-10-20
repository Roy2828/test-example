package com.example.myapplication.task.java;

import com.example.myapplication.task.java.nodes.TaskNodeA;
import com.example.myapplication.task.java.nodes.TaskNodeB;
import com.example.myapplication.task.java.nodes.TaskNodeC;
import com.example.myapplication.task.java.nodes.TaskNodeD;
import com.example.myapplication.task.java.nodes.TaskNodeE;
import com.example.myapplication.task.java.nodes.TaskNodeF;
import com.example.myapplication.task.java.nodes.TaskNodeG;
import com.example.myapplication.task.java.nodes.TaskNodeH;

import java.util.ArrayList;
import java.util.List;

/**
 * 组装真实的独立业务节点集合 (A -> B -> C -> D -> E -> F -> G -> H)
 * 每个节点对应一个独立的业务实现类，每个类拥有专有的业务逻辑与独立的 cancel() 取消处理机制。
 */
public class SimulatedTaskNodes {

    public static List<TaskNode> createSampleNodes() {
        List<TaskNode> list = new ArrayList<>();
        list.add(new TaskNodeA());
        list.add(new TaskNodeB());
        list.add(new TaskNodeC());
        list.add(new TaskNodeD());
        list.add(new TaskNodeE());
        list.add(new TaskNodeF());
        list.add(new TaskNodeG());
        list.add(new TaskNodeH());
        return list;
    }
}
