package com.derry.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.myapplication.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivityNav : AppCompatActivity() {

    var bottomNavigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_nav)
        bottomNavigationView = findViewById(R.id.nav_view)

        // NavController controller=Navigation.findNavController(this,R.id.my_nav_host_fragment);
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment?
        val controller = navHostFragment!!.navController

        NavigationUI.setupWithNavController(bottomNavigationView!!, controller)

        // 为了源码分析的代码 1
        // 官网代码
        /*
        val finalHost = NavHostFragment.create(R.navigation.nav_graph_main)

        supportFragmentManager.beginTransaction()
                .replace(R.id.ll_fragment_navigation, finalHost)
                .setPrimaryNavigationFragment(finalHost)
                .commit()
         */
    }

    // 为了源码分析的代码 2
    /*override fun onSupportNavigateUp(): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment)
        return NavHostFragment.findNavController(fragment!!).navigateUp()
    }*/
}