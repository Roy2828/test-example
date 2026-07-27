package com.example.myapplication.ble

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * BLE GATT 连接管理器
 *
 * 基于 GATT 操作队列实现稳定的 BLE 连接管理。
 * 支持：扫描、连接、发现服务、读写、通知、MTU 请求等操作的串行化执行。
 */
class BleGattManager(
    private val context: Context,
    private val listener: BleGattListener,
    private val tag: String = "BleGattManager"
) {

    interface BleGattListener {
        fun onScanResult(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?)
        fun onScanFailed(errorCode: Int)
        fun onScanStopped()
        fun onConnected(device: BluetoothDevice)
        fun onDisconnected(device: BluetoothDevice, status: Int)
        fun onServicesDiscovered(device: BluetoothDevice, services: List<BluetoothGattService>)
        fun onCharacteristicRead(characteristic: BluetoothGattCharacteristic, value: ByteArray)
        fun onCharacteristicWrite(characteristic: BluetoothGattCharacteristic, status: Int)
        fun onCharacteristicChanged(characteristic: BluetoothGattCharacteristic, value: ByteArray)
        fun onDescriptorWrite(descriptor: BluetoothGattDescriptor, status: Int)
        fun onMtuChanged(mtu: Int, status: Int)
        fun onConnectionReady(device: BluetoothDevice)
    }

    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }
    private val bluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private var connectedDevice: BluetoothDevice? = null

    // GATT 操作队列
    private val operationQueue = GattOperationQueue(tag)

    // GATT 操作执行器
    private var gattOperator: GattOperator? = null

    // 主线程 Handler
    private val mainHandler = Handler(Looper.getMainLooper())

    // 自动重连标记
    private var autoReconnect = false
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 3

    // 连接状态
    @Volatile
    var isConnected: Boolean = false
        private set

    @Volatile
    var isConnecting: Boolean = false
        private set

    // 扫描回调
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            listener.onScanResult(
                device,
                result.rssi,
                result.scanRecord?.bytes
            )
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                listener.onScanResult(
                    result.device,
                    result.rssi,
                    result.scanRecord?.bytes
                )
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(tag, "扫描失败，错误码: $errorCode")
            listener.onScanFailed(errorCode)
        }
    }

    // GATT 回调
    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address
            Log.d(tag, "连接状态变化: device=$deviceAddress, status=$status, newState=$newState")

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    isConnected = true
                    isConnecting = false
                    connectedDevice = gatt.device
                    listener.onConnected(gatt.device)

                    // 连接成功后自动发现服务
                    gattOperator?.discoverServices()
                    operationQueue.onOperationComplete()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    isConnected = false
                    isConnecting = false

                    // 通知完成待处理操作
                    operationQueue.clear()
                    operationQueue.onOperationComplete()

                    listener.onDisconnected(gatt.device, status)

                    // 自动重连
                    if (autoReconnect && reconnectAttempts < maxReconnectAttempts) {
                        reconnectAttempts++
                        Log.d(tag, "尝试自动重连 ($reconnectAttempts/$maxReconnectAttempts)...")
                        mainHandler.postDelayed({
                            connect(gatt.device)
                        }, 2000L)
                    }

                    gatt.close()
                    bluetoothGatt = null
                    gattOperator = null
                    operationQueue.onOperationComplete()
                }

                BluetoothProfile.STATE_CONNECTING -> {
                    isConnecting = true
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d(tag, "服务发现完成, status=$status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val services = gatt.services
                listener.onServicesDiscovered(gatt.device, services)

                // 服务发现完成后，通知连接就绪
                listener.onConnectionReady(gatt.device)
            }
            operationQueue.onOperationComplete()
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            Log.d(tag, "特征读取完成: uuid=${characteristic.uuid}, status=$status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                listener.onCharacteristicRead(characteristic, value)
            }
            operationQueue.onOperationComplete()
        }

        @Deprecated("Deprecated in Java", ReplaceWith("onCharacteristicRead(gatt, characteristic, value, status)"))
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.d(tag, "特征读取完成(旧API): uuid=${characteristic.uuid}, status=$status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val value = characteristic.value ?: ByteArray(0)
                listener.onCharacteristicRead(characteristic, value)
            }
            operationQueue.onOperationComplete()
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.d(tag, "特征写入完成: uuid=${characteristic.uuid}, status=$status")
            listener.onCharacteristicWrite(characteristic, status)
            operationQueue.onOperationComplete()
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            Log.d(tag, "特征通知: uuid=${characteristic.uuid}, 长度=${value.size}")
            listener.onCharacteristicChanged(characteristic, value)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            Log.d(tag, "描述符写入完成: uuid=${descriptor.uuid}, status=$status")
            listener.onDescriptorWrite(descriptor, status)
            operationQueue.onOperationComplete()
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.d(tag, "MTU 变更: mtu=$mtu, status=$status")
            listener.onMtuChanged(mtu, status)
            operationQueue.onOperationComplete()
        }
    }

    /**
     * 检查蓝牙是否可用
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    /**
     * 开始扫描 BLE 设备
     */
    fun startScan(durationMs: Long = 10000L) {
        if (!isBluetoothEnabled()) {
            listener.onScanFailed(-1)
            return
        }

        try {
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            bluetoothLeScanner.startScan(null, settings, scanCallback)
            Log.d(tag, "开始扫描 BLE 设备")

            // 自动停止扫描
            if (durationMs > 0) {
                mainHandler.postDelayed({
                    stopScan()
                }, durationMs)
            }
        } catch (e: Exception) {
            Log.e(tag, "启动扫描失败: ${e.message}")
            listener.onScanFailed(-2)
        }
    }

    /**
     * 停止扫描
     */
    fun stopScan() {
        try {
            bluetoothLeScanner.stopScan(scanCallback)
            Log.d(tag, "停止扫描")
            listener.onScanStopped()
        } catch (e: Exception) {
            Log.e(tag, "停止扫描失败: ${e.message}")
        }
    }

    /**
     * 连接 BLE 设备
     */
    fun connect(device: BluetoothDevice, autoReconnect: Boolean = false) {
        if (isConnected || isConnecting) {
            Log.w(tag, "已在连接中或已连接，跳过")
            return
        }

        this.autoReconnect = autoReconnect
        this.reconnectAttempts = 0
        isConnecting = true

        try {
            // Android 8.0+ 建议使用 TRANSPORT_LE
            bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(
                    context,
                    false,
                    gattCallback,
                    BluetoothDevice.TRANSPORT_LE
                )
            } else {
                device.connectGatt(context, false, gattCallback)
            }

            gattOperator = GattOperator(
                gatt = bluetoothGatt!!,
                queue = operationQueue,
                callback = object : GattOperator.GattOperationCallback {
                    override fun onOperationSuccess(
                        type: GattOperationQueue.OperationType,
                        detail: String
                    ) {
                        Log.d(tag, "操作成功: $type $detail")
                    }

                    override fun onOperationFailed(
                        type: GattOperationQueue.OperationType,
                        error: String
                    ) {
                        Log.e(tag, "操作失败: $type, $error")
                    }
                },
                tag = tag
            )

            Log.d(tag, "正在连接设备: ${device.address}")
        } catch (e: Exception) {
            Log.e(tag, "连接失败: ${e.message}")
            isConnecting = false
            listener.onDisconnected(device, -1)
        }
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        gattOperator?.disconnect()
        bluetoothGatt?.let { gatt ->
            if (isConnected) {
                gatt.disconnect()
            }
            gatt.close()
        }
        bluetoothGatt = null
        gattOperator = null
        isConnected = false
        isConnecting = false
        autoReconnect = false
        connectedDevice = null
    }

    /**
     * 发现服务
     */
    fun discoverServices() {
        gattOperator?.discoverServices()
    }

    /**
     * 读取 characteristic
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        gattOperator?.readCharacteristic(characteristic)
    }

    /**
     * 写入 characteristic
     */
    fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    ) {
        gattOperator?.writeCharacteristic(characteristic, data, writeType)
    }

    /**
     * 开启/关闭通知
     */
    fun setNotification(characteristic: BluetoothGattCharacteristic, enable: Boolean) {
        gattOperator?.setNotification(characteristic, enable)
    }

    /**
     * 请求 MTU
     */
    fun requestMtu(mtu: Int) {
        gattOperator?.requestMtu(mtu)
    }

    /**
     * 获取已连接的设备
     */
    fun getConnectedDevice(): BluetoothDevice? = connectedDevice

    /**
     * 获取 GATT 操作队列
     */
    fun getOperationQueue(): GattOperationQueue = operationQueue

    /**
     * 释放资源
     */
    fun release() {
        disconnect()
        stopScan()
        mainHandler.removeCallbacksAndMessages(null)
    }
}
