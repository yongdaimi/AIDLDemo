// IOnNewPersonArrivedListener.aidl
package com.realsil.test.server;
import com.realsil.test.server.Person;

// Declare any non-default types here with import statements

// 当服务端有新人加入时，就通知每一个已经申请提醒功能的用户
interface IOnNewPersonArrivedListener {
    void onNewPersonArrived(in Person person);
}