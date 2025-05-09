package com.example.myapplication

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.derry.navigation.MainActivityNav
import com.example.myapplication.data.DataTest
import com.example.myapplication.examplerecyclerview.RecyclerViewActivity
import com.example.myapplication.spi.SpiTest
import com.example.myapplication.textView.TextWeightActivity
import com.example.myapplication.utils.HookUtil
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
       // Log.e("Roy","hhhhhhhhhhhhhh")

      /*  navigationBar {     //这段代码会导致 hook contentprovde启动失败

        }*/

        testServer()
       /*  var retrofit = Retrofit.Builder().baseUrl("http://www.baidu.com")
             .build()


        var myin =  retrofit.create(MyIn::class.java)
        myin.getName("hame").enqueue(object :Callback<String>{
            override fun onResponse(p0: Call<String>, p1: Response<String>) {

            }

            override fun onFailure(p0: Call<String>, p1: Throwable) {

            }

        })*/

/*
        val text = "分享当前群聊，邀请好友进群薅空投，双方都可获得高额奖励！点击查看玩转超级链接 >>"

        val spannableStringBuilder = SpannableStringBuilder(text)

// 检查文本中是否包含目标子串
        val clickableText = "点击查看玩转超级链接 >>"
        val startIndex = text.indexOf(clickableText)

        if (startIndex != -1) { // 确保找到了子串
            val endIndex = startIndex + clickableText.length
            spannableStringBuilder.setSpan(
                ForegroundColorSpan(resources.getColor(R.color.colorPrimary)),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: android.view.View) {
                    // 处理点击事件
                    Toast.makeText(this@MainActivity, "点击事件触发！", Toast.LENGTH_SHORT).show()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false // 去除下划线
                }
            }

            spannableStringBuilder.setSpan(
                clickableSpan,
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tv3.text = spannableStringBuilder
        tv3.movementMethod = LinkMovementMethod.getInstance()
        tv3.highlightColor = android.graphics.Color.TRANSPARENT*/



        startContinuousAnimation(iv)
        testRe()
    }


    fun testRe(){
       // 待做： 1.密码正则  请使用数字、符号、大小写字母中任意三种组合
       // 2. 验证码输入框背景 VerificationCodeView这个类需要layoutInflater  EditText(mContext);
       // 3.登录tab选项间距问题   MySimplePagerTitleView 这个类改padding

/*        正则
        ^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\W_])[0-9a-zA-Z\W_]{8,20}$
        (?=.*\d): 至少包含一个数字
        (?=.*[a-z]): 至少包含一个小写字母
        (?=.*[A-Z]): 至少包含一个大写字母
        (?=.*[\W_]): 至少包含一个特殊符号
        [0-9a-zA-Z\W_]: 可以包含数字、大小写字母和特殊符号
        {8,20}: 密码长度在8到20个字符之间*/

    et_8.addTextChangedListener(object :TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            s?.apply {
                if (s.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_])[0-9a-zA-Z\\W_]{8,20}\$".toRegex())) {

                } else {
                   Toast.makeText(this@MainActivity,"请输入数字、符号、大小写字母中任意三种组合",Toast.LENGTH_SHORT).show()
                }
            }

        }
    })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.e("Roy","hhhhhhhhhhhhhh")

  /*      **2、长按HOME键，选择运行其他的程序时。**

        **3、按下电源按键（关闭屏幕显示）时。**

        **4、从activity A中启动一个新的activity时。**

        **5、屏幕方向切换时，例如从竖屏切换到横屏时。***/
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        //只有在activity确实是被系统回收，重新创建activity的情况下才会被调用
      /*  屏幕方向切换时，activity生命周期如下：
        onPause -> onSaveInstanceState -> onStop -> onDestroy -> onCreate -> onStart -> onRestoreInstanceState -> onResume*/
    }



    fun testServer(){
        // 创建两个 Intent 对象
        val serviceIntent1 = Intent(this, MyService::class.java)
        val serviceIntent2 = Intent(this, MyService::class.java)

        // 尝试并发绑定服务
        bindService(serviceIntent1, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // 服务连接成功时的处理
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // 服务连接断开时的处理
            }
        }, Context.BIND_AUTO_CREATE)

        bindService(serviceIntent2, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // 服务连接成功时的处理
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                // 服务连接断开时的处理
            }
        }, Context.BIND_AUTO_CREATE)

        //unbindService(ServiceConnection)
    }


    fun okhttp(){
    /*  var build =  OkHttpClient.Builder().build()
        var json = ""
        var body = RequestBody.create( MediaType.get("application/json"),json)
      var request = Request.Builder().addHeader("name","dd")
          .post(body)
          .build()
     var req =  build.newCall(request)
     req.enqueue(object :okhttp3.Callback{
         override fun onFailure(call: okhttp3.Call, e: IOException) {

         }

         override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {

         }

     })*/
    }

    fun startActivityMethod(view: View) {
      //  startActivity(Intent(this,MainActivity2::class.java))

       startActivity(Intent(this, TextWeightActivity::class.java))
    }

    fun startActivityMethod2(view: View) {

        startActivity(Intent(this,MainActivity3::class.java))
    }

    fun startActivityMethod3(view: View) {
        RecyclerViewActivity.startActivity(this)

     //startActivity(Intent(this, MainActivity8::class.java))

       // startActivity(Intent(this, MainActivity5::class.java))

        //var intent = Intent("router://superlink/chat/detail?groupId=155115515")
        //startActivity(intent)

        val intent = Intent("router")
        intent.setData(Uri.parse("router://superlink/chat/detail?groupId=155115515"))
        startActivity(intent)
    }

    @SuppressLint("BlockedPrivateApi")
    fun test3(){
        try {
            var im = getSystemService(INPUT_METHOD_SERVICE)
            var mCurRootView = InputMethodManager::class.java.getDeclaredField("mCurRootView");
            mCurRootView.isAccessible = true
            mCurRootView.set(im,null)

            var mNextServedView = InputMethodManager::class.java.getDeclaredField("mNextServedView")
            mNextServedView.isAccessible=true
            mNextServedView.set(im,null)

            var mServedView = InputMethodManager::class.java.getDeclaredField("mServedView")
            mServedView.isAccessible = true
            mServedView.set(im,null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun startContinuousAnimation(view: View) {

        view.pivotX = 0.1f
        view.pivotY = 0.15f

        val scaleDownX: ObjectAnimator = ObjectAnimator.ofFloat(view, "scaleX", 1.5f, 1.0f)
        val scaleDownY: ObjectAnimator = ObjectAnimator.ofFloat(view, "scaleY", 1.5f, 1.0f)
        val moveLeft: ObjectAnimator =
            ObjectAnimator.ofFloat(view, "translationX", 0f, 0f)
        val moveUp: ObjectAnimator =
            ObjectAnimator.ofFloat(view, "translationY", 0f, -30f)

        scaleDownX.setDuration(500)
        scaleDownY.setDuration(500)
        moveLeft.setDuration(1000)
        moveUp.setDuration(1000)

        val scaleAndMove = AnimatorSet()
        scaleAndMove.playTogether(scaleDownX, scaleDownY, moveLeft, moveUp)
        scaleAndMove.setInterpolator(AccelerateDecelerateInterpolator())

        val moveDown: ObjectAnimator =
            ObjectAnimator.ofFloat(view, "translationY", -30f, 0f)
        moveDown.setDuration(1000)
        moveDown.setRepeatCount(ObjectAnimator.INFINITE)
        moveDown.setRepeatMode(ObjectAnimator.REVERSE)

        val bounce = AnimatorSet()
        bounce.play(moveDown)
        bounce.setInterpolator(AccelerateDecelerateInterpolator())

        val continuousAnimation = AnimatorSet()
        continuousAnimation.playSequentially(scaleAndMove, bounce)

        continuousAnimation.start()
    }


    fun test(){}
    fun startActivityMethod5(view: View) {
        HookUtil.initProvider(this);

    }



    fun startActivityMethodNav(view: View) {

        startActivity(Intent(this,MainActivityNav::class.java))
    }


    fun startActivityAidl(view: View) {
      /*  val intent =Intent(this, MainActivityAidl::class.java)
        intent.putExtra("name",DataTest("Roy","test",10))
        startActivity(intent)*/

        ARouter.getInstance().build("/user/adil").withParcelable("dataTest", DataTest("Roy","test",10)).navigation()
    }

    fun startActivitySpi(view: View) {
        tv8.text = SpiTest.test()

        showMissingPermissionDialog("内容");
    }


    private fun showMissingPermissionDialog(
        message: String,

    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("哈哈哈哈")
        builder.setMessage(message)
        // 拒绝, 退出应用
        builder.setNegativeButton("取消",
            DialogInterface.OnClickListener { dialog, which ->


            })

        builder.setPositiveButton("确定",
            DialogInterface.OnClickListener { dialog, which ->

                //checkDrawOverlayPermission();
            })

        builder.setCancelable(false)
        builder.show()
    }
}