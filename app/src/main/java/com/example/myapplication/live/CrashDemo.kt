// ============================================================
// StickyLiveDataCrashDemo.kt
// 复现：Cannot add the same observer with different lifecycles
// ============================================================
//
// 真实线上场景（对应堆栈）：
//   ViewPager + FragmentStatePagerAdapter，Fragment View 被销毁后重建
//   但 observer 在 onAttach/init 时已注册到 fragment.lifecycle
//   onViewCreated 里又用同一 observer 注册到 viewLifecycleOwner
//   → LiveData 发现同一 observer 绑定了不同 lifecycle → crash
// ============================================================

package com.example.myapplication.live

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.myapplication.R

// ---------- 模拟全局单例 VM ----------
object ModuleFunctionVM {
    val authorityChange = MutableLiveData<Boolean>()
}

// ---------- DemoFragment ----------
class DemoFragment : Fragment(R.layout.fragment_demo) {

    private var containerView: ViewGroup? = null

    // ⚠️ 关键：Observer 存为成员变量（复用同一实例）
    // 线上常见写法：把 observer 抽成字段复用，避免每次 new
    private val authorityObserver = Observer<Boolean> { changed ->
        if (changed) {
            // 模拟操作 View
            containerView?.findViewById<View>(android.R.id.text1)?.visibility = View.GONE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // ⚠️ 第一步：在 onAttach 中用 fragment 的 lifecycle 注册
        //    fragment.lifecycle 随 Fragment 实例存活，不会因为 View 销毁而被移除
        //    → observer 进入 LiveData 内部 mObservers map
        ModuleFunctionVM.authorityChange.observe(this, authorityObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        containerView = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        containerView = view as ViewGroup

        // ⚠️ 第二步：在 onViewCreated 中又用 viewLifecycleOwner 注册"同一个"observer
        //    此时 observer 已在 mObservers 中（fragment lifecycle 没销毁）
        //    LiveData.observe() 内部检测：
        //      existing.isAttachedTo(viewLifecycleOwner) → false（attach 在 fragment lifecycle）
        //    → throw IllegalArgumentException:
        //      "Cannot add the same observer with different lifecycles"
        ModuleFunctionVM.authorityChange.observe(viewLifecycleOwner, authorityObserver)
    }
}

// ---------- 宿主 Activity ----------
class CrashDemoActivity : AppCompatActivity() {

    private var fragmentAdded = false

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CrashDemoActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_demo)

        val fragmentContainerId = R.id.fragment_container

        findViewById<Button>(R.id.btn_add).setOnClickListener {
            if (!fragmentAdded) {
                supportFragmentManager.commitNow {
                    add(fragmentContainerId, DemoFragment())
                }
                fragmentAdded = true
            }
        }

        findViewById<Button>(R.id.btn_crash).setOnClickListener {
            // 模拟 ViewPager + FragmentStatePagerAdapter 场景：
            //   用户切到其他 tab → Fragment View 被 destroy
            //   用户切回来 → View 重建 → onViewCreated 再次 observe
            //
            // detach:
            //   onDestroyView() → containerView = null
            //   ⚠️ 但 fragment.lifecycle 仍在 CREATED 以上
            //      → authorityObserver 仍留在 LiveData.mObservers 中！
            // attach:
            //   onCreateView → onViewCreated
            //   → observe(viewLifecycleOwner-new, authorityObserver)
            //   → LiveData 发现同一 observer 已绑定 fragment.lifecycle
            //   → isAttachedTo(viewLifecycleOwner) == false
            //   → 💥 IllegalArgumentException
            val frag = supportFragmentManager.findFragmentById(fragmentContainerId)
            if (frag != null) {
                supportFragmentManager.commitNow { detach(frag) }
                supportFragmentManager.commitNow { attach(frag) }
            }
        }
    }
}

// ============================================================
// 复现步骤：
//   1. 点「添加 Fragment」
//      → onAttach: observe(fragment.lifecycle, authorityObserver)
//        → observer 进入 LiveData.mObservers map
//      → onViewCreated: observe(viewLifecycleOwner, authorityObserver)
//        → 同一 observer 已在 map 中，lifecycle 不同
//        → 💥 IllegalArgumentException: Cannot add the same observer
//          with different lifecycles
//
//   崩溃时机：添加 Fragment 时就会崩，正是在 BaseStatefulFragment.onViewCreated
//   里 observe(viewLifecycleOwner, sameObserver) 这一行，与你给的堆栈完全一致
//
// 根本原因：
//   authorityObserver 先注册到 fragment.lifecycle（onAttach 时），
//   后又在 onViewCreated 中以同一个实例注册到 viewLifecycleOwner
//
// 修复方案：
//   ❌ 不要复用 Observer 实例，每次 observe 用 lambda 创建新实例
//   或者：
//   ✅ 统一只用一种 lifecycle（要么 fragment.lifecycle，要么 viewLifecycleOwner）
// ============================================================
