package com.woodys.demo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.financial.quantgroup.v2.bus.RxBus;
import com.quant.titlebar.TitleBar;
import com.woodys.demo.utils.InputMethodUtils;
import com.woodys.demo.utils.PackageUtils;
import com.woodys.demo.utils.Res;
import com.woodys.demo.utils.systembar.SystemBarTintUtils;

import java.io.File;
import java.net.URISyntaxException;

import cn.pedant.SafeWebViewBridge.InjectedChromeClient;

/**
 * Created by baobao on 17/1/11.
 */

public class OtherWebviewActivity extends FragmentActivity {
    private WebView webview;
    private WebSettings webSetting;
    private String turl, userAgent;
    private ProgressBar progressBar;
    private String message, qrcodeUrl;
    private String closeurl;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private String mytitle;
    private String zhifubua;
    private String type;
    private TitleBar titleBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.otherwebview);
        SystemBarTintUtils.initSystemBarTint(this, Res.getColor(R.color.colorPrimary));
        titleBar = (TitleBar) findViewById(R.id.titleBar);
        titleBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        titleBar.setVisibility(View.VISIBLE);
        Bundle arguments = getIntent().getExtras();
        if (null != arguments) {
            turl = arguments.getString("turl");
            mytitle = arguments.getString("title");
            message = arguments.getString("message");
            closeurl = arguments.getString("closeurl");
            qrcodeUrl = arguments.getString("qrcodeUrl");
            userAgent = arguments.getString("userAgent");
            type = arguments.getString("type");
        }
        webview = (WebView) findViewById(R.id.web_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        webview.setWebViewClient(new MyWebViewClient());


        webview.setDownloadListener(new MyWebViewDownLoadListener());

        webview.requestFocus();

        webSetting = webview.getSettings();
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
        webSetting.setMinimumFontSize(1);
        webSetting.setMinimumLogicalFontSize(1);
        webSetting.setAppCacheMaxSize(1024 * 1024 * 8);
        //启用地理定位
        webSetting.setGeolocationEnabled(true);
        String PICASSO_CACHE = "picasso-cache";
        File filesPicDir = new File(getCacheDir(), PICASSO_CACHE);
        webSetting.setGeolocationDatabasePath(PICASSO_CACHE);
        webSetting.setAppCachePath(filesPicDir.getAbsolutePath());
        webSetting.setAppCacheEnabled(false);
        if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 17) {
            try {
                webview.removeJavascriptInterface("searchBoxJavaBridge_");
                webview.removeJavascriptInterface("accessibility");
                webview.removeJavascriptInterface("accessibilityTraversal");
            } catch (Throwable tr) {
                tr.printStackTrace();
            }
        }
        String ua = webview.getSettings().getUserAgentString();
        zhifubua = webview.getSettings().getUserAgentString();
        if (!TextUtils.isEmpty(userAgent)) {
            webview.getSettings().setUserAgentString(userAgent);
        } else {
            webview.getSettings().setUserAgentString(ua + " xyqb/" + PackageUtils.getAppVersion());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        CustomChromeClient customChromeClient = new CustomChromeClient("xyqbNative", HostJsScope.class);
        webview.setWebChromeClient(customChromeClient);
//        turl="http://192.168.4.9:7001/app-landing?registerFrom=214&channelId=1&token=e243066d-e5d4-45d0-b429-4b1378fd7f25&appChannel=AppStore-Test";
//        turl = "http://www.yongqianbao.com/w/index?c=516";
        webview.loadUrl(turl);
//        webview.loadUrl("file:///android_asset/rem/index.html");
        titleBar.setTitleText(mytitle);

    }

    /**
     * 视频全屏参数
     */
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;

    public class CustomChromeClient extends InjectedChromeClient {

        public CustomChromeClient(String injectedName, Class injectedCls) {
            super(injectedName, injectedCls);
        }

        @Override
        public void onExceededDatabaseQuota(String url,
                                            String databaseIdentifier, long currentQuota,
                                            long estimatedSize, long totalUsedQuota,
                                            WebStorage.QuotaUpdater quotaUpdater) {
            quotaUpdater.updateQuota(estimatedSize * 2);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (!TextUtils.isEmpty(title) && !title.contains("title") && !title.contains("htt") && !title.contains("www.") && title.length() <= 10) {
                if (TextUtils.isEmpty(mytitle))
                    titleBar.setTitleText(title);
            }
        }

        @Override
        public View getVideoLoadingProgressView() {
            FrameLayout frameLayout = new FrameLayout(OtherWebviewActivity.this);
            frameLayout.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
            return frameLayout;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            showCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            HideCustomView();
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            // to do your work
            // ...
            Log.e("hhh", message + "params=" + result);

            new AlertDialog.Builder(OtherWebviewActivity.this).
                    setTitle("温馨提示").
                    setMessage(message).
                    setCancelable(true).
                    setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();


            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setProgress(newProgress);
            if (100 <= newProgress) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                    ViewCompat.animate(progressBar).alpha(0f).setDuration(300);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            // to do your work
            // ...
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }
    }

    @Override
    public void onResume() {
        try {
            webview.getClass().getMethod("onResume").invoke(webview, (Object[]) null);
            webview.onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        try {
            webview.getClass().getMethod("onPause").invoke(webview, (Object[]) null);
            webview.onPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }


    @Override
    public void onDestroy() {
        if (webview != null) {
            webview.destroy();
            webview = null;
            // 清除cookie即可彻底清除缓存
            removeAllCookies();
        }
        RxBus.INSTANCE.unSubscribeItems(this);
        super.onDestroy();
    }

    private void removeAllCookies() {
        CookieSyncManager.getInstance();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookie();
        } else {
            CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    //nothing-todo
                }
            });
        }
    }

    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent,
                                    String contentDisposition, String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            finish();
        }

    }


    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            if (null == webview)
                return;
            if (!TextUtils.isEmpty(webview.getTitle()) && !webview.getTitle().contains("{") && !webview.getTitle().contains("www.") && webview.getTitle().length() <= 15) {
                titleBar.setTitleText(webview.getTitle());
            }
            Log.e("url111", url);
            Log.e("getUserAgentString", "getUserAgentString = " + webview.getSettings().getUserAgentString());
