package com.example.myapplication.task.java;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.task.java.nodes.TaskNodeA;
import com.example.myapplication.task.java.nodes.TaskNodeB;
import com.example.myapplication.task.java.nodes.TaskNodeC;
import com.example.myapplication.task.java.nodes.TaskNodeD;
import com.example.myapplication.task.java.nodes.TaskNodeE;
import com.example.myapplication.task.java.nodes.TaskNodeF;
import com.example.myapplication.task.java.nodes.TaskNodeG;
import com.example.myapplication.task.java.nodes.TaskNodeH;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Java 任务链验证 Activity
 * 验证场景：
 * 1. A -> B -> C(网络耗时) -> D -> E -> F(网络耗时) -> G -> H 顺序链式调度
 * 2. C正在执行网络请求时，强行触发执行D，停止C并跳转到D，之后顺利完成 D->E->F->G->H
 * 3. 任何环节 (A, B, C, D, E, F, G, H等) 都可以被中断，中断后不再进行下一个环节，并给出具体哪个环节中断的回调
 */
public class JavaTaskChainActivity extends AppCompatActivity implements ChainListener {

    public static void start(Context context) {
        Intent intent = new Intent(context, JavaTaskChainActivity.class);
        context.startActivity(intent);
    }

    private TextView tvStatusLabel;
    private TextView tvStatusDetail;
    private TextView tvLog;
    private ScrollView scrollLog;

    private Button btnStartTest;
    private Button btnStartTest2;
    private Button btnStartCustom;
    private Button btnTriggerG;

    private Button btnInterruptCurrent;
    private Button btnInterruptA;
    private Button btnInterruptB;
    private Button btnInterruptC;
    private Button btnInterruptD;
    private Button btnInterruptE;
    private Button btnInterruptF;
    private Button btnInterruptG;
    private Button btnClearLog;

