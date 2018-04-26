package com.woodys.demo;

import android.app.Application;

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
}
