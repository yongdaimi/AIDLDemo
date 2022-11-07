package com.realsil.test.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 演示ADIL 跨进程服务调用
 * 参考链接一：https://www.jianshu.com/p/2683e27efe9a
 * 参考链接二：https://www.jianshu.com/p/69e5782dd3c3
 */
public class MyService extends Service {

    public static final String TAG = "xp.chen[server]";

    private CopyOnWriteArrayList<Person> mPersonList = new CopyOnWriteArrayList<>();

    private final RemoteCallbackList<IOnNewPersonArrivedListener> mListenerList = new RemoteCallbackList<>();

    private AtomicBoolean mIsServiceDestoryed = new AtomicBoolean(false);

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "服务已启动： onCreate(), pid: " + Process.myPid());
        Log.e(TAG, "是否是在主线程: " + (Looper.myLooper() == Looper.getMainLooper()));
        new Thread(new ServiceWorker()).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "service ----------: onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final ICalculateInterface.Stub mBinder = new ICalculateInterface.Stub() {

        @Override
        public int addNum(int num1, int num2) throws RemoteException {
            return num1 + num2;
        }

        @Override
        public List<Person> addPerson(Person person) throws RemoteException {
            mPersonList.add(person);
            return mPersonList;
        }

        @Override
        public void registerListener(IOnNewPersonArrivedListener listener) throws RemoteException {
            mListenerList.register(listener);
            Log.e(TAG, "registerListener: 当前监听者的数量为：" + mListenerList.getRegisteredCallbackCount());
            // if (!mListenerList.contains(listener)) {
            //     mListenerList.add(listener); // 添加监听
            // } else {
            //     Log.e(TAG, "registerListener: 新添加的listener已存在");
            // }
            // Log.e(TAG, "registerListener: 当前监听者的数量为：" + mListenerList.size());
        }

        @Override
        public void unregisterListener(IOnNewPersonArrivedListener listener) throws RemoteException {
            mListenerList.unregister(listener);
            Log.e(TAG, "unregisterListener: 当前监听者的数量为：" + mListenerList.getRegisteredCallbackCount());

            // if (mListenerList.contains(listener)) {
            //     mListenerList.remove(listener); // 移除监听
            //     Log.e(TAG, "unregisterListener: 移除了一个监听者");
            // } else {
            //     Log.e(TAG, "unregisterListener: 移除监听者失败，找不到指定的监听者");
            // }
            //
            // Log.e(TAG, "unregisterListener: 当前监听者的数量为：" + mListenerList.size());
        }

    };

    Random mRandom = new Random(System.currentTimeMillis());

    private class ServiceWorker implements Runnable {

        @Override
        public void run() {
            while (!mIsServiceDestoryed.get()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.e(TAG, "run: 服务端将要添加一个新的Person... ");

                Person person = new Person("new person name：" + mRandom.nextInt(100), 22);
                try {
                    onNewPersonArrived(person);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void onNewPersonArrived(Person person) throws RemoteException {
        mPersonList.add(person);
        // Log.e(TAG, "有新的人员加入，通知所有监听者，目前监听者数量为：" + mListenerList.size());
        // for (int i = 0; i < mListenerList.size(); i++) {
        //     IOnNewPersonArrivedListener listener = mListenerList.get(i);
        //     Log.e(TAG, "已通知监听者:" + listener);
        //     listener.onNewPersonArrived(person);
        // }
        synchronized (mListenerList) {
            int n = mListenerList.beginBroadcast();

            Log.e(TAG, "有新的人员加入，通知所有监听者，目前监听者数量为：" + n);

            for (int i = 0; i < n; i++) {
                IOnNewPersonArrivedListener listener = mListenerList.getBroadcastItem(i);
                if (listener != null) {
                    listener.onNewPersonArrived(person);
                    Log.e(TAG, "已通知监听者:" + listener);
                }
            }
            mListenerList.finishBroadcast();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "服务已被销毁");
        mIsServiceDestoryed.set(true);
    }

}