package com.example.clife_gait

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService

import java.util.concurrent.atomic.AtomicBoolean


object  ClifeGaitPlugin  {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity

    private var mScanCallback: ScanCallback? = null
    private lateinit var mBluetoothManager: BluetoothManager
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mBluetoothLeScanner: BluetoothLeScanner
    private val isScanning = AtomicBoolean(false)

    val detector by lazy {
        ScanTimeoutDetector().apply {
            onTimeout = {
                // 扫描连接已断开
                if (::mBluetoothAdapter.isInitialized &&
                                !mBluetoothAdapter.isDiscovering &&
                                isScanning.get()
                ) { // isScanning.get()
//                    stopScan()
//                    startScan()
                } else {
                    if (!isScanning.get()) {
                        destroy()
                    }
                }
            }
        }
    }


      fun onMethodCall( context: Context,method:String) {
        if (method == "initialize") {

            mBluetoothManager = getSystemService(context, BluetoothManager::class.java)!!
            mBluetoothAdapter = mBluetoothManager!!.adapter
            mBluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
            mScanCallback =
                    object : ScanCallback() {
                        override fun onScanResult(callbackType: Int, result: ScanResult) {
                            super.onScanResult(callbackType, result)
                            detector.onDataReceived()
                            // 处理扫描结果
                            val device: BluetoothDevice? = result.getDevice()
                            var deviceName = device?.name ?: "Unknown Device"
                            println("TTTT onScanResult device:$deviceName")
                            val scanRecord: ScanRecord? = result.scanRecord
                            if (device != null &&
                                            device.name != null &&
                                            device.name!!.startsWith("Gait-")
                            ) {
                                // 获取制造商特定数据（Type 0xFF）
                                val manufacturerData = scanRecord?.bytes
                                if (manufacturerData != null) {
                                    // 解析步态数据
                                    var gaitData = manufacturerData.toHexString()
                                    val tag = "020106030312FF"
                                    if (gaitData.startsWith(tag)) { // 步态鞋type、uuid识别
                                        gaitData = gaitData.substring(tag.length)
                                        if (gaitData.length >= 4) { // 步态数据长度计算
                                            var hexL = gaitData.substring(0, 2).toInt(16)
                                            val data =
                                                    gaitData.substring(
                                                            4,
                                                            4 + hexL * 2
                                                    ) /// -2为Type类型FF的占位
                                            // Log.d(
                                            //         "收到步态数据: 名称 = ${device.name}, ${$data}"
                                            // )

                                                val arr = hexStringToBytes("0x$data")
                                                val map = HashMap<String, Any>()
                                                map.put("name", device.name)
                                                map.put("data", arr)

                                        }
                                    }
                                }
                            }
                        }

                        override fun onScanFailed(errorCode: Int) {
                            super.onScanFailed(errorCode)
                            println("TTTT onScanFailed")
                            Log.e("Bluetooth", "扫描失败，错误码: $errorCode")
                            detector.startMonitoring()
                        }
                    }
        } else if (method == "stopScan") {
            if (mScanCallback != null) {
                stopScan()
            }
        } else if (method == "startScan") {
//            val context = mFlutterPluginBinding.applicationContext
            /*   if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) !=
                            PackageManager.PERMISSION_GRANTED
            ) {
                result.success(false)
                return
            }*/
            // if (mScanCallback == null) {
            //     mScanCallback =
            //             object : ScanCallback() {
            //                 override fun onScanResult(callbackType: Int, result: ScanResult) {
            //                     super.onScanResult(callbackType, result)
            //                     detector.onDataReceived()
            //                     // 处理扫描结果
            //                     val device: BluetoothDevice? = result.getDevice()
            //                     val scanRecord: ScanRecord? = result.scanRecord
            //                     if (device!=null && device.name != null &&
            // device.name!!.startsWith("Gait-")) {
            //                         // 获取制造商特定数据（Type 0xFF）
            //                         val manufacturerData = scanRecord?.bytes
            //                         if (manufacturerData != null) {
            //                             // 解析步态数据
            //                             var gaitData = manufacturerData.toHexString()
            //                             val tag = "020106030312FF"
            //                             if (gaitData.startsWith(tag)) { // 步态鞋type、uuid识别
            //                                 gaitData = gaitData.substring(tag.length)
            //                                 if (gaitData.length >= 4) { // 步态数据长度计算
            //                                     var hexL = gaitData.substring(0, 2).toInt(16)
            //                                     val data =
            //                                             gaitData.substring(
            //                                                     4,
            //                                                     4 + hexL * 2
            //                                             ) /// -2为Type类型FF的占位
            //                                     // Log.d(
            //                                     //         "收到步态数据: 名称 = ${device.name},
            // ${$data}"
            //                                     // )
            //                                     if (dataEventSink != null) {
            //                                         val arr = hexStringToBytes("0x$data")
            //                                         val map = HashMap<String, Any>()
            //                                         map.put("name", device.name)
            //                                         map.put("data", arr)
            //                                         dataEventSink!!.success(map)
            //                                     }
            //                                 }
            //                             }
            //                         }
            //                     }
            //                 }

            //                 override fun onScanFailed(errorCode: Int) {
            //                     super.onScanFailed(errorCode)
            //                     Log.e("Bluetooth", "扫描失败，错误码: $errorCode")
            //                     detector.startMonitoring()
            //                 }
            //             }
            // }
            detector.startMonitoring()
            // 启动扫描
            startScan()

        }
    }

