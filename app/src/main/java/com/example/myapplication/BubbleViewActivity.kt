package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityBubbleViewBinding
import com.example.myapplication.pop.PopupArrow

import razerdp.basepopup.BasePopupWindow


class BubbleViewActivity : AppCompatActivity() {
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_bubble_view)
        var demoText = "你好的的的但是发多少发送地方水电发发是 "

      var  binding = ActivityBubbleViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHandler.setOnClickListener {
            binding.tvTypewriter.stop()
            binding.tvTypewriter.startWithHandler(demoText)
        }
        binding.btnAnimator.setOnClickListener {
            binding.tvTypewriter.stop()
            binding.tvTypewriter.startWithAnimator(demoText)
        }
        binding.btnCoroutine.setOnClickListener {
            binding.tvTypewriter.stop()
            binding.tvTypewriter.startWithCoroutine(demoText)
        }





    }

    fun showPop(view: View) {
        val gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
         val   mPopupArrow = PopupArrow(this)

        mPopupArrow.setBlurBackgroundEnable(false)
        mPopupArrow.setPopupGravity(BasePopupWindow.GravityMode.ALIGN_TO_ANCHOR_SIDE, gravity )
        mPopupArrow.showPopupWindow(findViewById(R.id.view1))
    }
}