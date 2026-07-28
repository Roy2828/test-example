---
name: android-code-standards
description: "Android 核心编码规范、架构指南与性能准则。在编写、重构 Kotlin/Java/JNI 代码或处理高频数据流时必须严格遵守。"
---

# Android 核心编码与架构规范 (CodeBuddy Rules)

当为当前项目生成、修改或重构代码时，必须强制遵守以下规则：

---

## 一、 性能与内存规范（防卡顿与 GC 抖动）

1. **零内存抖动原则（高频场景）：**
   - **严禁对象频繁创建**：在高频回调（如 BLE/串口数据上报、传感器 20Hz+ 数据流、`Canvas` 绘制、`onDraw`）中，**绝对禁止 `new` 任何对象**（包括 `ByteArray`、`String`、`ArrayList` 等）。
   - **缓冲区复用**：必须复用成员变量中的 `ByteBuffer`、`RingBuffer` 或固定大小的 `ByteArray`。

2. **字节与符号安全（Hardware / Protocol）：**
   - **无符号转换**：所有单字节（Byte）拼接或组合成 `Int`/`Short` 的位运算，必须显式做无符号转换：`(b.toInt() and 0xFF)`。
   - **字节序显式声明**：涉及多字节解析时，必须在代码中明确指定大端序（Big-Endian）或小端序（Little-Endian）。

3. **线程与 Choreographer 隔离：**
   - **主线程零耗时**：严禁在主线程（`Dispatchers.Main`）做字节拆包、CRC 校验、文件/数据库读写或复杂逻辑计算。
   - **显式调度**：数据解析必须强制调度至 `Dispatchers.Default` 或专用的 `HandlerThread` / `Executors`。

---

## 二、 架构与状态管理规范（Clean Architecture）

1. **单向数据流（UDF）：**
   - **状态隔离**：ViewModel 只能向 UI 层暴露只读的 `StateFlow<UiState>`，绝对禁止暴露可变的 `MutableStateFlow`。
   - **事件驱动**：UI 层仅能通过发送 `Event/Intent` 触发 ViewModel 的业务逻辑。

2. **资源防泄漏约束：**
   - 任何涉及 `BroadcastReceiver`、`SensorEventListener`、`BluetoothGatt` 或 `C++/JNI Native` 指针的操作，**必须**在 `onCleared()`、`onPause()` 或 Lifecycle 销毁阶段显式释放/取消注册。

---

## 三、 代码生成范式模板

当请求生成高频数据处理、传感器解析或协议拆包代码时，必须遵循以下标准骨架：

```kotlin
package com.example.app.protocol

import java.nio.ByteBuffer

/**
 * 高频数据解析器基类范式
 */
class HighFrequencyDataParser(
    private val onPacketParsed: (ParsedData) -> Unit
) {
    // 1. 复用成员变量缓冲区，禁止在解析回调中分配内存
    private val buffer = ByteBuffer.allocate(1024)

    // 2. 线程安全与后台调度保证
    @Synchronized
    fun processRawBytes(data: ByteArray, length: Int) {
        // 边界防护：防止 IndexOutOfBoundsException
        if (length <= 0 || length > data.size) return
        
        // 3. 字节计算必须做 and 0xFF 转换，防止符号位拓展污染结果
        val header1 = data[0].toInt() and 0xFF
        val header2 = data[1].toInt() and 0xFF
        
        // 4. 解析业务逻辑（在后台线程完成）
        // ...
    }
}