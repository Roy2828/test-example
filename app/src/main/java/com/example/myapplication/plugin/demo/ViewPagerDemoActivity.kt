package com.example.myapplication.plugin.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.plugin.demo.vp.AutoHeightViewPager

class ViewPagerDemoActivity : AppCompatActivity() {
    private var mAutoHeightVp: AutoHeightViewPager? = null
    private var mAdapter: AutoHeightPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vp)

        val viewList: MutableList<View> = ArrayList()

        val view1 = LayoutInflater.from(this).inflate(R.layout.view_demo_1, null)
        setupRecyclerView(view1, R.id.recyclerView, generateHotData())
        viewList.add(view1)

        val view2 = LayoutInflater.from(this).inflate(R.layout.view_demo_2, null)
        setupRecyclerView(view2, R.id.recyclerView, generateOverseasData())
        viewList.add(view2)

        mAutoHeightVp = findViewById(R.id.viewpager)
        mAutoHeightVp?.adapter = AutoHeightPagerAdapter().also { mAdapter = it }
        mAdapter?.setViews(viewList)
        mAutoHeightVp?.currentItem = 0
    }

    private fun setupRecyclerView(rootView: View, recyclerId: Int, data: List<TravelItem>) {
        val recyclerView = rootView.findViewById<RecyclerView>(recyclerId)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TravelAdapter(data)
    }

    private fun generateHotData(): List<TravelItem> {
        return listOf(
            TravelItem("🏔 玉龙雪山", "海拔5596米，纳西族神山，终年积雪"),
            TravelItem("🏛 故宫博物院", "明清皇家宫殿，世界五大宫之首"),
            TravelItem("🌊 三亚蜈支洲岛", "中国版马尔代夫，碧海蓝天"),
            TravelItem("🎋 成都大熊猫基地", "近距离观赏国宝大熊猫"),
            TravelItem("🏯 西安兵马俑", "世界第八大奇迹"),
            TravelItem("🌄 张家界国家森林公园", "阿凡达取景地，三千奇峰"),
            TravelItem("🏖 厦门鼓浪屿", "海上花园，万国建筑博览"),
            TravelItem("⛩ 杭州西湖", "人间天堂，断桥残雪"),
        )
    }

    private fun generateOverseasData(): List<TravelItem> {
        return listOf(
            TravelItem("🗼 巴黎埃菲尔铁塔", "法国浪漫之都地标，夜景璀璨"),
            TravelItem("🏝 马尔代夫", "印度洋上的珍珠，蜜月天堂"),
            TravelItem("🏔 瑞士少女峰", "阿尔卑斯山皇后，欧洲之巅"),
            TravelItem("🗽 纽约自由女神像", "美国精神象征，自由之光"),
            TravelItem("🏯 日本富士山", "日本第一高峰，樱花与雪景"),
            TravelItem("🌋 夏威夷火山", "活火山与热带雨林共存"),
            TravelItem("🕌 迪拜哈利法塔", "世界最高建筑，828米云端"),
            TravelItem("🏰 新天鹅堡", "迪士尼城堡原型，童话世界"),
            TravelItem("🌊 大堡礁", "世界最大珊瑚礁，潜水圣地"),
            TravelItem("🏞 冰岛极光", "北极圈内的奇幻绿光"),
        )
    }
}
