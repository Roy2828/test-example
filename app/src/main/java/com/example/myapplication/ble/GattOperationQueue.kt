package com.example.myapplication.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothStatusCodes
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * GATT 操作队列 —— BLE 稳定连接的关键
 *
 * Android BLE API 要求所有 GATT 操作（连接、发现服务、读写等）必须串行执行。
 * 并发执行多个 GATT 操作会导致操作失败、连接断开等问题。
 * 本队列将所有操作排队，确保前一个操作完成后再执行下一个。
 */
class GattOperationQueue(private val tag: String = "GattOpQueue") {

    private val operations: ConcurrentLinkedQueue<GattOperation> = ConcurrentLinkedQueue()
    private val isProcessing = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * GATT 操作类型
     */
    enum class OperationType {
        CONNECT,
        DISCONNECT,
        DISCOVER_SERVICES,
        READ_CHARACTERISTIC,
        WRITE_CHARACTERISTIC,
        READ_DESCRIPTOR,
        WRITE_DESCRIPTOR,
        SET_NOTIFICATION,
        REQUEST_MTU,
        DELAY
    }

    /**
     * GATT 操作封装
     */
    data class GattOperation(
        val type: OperationType,
        val characteristic: BluetoothGattCharacteristic? = null,
        val descriptor: BluetoothGattDescriptor? = null,
        val data: ByteArray? = null,
        val mtu: Int = 0,
        val delayMs: Long = 0,
        val enableNotification: Boolean = false,
        val action: (() -> Unit)? = null
    )

    /**
     * 添加操作到队列尾部
     */
    fun enqueue(operation: GattOperation) {
        operations.offer(operation)
        Log.d(tag, "入队: ${operation.type}, 队列大小: ${operations.size}")
        processNext()
    }

    /**
     * 插入操作到队列头部（高优先级）
     */
    fun enqueueAtFront(operation: GattOperation) {
        val tempList = mutableListOf(operation)
        tempList.addAll(operations)
        operations.clear()
        operations.addAll(tempList)
        Log.d(tag, "头部入队: ${operation.type}, 队列大小: ${operations.size}")
        processNext()
    }

    /**
     * 清除所有待处理操作
     */
    fun clear() {
        operations.clear()
        Log.d(tag, "队列已清空")
    }

    /**
     * 标记即将断开连接，清除队列并阻止后续操作
     */
    fun prepareDisconnect() {
        clear()
    }

    /**
     * 处理队列中的下一个操作
     */
    @Synchronized
    fun processNext() {
        if (isProcessing.get() || operations.isEmpty()) {
            return
        }

        val operation = operations.poll() ?: return
        isProcessing.set(true)

        Log.d(tag, "执行: ${operation.type}, 剩余队列: ${operations.size}")

        // 延迟操作：直接 postDelay 然后完成
        if (operation.type == OperationType.DELAY) {
            mainHandler.postDelayed({
                Log.d(tag, "延迟完成: ${operation.delayMs}ms")
                onOperationComplete()
            }, operation.delayMs)
            return
        }

        // 执行操作 action
        operation.action?.invoke()
    }

    /**
     * 标记当前操作完成，处理下一个
     */
    fun onOperationComplete() {
        isProcessing.set(false)
        processNext()
    }

    /**
     * 获取队列大小
     */
    fun size(): Int = operations.size

    /**
     * 是否正在处理操作
     */
    fun isBusy(): Boolean = isProcessing.get()
}

/**
 * GATT 操作辅助类，封装串行化 GATT 调用
 * 所有 GATT API 调用通过队列串行执行，避免并发冲突。
 */
class GattOperator(
    private val gatt: BluetoothGatt,
    private val queue: GattOperationQueue,
    private val callback: GattOperationCallback,
    private val tag: String = "GattOperator"
) {

    interface GattOperationCallback {
        fun onOperationSuccess(type: GattOperationQueue.OperationType, detail: String = "")
        fun onOperationFailed(type: GattOperationQueue.OperationType, error: String)
    }

    /**
     * 发现服务
     */
    fun discoverServices() {
        queue.enqueue(
            GattOperationQueue.GattOperation(
                type = GattOperationQueue.OperationType.DISCOVER_SERVICES,
                action = {
                    val result = gatt.discoverServices()
                    if (!result) {
                        callback.onOperationFailed(
                            GattOperationQueue.OperationType.DISCOVER_SERVICES,
                            "discoverServices() 返回 false"
                        )
                        queue.onOperationComplete()
                    }
                }
            )
        )
    }

    /**
     * 读取 characteristic
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        queue.enqueue(
            GattOperationQueue.GattOperation(
                type = GattOperationQueue.OperationType.READ_CHARACTERISTIC,
                characteristic = characteristic,
                action = {
                    val result = gatt.readCharacteristic(characteristic)
                    if (!result) {
                        callback.onOperationFailed(
                            GattOperationQueue.OperationType.READ_CHARACTERISTIC,
                            "readCharacteristic() 返回 false"
                        )
                        queue.onOperationComplete()
                    }
                }
            )
        )
    }

    /**
     * 写入 characteristic
     */
    fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    ) {
        queue.enqueue(
            GattOperationQueue.GattOperation(
                type = GattOperationQueue.OperationType.WRITE_CHARACTERISTIC,
                characteristic = characteristic,
                data = data,
                action = {
                    val result = gatt.writeCharacteristic(characteristic, data, writeType)
                    if (result != BluetoothStatusCodes.SUCCESS) {
                        callback.onOperationFailed(
                            GattOperationQueue.OperationType.WRITE_CHARACTERISTIC,
                            "writeCharacteristic() 返回 $result"
                        )
                        queue.onOperationComplete()
                    }
                }
            )
        )
    }

    /**
     * 设置/取消通知
     */
    fun setNotification(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ) {
        queue.enqueue(
            GattOperationQueue.GattOperation(
                type = GattOperationQueue.OperationType.SET_NOTIFICATION,
                characteristic = characteristic,
                enableNotification = enable,
                action = {
                    val result = gatt.setCharacteristicNotification(characteristic, enable)
                    if (!result) {
                        callback.onOperationFailed(
                            GattOperationQueue.OperationType.SET_NOTIFICATION,
                            "setCharacteristicNotification() 返回 false"
                        )
                        queue.onOperationComplete()
                        return@GattOperation
                    }

                    // 同时配置 CCCD descriptor
                    val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                    val descriptor = characteristic.getDescriptor(cccdUuid)
                    if (descriptor != null) {
                        gatt.writeDescriptor(descriptor)
                    } else {
                        queue.onOperationComplete()
                    }
                }
            )
        )
    }

    /**
     * 请求 MTU
     */
    fun requestMtu(mtu: Int) {
        queue.enqueue(
            GattOperationQueue.GattOperation(
                type = GattOperationQueue.OperationType.REQUEST_MTU,
                mtu = mtu,
                action = {
                    val result = gatt.requestMtu(mtu)
                    if (!result) {
                        callback.onOperationFailed(
                            GattOperationQueue.OperationType.REQUEST_MTU,
                            "requestMtu() 返回 false"
                        )
                        queue.onOperationComplete()
                    }
                }
            )
        )
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        queue.prepareDisconnect()
        gatt.disconnect()
    }

    /**
     * 延迟指定时间（用于某些设备需要的操作间隔）
     */
    fun delay(ms: Long) {
        queue.enqueue(
            GattOperationQueue.GattOperation(
                type = GattOperationQueue.OperationType.DELAY,
                delayMs = ms
            )
        )
    }
}
