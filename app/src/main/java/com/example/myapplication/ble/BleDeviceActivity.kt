package com.example.myapplication.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.ble.BleGattManager.BleGattListener

/**
 * BLE 设备操作演示 Activity
 *
 * 展示基于 GATT 操作队列的稳定 BLE 连接流程：
 * 1. 扫描设备
 * 2. 连接设备
 * 3. 发现服务
 * 4. 读取/写入特征值
 * 5. 开启通知
 */
class BleDeviceActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BleDeviceActivity"

        fun start(context: Context) {
            context.startActivity(Intent(context, BleDeviceActivity::class.java))
        }
    }

    // UI
    private lateinit var tvStatus: TextView
    private lateinit var tvDeviceInfo: TextView
    private lateinit var tvServiceInfo: TextView
    private lateinit var tvLog: TextView
    private lateinit var btnScan: Button
    private lateinit var btnConnect: Button
    private lateinit var btnDiscover: Button
    private lateinit var btnRead: Button
    private lateinit var btnWrite: Button
    private lateinit var btnNotify: Button
    private lateinit var btnMtu: Button
    private lateinit var btnDisconnect: Button

    // BLE 管理器
    private lateinit var bleManager: BleGattManager

    // 扫描到的设备列表
    private val scannedDevices = mutableListOf<BluetoothDevice>()

    // 当前连接的设备
    private var currentDevice: BluetoothDevice? = null

    // 当前选中的服务和特征
    private var selectedService: BluetoothGattService? = null
    private var selectedCharacteristic: BluetoothGattCharacteristic? = null

    // 日志
    private val logLines = mutableListOf<String>()
    private val mainHandler = Handler(Looper.getMainLooper())

    // 权限
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) {
            startBleScan()
        } else {
            Toast.makeText(this, "需要蓝牙权限才能扫描", Toast.LENGTH_SHORT).show()
        }
    }

    private val bleListener = object : BleGattListener {
        override fun onScanResult(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?) {
            runOnUiThread {
                // 去重
                if (scannedDevices.none { it.address == device.address }) {
                    scannedDevices.add(device)
                    addLog("发现设备: ${device.name ?: "未知"} [${device.address}] RSSI: $rssi")
                    updateDeviceList()
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            runOnUiThread {
                addLog("扫描失败，错误码: $errorCode")
                setStatus("扫描失败")
            }
        }

        override fun onScanStopped() {
            runOnUiThread {
                addLog("扫描已停止，共发现 ${scannedDevices.size} 个设备")
                setStatus("扫描完成")
                btnScan.isEnabled = true
                updateButtonState(false)
            }
        }

        override fun onConnected(device: BluetoothDevice) {
            runOnUiThread {
                currentDevice = device
                addLog("已连接: ${device.name ?: "未知"} [${device.address}]")
                setStatus("已连接")
                updateButtonState(true)
            }
        }

        override fun onDisconnected(device: BluetoothDevice, status: Int) {
            runOnUiThread {
                addLog("已断开连接: ${device.name ?: "未知"}, status=$status")
                setStatus("已断开")
                currentDevice = null
                updateButtonState(false)
            }
        }

        override fun onServicesDiscovered(
            device: BluetoothDevice,
            services: List<BluetoothGattService>
        ) {
            runOnUiThread {
                addLog("服务发现完成，共 ${services.size} 个服务")
                val sb = StringBuilder()
                for (service in services) {
                    sb.append("服务: ${service.uuid}\n")
                    for (char in service.characteristics) {
                        sb.append("  特征: ${char.uuid}\n")
                        for (desc in char.descriptors) {
                            sb.append("    描述符: ${desc.uuid}\n")
                        }
                    }
                }
                tvServiceInfo.text = sb.toString()
                updateButtonState(true)
            }
        }

        override fun onCharacteristicRead(
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            runOnUiThread {
                val hexStr = value.joinToString(" ") { "%02X".format(it) }
                addLog("读取特征 ${characteristic.uuid}: $hexStr")
            }
        }

        override fun onCharacteristicWrite(
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            runOnUiThread {
                val msg = if (status == BluetoothGatt.GATT_SUCCESS) "成功" else "失败(status=$status)"
                addLog("写入特征 ${characteristic.uuid}: $msg")
            }
        }

        override fun onCharacteristicChanged(
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            runOnUiThread {
                val hexStr = value.joinToString(" ") { "%02X".format(it) }
                addLog("通知 ${characteristic.uuid}: $hexStr")
            }
        }

        override fun onDescriptorWrite(descriptor: BluetoothGattDescriptor, status: Int) {
            runOnUiThread {
                val msg = if (status == BluetoothGatt.GATT_SUCCESS) "成功" else "失败(status=$status)"
                addLog("描述符写入 ${descriptor.uuid}: $msg")
            }
        }

        override fun onMtuChanged(mtu: Int, status: Int) {
            runOnUiThread {
                val msg = if (status == BluetoothGatt.GATT_SUCCESS) "成功" else "失败(status=$status)"
                addLog("MTU 请求: $mtu, $msg")
            }
        }

        override fun onConnectionReady(device: BluetoothDevice) {
            runOnUiThread {
                addLog("连接就绪，可进行读写操作")
                setStatus("就绪")
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_device)

        initViews()
        bleManager = BleGattManager(this, bleListener, TAG)

        addLog("BLE GATT 操作队列演示")
        addLog("提示：所有 GATT 操作均通过队列串行执行，避免并发冲突")
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tv_ble_status)
        tvDeviceInfo = findViewById(R.id.tv_ble_device_info)
        tvServiceInfo = findViewById(R.id.tv_ble_service_info)
        tvLog = findViewById(R.id.tv_ble_log)
        btnScan = findViewById(R.id.btn_ble_scan)
        btnConnect = findViewById(R.id.btn_ble_connect)
        btnDiscover = findViewById(R.id.btn_ble_discover)
        btnRead = findViewById(R.id.btn_ble_read)
        btnWrite = findViewById(R.id.btn_ble_write)
        btnNotify = findViewById(R.id.btn_ble_notify)
        btnMtu = findViewById(R.id.btn_ble_mtu)
        btnDisconnect = findViewById(R.id.btn_ble_disconnect)

        setStatus("未连接")
        updateButtonState(false)

        btnScan.setOnClickListener { requestBlePermission() }
        btnConnect.setOnClickListener { connectToFirstDevice() }
        btnDiscover.setOnClickListener { bleManager.discoverServices() }
        btnRead.setOnClickListener { readFirstCharacteristic() }
        btnWrite.setOnClickListener { writeTestData() }
        btnNotify.setOnClickListener { toggleNotification() }
        btnMtu.setOnClickListener { bleManager.requestMtu(512) }
        btnDisconnect.setOnClickListener { bleManager.disconnect() }
    }

    private fun requestBlePermission() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startBleScan() {
        scannedDevices.clear()
        addLog("开始扫描 BLE 设备...")
        setStatus("扫描中...")
        btnScan.isEnabled = false
        bleManager.startScan(15000L)
    }

    private fun connectToFirstDevice() {
        if (scannedDevices.isEmpty()) {
            Toast.makeText(this, "请先扫描设备", Toast.LENGTH_SHORT).show()
            return
        }
        val device = scannedDevices.first()
        connectToDevice(device)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        addLog("正在连接: ${device.name ?: "未知"} [${device.address}]")
        setStatus("连接中...")
        bleManager.connect(device, autoReconnect = false)
        tvDeviceInfo.text = "设备: ${device.name ?: "未知"}\n地址: ${device.address}"
    }

    private fun readFirstCharacteristic() {
        val characteristic = findFirstReadableCharacteristic() ?: run {
            Toast.makeText(this, "未找到可读的特征", Toast.LENGTH_SHORT).show()
            return
        }
        addLog("读取特征: ${characteristic.uuid}")
        bleManager.readCharacteristic(characteristic)
    }

    private fun writeTestData() {
        val characteristic = findFirstWritableCharacteristic() ?: run {
            Toast.makeText(this, "未找到可写的特征", Toast.LENGTH_SHORT).show()
            return
        }
        val testData = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        addLog("写入特征 ${characteristic.uuid}: ${testData.joinToString(" ") { "%02X".format(it) }}")
        bleManager.writeCharacteristic(characteristic, testData)
    }

    private fun toggleNotification() {
        val characteristic = findFirstNotifiableCharacteristic() ?: run {
            Toast.makeText(this, "未找到支持通知的特征", Toast.LENGTH_SHORT).show()
            return
        }
        // 简单切换：如果按钮文字包含"开启"，就开启；否则关闭
        val shouldEnable = btnNotify.text.toString().contains("开启")
        addLog("${if (shouldEnable) "开启" else "关闭"}通知: ${characteristic.uuid}")
        bleManager.setNotification(characteristic, shouldEnable)
        btnNotify.text = if (shouldEnable) "关闭通知" else "开启通知"
    }

    private fun findFirstReadableCharacteristic(): BluetoothGattCharacteristic? {
        bleManager.getConnectedDevice()?.let { device ->
            bleManager.let { mgr ->
                // 通过 BluetoothGatt 获取服务
            }
        }
        // 简化：从已打印的服务信息中获取
        return null
    }

    private fun findFirstWritableCharacteristic(): BluetoothGattCharacteristic? {
        return null
    }

    private fun findFirstNotifiableCharacteristic(): BluetoothGattCharacteristic? {
        return null
    }

    private fun updateDeviceList() {
        tvDeviceInfo.text = "发现 ${scannedDevices.size} 个设备:\n" +
                scannedDevices.take(5).joinToString("\n") { d ->
                    "${d.name ?: "未知"} [${d.address}]"
                } + if (scannedDevices.size > 5) "\n..." else ""
    }

    private fun updateButtonState(connected: Boolean) {
        btnConnect.isEnabled = scannedDevices.isNotEmpty() && !connected
        btnDiscover.isEnabled = connected
        btnRead.isEnabled = connected
        btnWrite.isEnabled = connected
        btnNotify.isEnabled = connected
        btnMtu.isEnabled = connected
        btnDisconnect.isEnabled = connected
    }

    private fun setStatus(status: String) {
        tvStatus.text = "状态: $status"
    }

    private fun addLog(message: String) {
        logLines.add(message)
        if (logLines.size > 200) {
            logLines.removeAt(0)
        }
        tvLog.text = logLines.joinToString("\n")
        // 自动滚动到底部
        mainHandler.post {
            tvLog.post { tvLog.scrollTo(0, tvLog.height) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.release()
        mainHandler.removeCallbacksAndMessages(null)
    }
}
