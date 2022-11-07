package com.realsil.test.server;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    public static final String TAG = "xp.chen[server]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "准备启动服务...");
        startService(new Intent(getApplicationContext(), MyService.class));
    }
}