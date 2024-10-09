package com.example.myapplication;

import com.example.myapplication.IMyAidlInterface;
/**
 * desc   :
 * e-mail : 1391324949@qq.com
 * date   : 2024/10/2 19:56
 * author : Roy
 * version: 1.0
 */
interface IMyService {
    String getMessage();
    oneway void unwatchOnlineState(in int conversationType, in String[] targets, in IMyAidlInterface callback);
}
