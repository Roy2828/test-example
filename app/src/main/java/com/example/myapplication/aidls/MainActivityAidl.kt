package com.example.myapplication.aidls

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.RemoteException
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.example.clife_gait.ClifeGaitPlugin
import com.example.myapplication.IMyService
import com.example.myapplication.R
import com.example.myapplication.ble.BleActivity
import com.example.myapplication.data.DataTest

/**
 * desc   :
 * e-mail : 1391324949@qq.com
 * date   : 2024/10/2 19:59
 * author : Roy
 * version: 1.0
 */
@Route(path = "/user/adil")
class MainActivityAidl : AppCompatActivity() {
    private var myService: IMyService? = null
    private var isBound = false

    @JvmField
    @Autowired
    var dataTest: DataTest? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            myService = IMyService.Stub.asInterface(service)
            isBound = true
            try {
                val message = myService?.getMessage()
                val textView = findViewById<TextView>(R.id.text_view)
                textView.text = message
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            myService = null
            isBound = false
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        setContentView(R.layout.activity_main_aidl)
        val intent = Intent("com.example.IMyService")
        intent.setPackage("com.example.myapplication")
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)





        findViewById<TextView>(R.id.tv_start).setOnClickListener {
            // ClifeGaitPlugin.onMethodCall(this,"startScan")
            requestBluetoothPermissions()
        }

        findViewById<TextView>(R.id.tv_stop).setOnClickListener {


            try {
                myService?.stopScan()
            } catch (e: Exception) {
                println(e)
            }

        }

        findViewById<TextView>(R.id.tv_init).setOnClickListener {

            myService?.initialize()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }







    // 在 Activity/Fragment 中定义权限请求启动器
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) {
            myService?.startScan()
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
