package com.woodys.demo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.financial.quantgroup.v2.bus.RxBus;
import com.woodys.demo.entity.StateViewType;
import com.woodys.stateview.ViewHelperController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SafeWebViewBridge.InjectedChromeClient;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class WebActivity extends Activity {
    public static final int REFRESH_AUTH_STATUS_CODE = 0x0010;
    WebView webView = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        webView=(WebView) findViewById(R.id.web_view);
        WebSettings webSetting = webView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(false);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSetting.setDomStorageEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setAppCacheEnabled(false);
        webSetting.setAppCacheMaxSize(1024 * 1024 * 8);
        //启用地理定位
        webSetting.setGeolocationEnabled(true);
        String PICASSO_CACHE = "picasso-cache";
        webSetting.setGeolocationDatabasePath(PICASSO_CACHE);
        webSetting.setAppCacheEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }


        webView.setWebChromeClient(
            new CustomChromeClient("xyqbNative", HostJsScope.class)
        );

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        String url = "http://192.168.28.30:8080/test/index.html";
        //webView.loadUrl(url);

        webView.loadUrl("file:///android_asset/index.html");

    }


    /**
     * 网页爬虫授权完成后返回上级页面刷新状态
     */
    private void refreshAuthStatus(){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("event", "webViewRefreshAuthStatus");
            Map<String,String> stringMap = new HashMap<String,String>();
            stringMap.put("type","TAOBAO");
            jsonObject.put("data",stringMap);
            webView.loadUrl("javascript:webViewRefreshAuthStatus(" + jsonObject + ")");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(REFRESH_AUTH_STATUS_CODE==requestCode && resultCode==RESULT_OK ){
            //注入js代码
            refreshAuthStatus();
        }
    }

    public class CustomChromeClient extends InjectedChromeClient {

        public CustomChromeClient (String injectedName, Class injectedCls) {
            super(injectedName, injectedCls);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            // to do your work
            new AlertDialog.Builder(WebActivity.this).setTitle("温馨提示").setMessage(message).setCancelable(true).setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create().show();
            // ...
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public void onProgressChanged (WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            // to do your work
            // ...
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            // to do your work
            // ...
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }
    }
}