//            webview.loadUrl("javascript:initializeXYAB()");
            progressBar.setVisibility(View.GONE);
            try {
                String[] strArrays = new String[]{};
                if (!TextUtils.isEmpty(message)) {
                    strArrays = message.split(",");
                    CookieManager cookieManager = CookieManager.getInstance();
                    String cookieStr = cookieManager.getCookie(url);
                    if (!TextUtils.isEmpty(cookieStr)) {
                        String ua = webSetting.getUserAgentString();
                        int cookienum = 0;
                        Log.e("strArrays", strArrays.toString());
                        for (int i = 0; i < strArrays.length; i++) {
                            if (cookieStr.contains(strArrays[i])) {
                                cookienum = cookienum + 1;
                            }
                        }
                        if (!TextUtils.isEmpty(type) && "1".equals(type)) {
                            if (cookienum > 0 && cookienum < strArrays.length) {
                                clearWebViewCache();
                            } else if (cookienum >= 3) {
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putString("CookieStr", cookieStr);
                                bundle.putString("ua", ua);
                                intent.putExtras(bundle);
                                clearWebViewCache();
                                setResult(Constant.RESULTZHIFUBAO, intent);
                                finish();
//                                RxBus.post(new AuthZhifubaoTypeEvent(0));
                            }
                        } else {
                            if (cookienum < 3) {
                                if (!TextUtils.isEmpty(qrcodeUrl)) {
                                    int qrcode = 0;
                                    String[] qrcodeArrays = qrcodeUrl.split(",");
                                    for (int i = 0; i < qrcodeArrays.length; i++) {
                                        if (url.contains(qrcodeArrays[i])) {
                                            qrcode = qrcode + 1;
                                        }
                                    }
                                    if (qrcode >= qrcodeArrays.length) {
                                        webview.getSettings().setUserAgentString(zhifubua + " xyqb/" + PackageUtils.getAppVersion());
                                    }
                                } else {
                                    clearWebViewCache();
                                }
                            } else if (cookienum >= 3) {
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putString("CookieStr", cookieStr);
                                bundle.putString("ua", ua);
                                intent.putExtras(bundle);
                                setResult(Constant.RESULT, intent);
                                clearWebViewCache();
                                finish();
                            }
                        }
                    }
                }

            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        public void clearWebViewCache() {
            // 清除cookie即可彻底清除缓存
            CookieSyncManager.createInstance(Res.getContext());
            CookieManager.getInstance().removeAllCookie();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            try {
                progressBar.setProgress(0);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                    ViewCompat.animate(progressBar).alpha(1f).setDuration(100);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
                if (url.startsWith("alipayqr") || url.startsWith("intent://")) {
//                view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivityForResult(intent, Constant.RESULT);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            // TODO Auto-generated method stub
            super.onReceivedError(view, errorCode, description, failingUrl);
            progressBar.setVisibility(View.GONE);
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                //这里拦截webview的xxqb应用内schema跳转
                if (url.contains("public/blank-note/index.html")) {
                    finish();
                } else if (!TextUtils.isEmpty(closeurl) && url.contains(closeurl)) {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else if (url.contains("localhost")) {
                    Log.e("url555", url + "closeurl=" + closeurl);
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else if ((url.startsWith("http:") || url.startsWith("https:"))) {
                    view.loadUrl(url);
                } else {
                    if (url.startsWith("intent://")) {
                        Intent intent;
                        try {
                            InputMethodUtils.hideKeyboard(OtherWebviewActivity.this);
                            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            intent.addCategory("android.intent.category.BROWSABLE");
                            intent.setComponent(null);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                                intent.setSelector(null);
                            }
                            startActivity(intent);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.addCategory("android.intent.category.BROWSABLE");
                        intent.setComponent(null);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                            intent.setSelector(null);
                        }
                        startActivityForResult(intent, Constant.RESULT);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            // TODO Auto-generated method stub
            return super.shouldOverrideKeyEvent(view, event);
        }
    }


    private void clear() {
        if (mUploadMessage != null) {
            mUploadMessage.onReceiveValue(null);
            mUploadMessage = null;
        }
        if (mUploadCallbackAboveL != null) {
            Uri[] results = new Uri[]{};
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        }
    }

    public boolean fileIsExists(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 视频播放全屏
     **/
    private void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }
        getWindow().getDecorView();
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        fullscreenContainer = new FullscreenHolder(this);
        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        setStatusBarVisibility(false);
        customViewCallback = callback;
    }


    /**
     * 全屏容器界面
     */
    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    boolean canGoBack = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (customView != null) {
                HideCustomView();
            } else if (canGoBack = webview.canGoBack()) {
                webview.goBack();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 隐藏视频全屏
     */
    public void HideCustomView() {
        if (customView == null) {
            return;
        }
        setStatusBarVisibility(true);
        FrameLayout decor = (FrameLayout) getWindow().getDecorView();
        decor.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
        webview.setVisibility(View.VISIBLE);
    }

}
