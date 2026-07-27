package com.example.myapplication.ble

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.clife_gait.ClifeGaitPlugin
import com.example.myapplication.R
import com.example.myapplication.ble.BleDeviceActivity

/**
 *    desc   :
 *    date   : 2025/8/15 09:22
 *    author : Roy
 *    version: 1.0
 */
class BleActivity :AppCompatActivity() {


    private var mHandler: Handler? = null

    private var myLooper: Looper? = null

    companion object{
        fun doIntent(context: Context){
            context.startActivity(Intent(context, BleActivity::class.java))
        }
    }



    private val mThread = Thread {
        try {
            Looper.prepare()
            mHandler = object : Handler(Looper.myLooper()!!) {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    when (msg.what) {
                        0 -> {
                            ClifeGaitPlugin.onMethodCall(this@BleActivity,"initialize")
                        }

                        1 -> {
                            ClifeGaitPlugin.onMethodCall(this@BleActivity,"startScan")
                        }

                        2 -> {
                            ClifeGaitPlugin.onMethodCall(this@BleActivity,"stopScan")
                        }
                    }
                }
            }

            Looper.loop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        myLooper = Looper.myLooper()
    }


    fun start() {
        if (!mThread.isAlive) {
            mThread.start()
        }
    }


    fun sendMessage(what: Int) {
        mHandler?.sendMessage(Message.obtain(mHandler, what))
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ble_activity)

        start()




        findViewById<TextView>(R.id.tv_start).setOnClickListener {
           // ClifeGaitPlugin.onMethodCall(this,"startScan")
            requestBluetoothPermissions()
        }

        findViewById<TextView>(R.id.tv_stop).setOnClickListener {

            sendMessage(2)
            myLooper?.quit()
        }

        findViewById<TextView>(R.id.tv_init).setOnClickListener {
            sendMessage(0)
        }

        findViewById<TextView>(R.id.tv_gatt_queue).setOnClickListener {
            BleDeviceActivity.start(this)
        }
    }



    // 在 Activity/Fragment 中定义权限请求启动器
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) {
            sendMessage(1)
        } else {
            Toast.makeText(this, "权限被拒绝，无法扫描设备", Toast.LENGTH_SHORT).show()
        }
    }

    // 动态请求权限
    fun requestBluetoothPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 需要新权限
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Android 6.0–11 需要位置权限
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        permissionLauncher.launch(permissions.toTypedArray())
    }
}