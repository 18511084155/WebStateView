package com.woodys.demo;

public class WebViewUseReduceTime {
    private static long appUseReduceTime = 0L;

    public static void initUseReduceTime(){
        appUseReduceTime = System.currentTimeMillis();
    }

    public static long getUseReduceTime(){
        long useReduceTime = System.currentTimeMillis() - appUseReduceTime;
        return useReduceTime;
    }

    public static long getUseReduceTimeByReplace(){
        long useReduceTime = System.currentTimeMillis() - appUseReduceTime;
        appUseReduceTime = System.currentTimeMillis();
        return useReduceTime;
    }
}
