package com.woodys.demo;

import android.app.Application;
import android.content.Context;

import com.woodys.keyboard.InputMethodHolder;
import com.woodys.libsocket.sdk.OkSocket;


/**
 * Created by woodys on 2017/4/22.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OkSocket.initialize(this, true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        // 建议在此处初始化
        InputMethodHolder.init(base);
        super.attachBaseContext(base);
    }
}
