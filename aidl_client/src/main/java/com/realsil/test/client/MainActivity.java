package com.realsil.test.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.realsil.test.server.ICalculateInterface;
import com.realsil.test.server.IOnNewPersonArrivedListener;
import com.realsil.test.server.Person;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "xp.chen[client]";

    private ICalculateInterface mICalculateInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    private void bind_service() {
        Log.i(TAG, "准备绑定服务...: ");
        Intent intent = new Intent();
        intent.setAction("com.realsil.android.test.service");
        intent.setPackage("com.realsil.test.server");
        // intent.setComponent(new ComponentName("com.realsil.test.server", "com.realsil.test.server.MyService"));
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private ClientHandler mClientHandler = new ClientHandler();

    private static class ClientHandler extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.i(TAG, "远程服务端通知收到了：new person : " + msg.obj);
        }

    }

    private final IOnNewPersonArrivedListener mIOnNewPersonArrivedListener = new IOnNewPersonArrivedListener.Stub() {
        @Override
        public void onNewPersonArrived(Person person) throws RemoteException {
            // 收到消息后将其转发出去
            mClientHandler.obtainMessage(0, person).sendToTarget();
        }
    };

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected: 成功连接到服务");
            mICalculateInterface = ICalculateInterface.Stub.asInterface(service);

            // 给 Binder设置死亡代理，当Binder死亡时就可以收到通知
            try {
                // 监听其它监听者到来的迅息
                mICalculateInterface.registerListener(mIOnNewPersonArrivedListener);
                service.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceConnected: 服务已断开");
            mICalculateInterface = null;
        }

    };

    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            Log.i(TAG, "Binder 已死");

            if (mICalculateInterface == null) {
                return;
            }

            mICalculateInterface.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mICalculateInterface = null;
            bind_service();
        }

    };

    private void unbind_service() {
        Log.i(TAG, "客户端取消绑定服务: ");
    }

    public void bindService(View view) {
        bind_service();
    }

    public void unBindService(View view) {
        unbind_service();
    }

    public void calculateResult(View view) {
        if (mICalculateInterface != null) {
            try {
                int ret = mICalculateInterface.addNum(3, 5);
                Log.i(TAG, "3 + 5 = " + ret);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void addNewPerson(View view) {
        if (mICalculateInterface != null) {
            try {
                List<Person> personList = mICalculateInterface.addPerson(new Person("zhangsan", 20));
                Log.i(TAG, "addNewPerson: " + personList.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mICalculateInterface != null && mICalculateInterface.asBinder().isBinderAlive()) {
            try {
                Log.i(TAG, "客户退出监听服务：unregister listener : " + mIOnNewPersonArrivedListener);
                mICalculateInterface.unregisterListener(mIOnNewPersonArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbind_service();
    }

}