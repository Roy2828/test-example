package com.example.myapplication;

import android.os.AsyncTask;

/**
 * desc   :
 * e-mail : 1391324949@qq.com
 * date   : 2024/8/14 10:42
 * author : Roy
 * version: 1.0
 */
public class aad {
    private static Object globalReference;

    public static void main(String[] args) {
        Object obj = new Object(); // 创建一个对象


        globalReference = obj; // 全局变量持有对该对象的引用

        obj = null; // 将局部引用置为null

// 模拟30秒后执行垃圾回收

        // globalReference //不会等于null  因为只是obj这个局部变量的设置的引用为null  但是 都是指向 堆  又没有把堆干掉
        try {
            Thread.sleep(30000);
            System.gc(); // 请求系统进行垃圾回收
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        AsyncTask  task = new  AsyncTask<String,String,String>(){

           @Override
           protected String doInBackground(String... strings) {
               return null;
           }
       };
        task.execute();

// 在这里，全局引用仍然存在，对象无法被释放
    }
}