    private TaskChainEngine engine;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_task_chain);

        initViews();
        initTaskEngine();
        setupListeners();
    }

    private void initViews() {
        tvStatusLabel = findViewById(R.id.tv_status_label);
        tvStatusDetail = findViewById(R.id.tv_status_detail);
        tvLog = findViewById(R.id.tv_log);
        scrollLog = findViewById(R.id.scroll_log);

        btnStartTest = findViewById(R.id.btn_start_chain_test);
        btnStartTest2 = findViewById(R.id.btn_start_chain_test2);
        btnStartCustom = findViewById(R.id.btn_start_chain_custom);
        btnTriggerG = findViewById(R.id.btn_trigger_g);

        btnInterruptCurrent = findViewById(R.id.btn_interrupt_current);
        btnInterruptA = findViewById(R.id.btn_interrupt_a);
        btnInterruptB = findViewById(R.id.btn_interrupt_b);
        btnInterruptC = findViewById(R.id.btn_interrupt_c);
        btnInterruptD = findViewById(R.id.btn_interrupt_d);
        btnInterruptE = findViewById(R.id.btn_interrupt_e);
        btnInterruptF = findViewById(R.id.btn_interrupt_f);
        btnInterruptG = findViewById(R.id.btn_interrupt_g);
        btnClearLog = findViewById(R.id.btn_clear_log);
    }

    private void initTaskEngine() {
        engine = new TaskChainEngine();
        engine.setListener(this);
    }

    private void setupListeners() {
        // 动态组合并执行 test() : A -> B -> C -> F
        btnStartTest.setOnClickListener(v -> {
            appendLog("======================");
            appendLog("【启动指令】执行动态组合 test() : A -> B -> C -> F");
            engine.start(Arrays.asList(
                    new TaskNodeA(),
                    new TaskNodeB(),
                    new TaskNodeC(),
                    new TaskNodeF()
            ));
        });

        // 动态组合并执行 test2() : B -> C -> E -> G
        btnStartTest2.setOnClickListener(v -> {
            appendLog("======================");
            appendLog("【启动指令】执行动态组合 test2() : B -> C -> E -> G");
            engine.start(Arrays.asList(
                    new TaskNodeB(),
                    new TaskNodeC(),
                    new TaskNodeE(),
                    new TaskNodeG()
            ));
        });

        // 随意乱序组合 : D -> H -> A
        btnStartCustom.setOnClickListener(v -> {
            appendLog("======================");
            appendLog("【启动指令】执行动态乱序组合 : D -> H -> A");
            engine.start(Arrays.asList(
                    new TaskNodeD(),
                    new TaskNodeH(),
                    new TaskNodeA()
            ));
        });

        // 新增：测试抢占执行 G
        btnTriggerG.setOnClickListener(v -> {
            appendLog("【抢占指令】请求强行执行节点 G");
            boolean success = engine.proceedToStep("G");
            if (!success) {
                if (!engine.isRunning()) {
                    Toast.makeText(this, "当前引擎未运行，抢占失败！请先点击『执行 test2()』然后再点抢占。", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "当前任务链中不包含 G 节点，无法跳转！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 4. 中断当前运行的环节
        btnInterruptCurrent.setOnClickListener(v -> interruptSpecificNode(null));

        // 5. 中断具体环节 (A ~ G)
        btnInterruptA.setOnClickListener(v -> interruptSpecificNode("A"));
        btnInterruptB.setOnClickListener(v -> interruptSpecificNode("B"));
        btnInterruptC.setOnClickListener(v -> interruptSpecificNode("C"));
        btnInterruptD.setOnClickListener(v -> interruptSpecificNode("D"));
        btnInterruptE.setOnClickListener(v -> interruptSpecificNode("E"));
        btnInterruptF.setOnClickListener(v -> interruptSpecificNode("F"));
        btnInterruptG.setOnClickListener(v -> interruptSpecificNode("G"));

        // 6. 清空日志
        btnClearLog.setOnClickListener(v -> tvLog.setText("--- 日志已清空 ---\n"));
    }

    private void interruptSpecificNode(String targetNodeId) {
        TaskNode currentNode = engine.getCurrentNode();
        if (targetNodeId == null) {
            String nodeInfo = currentNode != null ? "[" + currentNode.getId() + "] " + currentNode.getName() : "无";
            appendLog("【中断指令】手动中断当前运行环节: " + nodeInfo);
            engine.interrupt("点击【中断当前环节】");
        } else {
            appendLog("【中断指令】请求中断指定节点 [" + targetNodeId + "]");
            engine.interrupt("触发指定中断条件：节点 [" + targetNodeId + "] 被手动打断");
        }
    }

    // ==================== ChainListener 回调实现 ====================

    @Override
    public void onChainStart() {
        updateStatus("RUNNING", "#2196F3", "任务链已启动，正从节点 A 开始顺序推进...");
        appendLog("[链事件] 🚀 任务链已启动");
    }

    @Override
    public void onNodeStart(TaskNode node) {
        String asyncFlag = node.isAsync() ? "【网络耗时任务】" : "【任务环节】";
        updateStatus("RUNNING", "#2196F3", "正在执行: [" + node.getId() + "] " + node.getName() + " " + asyncFlag);
        appendLog("[节点开始] ▶️ [" + node.getId() + "] " + node.getName() + " " + asyncFlag);
    }

    @Override
    public void onNodeSuccess(TaskNode node, Object result) {
        appendLog("[节点成功] ✅ [" + node.getId() + "] " + node.getName() + " 完成，结果: " + result);
    }

    @Override
    public void onNodePreempted(TaskNode node, String targetNodeId) {
        updateStatus("PREEMPTED", "#FF9800", "节点 [" + node.getId() + "] 已被抢占，直接跳转到 [" + targetNodeId + "]");
        appendLog("[节点抢占/取消] ⏩ 节点 [" + node.getId() + "] (" + node.getName() + ") 被停止/取消，任务链跳转至节点 [" + targetNodeId + "]");
    }

    @Override
    public void onNodeInterrupted(TaskNode node, String reason) {
        String nodeName = node != null ? "[" + node.getId() + "] " + node.getName() : "未确定节点";
        updateStatus("INTERRUPTED", "#F44336", "环节 " + nodeName + " 中断！流程终止");
        appendLog("--------------------------------------------------");
        appendLog("【中断回调通知】 ⚠️ 任务链在环节 " + nodeName + " 被中断！");
        appendLog("【中断原因】: " + reason);
        appendLog("【后果说明】: 任务链终止，后续环节绝对不会执行！");
        appendLog("--------------------------------------------------");
    }

    @Override
    public void onChainCompleted(TaskContext context) {
        updateStatus("COMPLETED", "#4CAF50", "整条任务链正常处理完成！");
        appendLog("[链事件] 🎉 全流程 (A->B->C->D->E->F->G->H) 顺利执行完毕！");
    }

    @Override
    public void onChainError(TaskNode node, Throwable error) {
        String nodeName = node != null ? "[" + node.getId() + "]" : "未知";
        updateStatus("ERROR", "#D32F2F", "节点 " + nodeName + " 发生错误: " + error.getMessage());
        appendLog("[节点错误] ❌ 节点 " + nodeName + " 报错: " + error.getMessage());
    }

    // ==================== UI 辅助方法 ====================

    private void updateStatus(String statusText, String colorHex, String detailText) {
        tvStatusLabel.setText(statusText);
        tvStatusLabel.setBackgroundColor(Color.parseColor(colorHex));
        tvStatusDetail.setText(detailText);
    }

    private void appendLog(String log) {
        String time = timeFormat.format(new Date());
        tvLog.append("[" + time + "] " + log + "\n");
        scrollLog.post(() -> scrollLog.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
