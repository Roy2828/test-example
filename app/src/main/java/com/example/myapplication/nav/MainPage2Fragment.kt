package com.derry.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.myapplication.R

class MainPage2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_main_page2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btn = view.findViewById<Button>(R.id.btn)
        btn.setOnClickListener { view ->
            Navigation.findNavController(view).navigate(R.id.action_page1)
            // Navigation.findNavController(view).navigateUp(); 返回上一个Fragment
        }
        val btn2 = view.findViewById<Button>(R.id.btn2)
        btn2.setOnClickListener { view ->
            Navigation.findNavController(view).navigate(R.id.action_page3)
        }
    }
}