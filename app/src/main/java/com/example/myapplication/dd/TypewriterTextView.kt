package com.example.myapplication.dd

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.animation.addListener
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.google.common.collect.ComparisonChain.start
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *    desc   :
 *    date   : 2025/6/11 17:28
 *    author : Roy
 *    version: 1.0
 */
class TypewriterTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    // 配置参数
    private var charInterval = 150L
    private var delayStart = 0L
    private var repeat = false

    private var fullText: CharSequence = ""
    private var running = false

    // Handler 方案
    private val handler = Handler(Looper.getMainLooper())
    private val charRunnable = object : Runnable {
        override fun run() {
            if (currentIdx <= fullText.length) {
                text = fullText.subSequence(0, currentIdx)
                currentIdx++
                if (currentIdx <= fullText.length) {
                    handler.postDelayed(this, charInterval)
                } else if (repeat) {
                    handler.postDelayed({ start() }, charInterval)
                } else {
                    running = false
                }
            }
        }
    }
    private var currentIdx = 0

    // Animator 方案
    private var animator: ValueAnimator? = null

    // Coroutine 方案
    private var job: Job? = null

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.TypewriterTextView, 0, 0)
            charInterval = a.getInt(R.styleable.TypewriterTextView_tw_charInterval, charInterval.toInt()).toLong()
            delayStart = a.getInt(R.styleable.TypewriterTextView_tw_delayStart, 0).toLong()
            repeat = a.getBoolean(R.styleable.TypewriterTextView_tw_repeat, false)
            a.recycle()
        }
    }

    /** 方案一：Handler 实现 */
    fun startWithHandler(text: CharSequence) {
        if (running) return
        fullText = text
        currentIdx = 1
        running = true
        this.text = ""
        handler.postDelayed(charRunnable, delayStart)
    }

    /** 方案二：ValueAnimator 实现 */
    fun startWithAnimator(text: CharSequence) {
        if (running) return
        fullText = text
        animator?.cancel()
        animator = ValueAnimator.ofInt(1, text.length).apply {
            duration = charInterval * text.length
            addUpdateListener {
                val idx = it.animatedValue as Int
                this@TypewriterTextView.text = fullText.subSequence(0, idx)
            }
            startDelay = delayStart
            addListener(onStart = { running = true },
                onEnd   = {
                    running = false
                    if (repeat) startWithAnimator(fullText)
                })
            start()
        }
    }

    /** 方案三：Coroutine 实现（需在 LifecycleOwner 范围内调用） */
    fun startWithCoroutine(text: CharSequence) {
        if (running) return
        fullText = text
        val owner = findViewTreeLifecycleOwner() ?: return
        job?.cancel()
        job = owner.lifecycleScope.launch {
            running = true
            delay(delayStart)
            for (i in 1..fullText.length) {
                this@TypewriterTextView.text = fullText.subSequence(0, i)
                delay(charInterval)
            }
            running = false
            if (repeat) startWithCoroutine(fullText)
        }
    }

    fun stop() {
        handler.removeCallbacks(charRunnable)
        animator?.cancel()
        job?.cancel()
        running = false
    }

    fun isRunning(): Boolean = running
}