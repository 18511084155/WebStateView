package com.woodys.demo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    Button taobao;

    private WebView webView;
    private ProgressBar progressBar;

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.i("ansen", "页面加载完成: " + url);

            if (!url.contains("login") && !url.contains("jifen")) {
//                view.loadUrl("javascript:xyqbNative.webViewAuthFailure();");
                // 验证 登录淘宝后href到积分，是否会重新登录
                view.loadUrl("javascript:function jumpJifen() {window.location.href = \"https://login.taobao.com/jump?target=https%3A%2F%2Fpages.tmall.com%2Fwow%2Fjifen%2Fact%2Fpoint-details\"};jumpJifen();");
            }

            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.i("ansen", "拦截url: " + request.toString());
            return super.shouldOverrideUrlLoading(view, request);
        }
    };

    private WebChromeClient webChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            Log.i("ansen", "网页标题: " + title);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setProgress(newProgress);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
            localBuilder.setMessage(message).setPositiveButton("确定",null);
            localBuilder.setCancelable(false);
            localBuilder.create().show();
            result.confirm();
            return true;
//            return super.onJsAlert(view, url, message, result);
        }
    };

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        taobao = (Button) findViewById(R.id.taobao);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        webView = (WebView) findViewById(R.id.webview);

        taobao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadUrl("https://login.m.taobao.com/login.htm?tpl_redirect_url=https%3a%2f%2fmember1.taobao.com%2fmember%2ffresh%2faccount_profile.htm");
                webView.setWebChromeClient(webChromeClient);
                webView.setWebViewClient(webViewClient);

                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);

                webSettings.setSupportZoom(true);
                webSettings.setBuiltInZoomControls(true);

                webView.addJavascriptInterface(new JSInterface(), "xyqbNative");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
        webView = null;
    }

    private final class JSInterface {
        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void webViewAuthProgress () {
            Log.i("jsface", "authProgress");
        }

        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void webViewAuthCollectionResults () {
            Log.i("jsface", "authResults");
        }

        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void webViewAuthFailure () {
            Log.i("jsface", "authFail");
        }

        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void webViewAuth () {
            Log.i("jsface", "auth");
        }
    }
}