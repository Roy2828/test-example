package com.example.myapplication.mvi

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TestMviActivity : AppCompatActivity() {

    private val viewModel: TestMviViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_mvi)

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvData = findViewById<TextView>(R.id.tvData)
        val tvCount = findViewById<TextView>(R.id.tvCount)
        val btnFetch = findViewById<Button>(R.id.btnFetch)
        val btnIncrement = findViewById<Button>(R.id.btnIncrement)
        val etName = findViewById<EditText>(R.id.etName)
        val tvNamePreview = findViewById<TextView>(R.id.tvNamePreview)

        // 1. 发送 Intent
        btnFetch.setOnClickListener {
            viewModel.sendIntent(TestMviContract.TestIntent.FetchData)
        }

        btnIncrement.setOnClickListener {
            viewModel.sendIntent(TestMviContract.TestIntent.IncrementCount)
        }

        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.sendIntent(TestMviContract.TestIntent.UpdateName(s?.toString() ?: ""))
            }
        })

        // 2. 观察 UiState - 拆分为多个局部观察，提高性能并符合单一职责

        // 观察内容状态 (加载、数据)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .map { it.content }  //过滤
                    .distinctUntilChanged()
                    .collect { content ->
                        progressBar.visibility = if (content.isLoading) View.VISIBLE else View.GONE
                        tvData.text = content.data
                        btnFetch.isEnabled = !content.isLoading
                    }
            }
        }

        // 观察计数器状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .map { it.counter }  //过滤
                    .distinctUntilChanged()
                    .collect { counter ->
                        tvCount.text = "计数：${counter.count}"
                    }
            }
        }

        // 观察表单状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .map { it.userForm }
                    .distinctUntilChanged()
                    .collect { form ->
                        tvNamePreview.text = "用户名预览：${form.userName}"
                    }
            }
        }

        // 3. 观察 UiEffect
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is TestMviContract.TestEffect.ShowToast -> {
                            Toast.makeText(this@TestMviActivity, effect.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}