    private fun stopScan() {
        if (mScanCallback != null) {
            try {
                mBluetoothLeScanner.stopScan(mScanCallback)
//                setupScanner(false)
            } catch (e: Exception) {
                Log.e("Bluetooth", "stopScan失败，错误: $e")
            }
        }
        isScanning.set(false)
    }

    private fun startScan() {
        if (::mBluetoothLeScanner.isInitialized) {
            stopScan()
            try {
//                setupScanner(true)
                // 启动扫描
                val scanSettings =
                    ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
                mBluetoothLeScanner.startScan(null, scanSettings, mScanCallback)

            } catch (e: Exception) {
                Log.e("Bluetooth", "startScan失败，错误: $e")
            }
            isScanning.set(true)
        }
    }

    private fun setupScanner(flag : Boolean) {
        mScanCallback = null
        if (flag) {
            mScanCallback =
                object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        super.onScanResult(callbackType, result)
                        detector.onDataReceived()
                        // 处理扫描结果
                        val device: BluetoothDevice? = result.getDevice()
                        println("TTTT device:${device?.name}")
                        val scanRecord: ScanRecord? = result.scanRecord
                        if (device != null &&
                            device.name != null &&
                            device.name!!.startsWith("Gait-")
                        ) {
                            Log.e("Bluetooth", "device: ${device.name}")
                            // 获取制造商特定数据（Type 0xFF）
                            val manufacturerData = scanRecord?.bytes
                            if (manufacturerData != null) {
                                // 解析步态数据
                                var gaitData = manufacturerData.toHexString()
                                val tag = "020106030312FF"
                                if (gaitData.startsWith(tag)) { // 步态鞋type、uuid识别
                                    gaitData = gaitData.substring(tag.length)
                                    if (gaitData.length >= 4) { // 步态数据长度计算
                                        var hexL = gaitData.substring(0, 2).toInt(16)
                                        val data =
                                            gaitData.substring(
                                                4,
                                                4 + hexL * 2
                                            ) /// -2为Type类型FF的占位
                                        // Log.d(
                                        //         "收到步态数据: 名称 = ${device.name}, ${$data}"
                                        // )
                                        val arr = hexStringToBytes("0x$data")
                                        val map = HashMap<String, Any>()
                                        map.put("name", device.name)
                                        map.put("data", arr)
                                    }
                                }
                            }
                        }
                    }

                    override fun onScanFailed(errorCode: Int) {
                        super.onScanFailed(errorCode)
                        Log.e("Bluetooth", "扫描失败，错误码: $errorCode")
                        detector.startMonitoring()
                    }
                }
        }
    }

    // 字节数组转十六进制字符串
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }

    fun hexStringToBytes(hexString: String): ByteArray {
        // 去除字符串中的空格和前缀（如 "0x"）
        val cleanedHexString = hexString.replace(" ", "").replace("0x", "")

        // 确保字符串长度为偶数
        require(cleanedHexString.length % 2 == 0) { "十六进制字符串长度必须为偶数" }

        // 创建字节数组
        val byteArray = ByteArray(cleanedHexString.length / 2)

        // 逐字节解析
        for (i in cleanedHexString.indices step 2) {
            val byteStr = cleanedHexString.substring(i, i + 2)
            val byte = byteStr.toInt(16).toByte()
            byteArray[i / 2] = byte
        }

        return byteArray
    }





}
