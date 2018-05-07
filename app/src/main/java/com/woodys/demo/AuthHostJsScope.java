/**
 * Summary: js脚本所能执行的函数空间
 * Version 1.0
 * Date: 13-11-20
 * Time: 下午4:40
 * Copyright: Copyright (c) 2013
 */

package com.woodys.demo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.financial.quantgroup.v2.bus.RxBus;
import com.woodys.demo.entity.DataStateType;
import com.woodys.demo.entity.StateViewType;
import com.woodys.demo.utils.TaskExecutor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.pedant.SafeWebViewBridge.JsCallback;

/**
 * HostJsScope中需要被JS调用的函数，必须定义成public static，且必须包含WebView这个参数
 */
public class AuthHostJsScope {
    /**
     * 短暂气泡提醒
     *
     * @param webView 浏览器
     * @param message 提示信息
     */
    public static void toast(WebView webView, String message) {
        Toast.makeText(webView.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 可选择时间长短的气泡提醒
     *
     * @param webView    浏览器
     * @param message    提示信息
     * @param isShowLong 提醒时间方式
     */
    public static void toast(WebView webView, String message, int isShowLong) {
        Toast.makeText(webView.getContext(), message, isShowLong).show();
    }

    /**
     * 弹出记录的测试JS层到Java层代码执行损耗时间差
     *
     * @param webView   浏览器
     * @param timeStamp js层执行时的时间戳
     */
    public static void testLossTime(WebView webView, long timeStamp) {
        timeStamp = System.currentTimeMillis() - timeStamp;
        alert(webView, String.valueOf(timeStamp));
    }

    /**
     * 系统弹出提示框
     *
     * @param webView 浏览器
     * @param message 提示信息
     */
    public static void alert(WebView webView, String message) {
        // 构建一个Builder来显示网页中的alert对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(webView.getContext());
        builder.setTitle(webView.getContext().getString(R.string.dialog_title_system_msg));
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.create();
        builder.show();
    }

    public static void alert(WebView webView, int msg) {
        alert(webView, String.valueOf(msg));
    }

    public static void alert(WebView webView, boolean msg) {
        alert(webView, String.valueOf(msg));
    }

    /**
     * 获取设备IMSI
     *
     * @param webView 浏览器
     * @return 设备IMSI
     */
    public static String getIMSI(WebView webView) {
        if (ActivityCompat.checkSelfPermission(webView.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return ((TelephonyManager) webView.getContext().getSystemService(Context.TELEPHONY_SERVICE)).getSubscriberId();
    }

    /**
     * 获取用户系统版本大小
     *
     * @param webView 浏览器
     * @return 安卓SDK版本
     */
    public static int getOsSdk(WebView webView) {
        return Build.VERSION.SDK_INT;
    }

    //---------------- 界面切换类 ------------------

    /**
     * 结束当前窗口
     *
     * @param view 浏览器
     */
    public static void goBack(WebView view) {
        if (view.getContext() instanceof Activity) {
            ((Activity) view.getContext()).finish();
        }
    }

    /**
     * 传入Json对象
     *
     * @param view 浏览器
     * @param jo   传入的JSON对象
     * @return 返回对象的第一个键值对
     */
    public static String passJson2Java(WebView view, JSONObject jo) {
        Iterator iterator = jo.keys();
        String res = "";
        while (iterator.hasNext()) {
            try {
                String keyW = (String) iterator.next();
                res += keyW + ": " + jo.getString(keyW);
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
        return res;
    }

    /**
     * 将传入Json对象直接返回
     *
     * @param view 浏览器
     * @param jo   传入的JSON对象
     * @return 返回对象的第一个键值对
     */
    public static JSONObject retBackPassJson(WebView view, JSONObject jo) {
        return jo;
    }

    public static int overloadMethod(WebView view, int val) {
        return val;
    }

    public static String overloadMethod(WebView view, String val) {
        return val;
    }

    public static class RetJavaObj {
        public int intField;
        public String strField;
        public boolean boolField;
    }

    public static List<RetJavaObj> retJavaObject(WebView view) {
        RetJavaObj obj = new RetJavaObj();
        obj.intField = 1;
        obj.strField = "mine str";
        obj.boolField = true;
        List<RetJavaObj> rets = new ArrayList<RetJavaObj>();
        rets.add(obj);
        return rets;
    }

    public static void delayJsCallBack(WebView view, int ms, final String backMsg, final JsCallback jsCallback) {
        TaskExecutor.scheduleTaskOnUiThread(ms * 1000, new Runnable() {
            @Override
            public void run() {
                try {
                    jsCallback.apply(backMsg);
                } catch (JsCallback.JsCallbackException je) {
                    je.printStackTrace();
                }
            }
        });
    }

    public static long passLongType(WebView view, long i) {
        return i;
    }


    /**
     * 网页爬虫授权进度
     *
     * @param webView 浏览器
     * @param json    传入的JSON对象
     * @return 返回对象的第一个键值对
     */
    public static void webViewAuthProgress(WebView webView, JSONObject json) {
        if (BuildConfig.DEBUG) {
            String jsonStr = passJson2Java(webView, json);
            Log.e("测试", "====webViewAuthProgress====  jsonStr:" + jsonStr);
        }
        try {
            //获取进度操作信息
            int progress = json.getInt("progress");
            RxBus.INSTANCE.post(new StateViewType(StateViewType.LAYOUT_LOADING_TYPE, progress));
        } catch (Exception e) {
        }
    }


    /**
     * 网页爬虫授权失败
     *
     * @param webView 浏览器
     *                "errorCode":"0001" //0001密码错误   0002采集错误
     */
    public static void webViewAuthFailure(WebView webView, JSONObject json) {
        if (BuildConfig.DEBUG) {
            String jsonStr = passJson2Java(webView, json);
            Log.e("测试", "====webViewAuthFailure====  jsonStr:" + jsonStr);
        }
        String errorCode = null;
        try {
            errorCode = json.getString("errorCode");
        } catch (Exception e) {
        }
        if (!"0001".equals(errorCode)) {
            //认证失败
            RxBus.INSTANCE.post(new StateViewType(StateViewType.LAYOUT_ERROR_TYPE, 0));
        }
    }


    /**
     * 网页爬虫授权采集结果
     *
     * @param webView 浏览器
     * @param json    传入的JSON对象
     * @return 返回对象的第一个键值对
     */
    public static void webViewAuthCollectionResults(WebView webView, JSONObject json) {
        if (BuildConfig.DEBUG) {
            String jsonStr = passJson2Java(webView, json);
            Log.e("测试", "====webViewAuthCollectionResults====  jsonStr:" + jsonStr);
        }
        try {
            String type = (String) webView.getTag();
            String data = json.getString("data");
            //认证成功
            RxBus.INSTANCE.post(new DataStateType(type, data));
        } catch (Exception e) {
        }
    }


    /**
     * 设置webview的userAgent
     *
     * @param webView 浏览器
     * @param json    传入的JSON对象
     * @return 返回对象的第一个键值对
     */
    public static void webViewAuthSetUserAgent(WebView webView, JSONObject json) {
        if (BuildConfig.DEBUG) {
            String jsonStr = passJson2Java(webView, json);
            Log.e("测试", "====webViewAuthSetUserAgent====  jsonStr:" + jsonStr);
        }
        try {
            String url = json.getString("url");
            String userAgent = json.getString("userAgent");
            webView.getSettings().setUserAgentString(userAgent);
            if (!TextUtils.isEmpty(url)) {
                String javascript = String.format("window.location.href='%s';", url);
                webView.loadUrl("javascript:" + javascript);
            }
        } catch (Exception e) {
        }
    }

    /**
     * 将cookie设置到 WebView
     *
     * @param url    要加载的 url
     * @param cookie 要同步的 cookie
     */
    public static void syncCookie(WebView webview, String url, String cookie) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(webview.getContext());
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();// 移除
        cookieManager.removeAllCookie();
        //部分手机WebView无法成功同步Cookie问题的解决方案
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webview, true);
        }
        /**
         * cookie 设置形式
         * cookieManager.setCookie(url, "key=value;" + "domain=[your domain];path=/;")
         **/
        getCookieAllKeysByCookieString(cookie);
        cookieManager.setCookie(url, cookie);
        CookieSyncManager.getInstance().sync();
    }


    public static List<String> getCookieAllKeysByCookieString(String pCookies) {
        if (TextUtils.isEmpty(pCookies)) {
            return null;
        }
        String[] cookieField = pCookies.split(";");
        int len = cookieField.length;
        for (int i = 0; i < len; i++) {
            cookieField[i] = cookieField[i].trim();
        }
        List<String> allCookieField = new ArrayList<>();
        for (int i = 0; i < len; i++) {
            if (TextUtils.isEmpty(cookieField[i])) {
                continue;
            }
            if (!cookieField[i].contains("=")) {
                continue;
            }
            String[] singleCookieField = cookieField[i].split("=");
            allCookieField.add(singleCookieField[0]);
        }
        if (allCookieField.isEmpty()) {
            return null;
        }
        return allCookieField;
    }
}