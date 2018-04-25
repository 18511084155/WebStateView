package com.woodys.demo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.pedant.SafeWebViewBridge.InjectedChromeClient;


public class WebActivity extends Activity {
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
                String javascript = getIntent().getStringExtra("javascript");
                if(!TextUtils.isEmpty(javascript)) webView.loadUrl("javascript:" + javascript);
            }
        });


        String url = getIntent().getStringExtra("url");
        if(null==url) url = "http://192.168.28.30:8080/test/index.html";
        //webView.loadUrl(url);

        webView.loadUrl("file:///android_asset/test.html");
    }

    public class CustomChromeClient extends InjectedChromeClient {

        public CustomChromeClient (String injectedName, Class injectedCls) {
            super(injectedName, injectedCls);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            // to do your work
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
