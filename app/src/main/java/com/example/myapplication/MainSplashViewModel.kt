package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *    desc   :
 *    e-mail : 1391324949@qq.com
 *    date   : 2025/1/6 9:22
 *    author : Roy
 *    version: 1.0
 */
class MainSplashViewModel:ViewModel() {

    val liveData:MutableLiveData<Unit> = MutableLiveData()

       fun start(){
          viewModelScope.launch {
               delay(6000)
              liveData.postValue(Unit)
          }
       }
}