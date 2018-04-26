package com.woodys.demo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.financial.quantgroup.v2.bus.RxBus;
import com.quant.titlebar.TitleBarActivity;
import com.woodys.demo.entity.StateViewType;
import com.woodys.demo.utils.PackageUtils;
import com.woodys.demo.utils.Res;
import com.woodys.demo.utils.systembar.SystemBarTintUtils;
import com.woodys.stateview.ViewHelperController;

import java.io.File;

import cn.pedant.SafeWebViewBridge.InjectedChromeClient;
import cz.widget.progress.ProgressBar;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * Created by yuetao on 18/4/26.
 */

public class AuthWebActivity extends TitleBarActivity {
    private WebView webView;
    private ProgressBar progressBar;

    private String webUrl;
    private String webTitle;
    private String webType;
    private String webJavaScript;
    private String webReturnUrl;

    private ViewHelperController helperController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_auth_webview);
        SystemBarTintUtils.initSystemBarTint(this, Res.getColor(R.color.colorPrimary));
        //初始化view
        titleBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        webView = (WebView) findViewById(R.id.web_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        //获取数据信息
        Bundle arguments = getIntent().getExtras();
        String userAgent = null;
        if (null != arguments) {
            webUrl = arguments.getString("url");
            if(BuildConfig.DEBUG) Log.e("测试", "====webUrl====  url:"+webUrl);
            webTitle = arguments.getString("title");
            webType = arguments.getString("type");
            webJavaScript = arguments.getString("javascript");
            webReturnUrl = arguments.getString("returnUrl");
            if(BuildConfig.DEBUG) Log.e("测试", "====webReturnUrl====  url:"+webReturnUrl);
            userAgent = arguments.getString("userAgent");
        }

        //设置webview的配置信息
        initWebSettings(webView, userAgent);
        //设置webview的事件监听操作
        webView.setWebViewClient(new MyWebViewClient());
        webView.setDownloadListener(new MyWebViewDownLoadListener());
        webView.setWebChromeClient(new CustomChromeClient("xyqbNative", AuthHostJsScope.class));

        //初始化状态控制器
        helperController = getViewHelperController();
        //进行webview的地址跳转操作
        if (!TextUtils.isEmpty(webUrl)) {
            //显示内容
            helperController.restore();
            if (!TextUtils.isEmpty(webTitle)) titleBar.setTitleText(webTitle);
            progressBar.startProgressAnim();
            webView.loadUrl(webUrl);
        } else {
            //显示空页面试图
            helperController.showEmptyView();
        }
    }

    /**
     * 设置webview的配置信息
     *
     * @param webView
     * @param userAgent
     */
    private void initWebSettings(WebView webView, String userAgent) {
        WebSettings webSetting = webView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(false);
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSetting.setDomStorageEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setMinimumFontSize(1);
        webSetting.setMinimumLogicalFontSize(1);
        webSetting.setAppCacheMaxSize(1024 * 1024 * 8);

        if (!TextUtils.isEmpty(userAgent)) {
            webView.getSettings().setUserAgentString(userAgent);
        }
        //启用地理定位
        webSetting.setGeolocationEnabled(true);
        String PICASSO_CACHE = "picasso-cache";
        File filesPicDir = new File(getCacheDir(), PICASSO_CACHE);
        webSetting.setGeolocationDatabasePath(PICASSO_CACHE);
        webSetting.setAppCachePath(filesPicDir.getAbsolutePath());
        webSetting.setAppCacheEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }


    }

    /**
     * 初始化状态选择器
     *
     * @return
     */
    private ViewHelperController getViewHelperController() {
        final ViewHelperController helperController = ViewHelperController.createCaseViewHelperController(webView);
        findViewById(R.id.text1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helperController.restore();
            }
        });
        findViewById(R.id.text2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helperController.showLoadingView();
            }
        });
        findViewById(R.id.text3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helperController.showErrorView();
            }
        });
        findViewById(R.id.text4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helperController.showSuccessView();
            }
        });


        RxBus.INSTANCE.subscribe(this, StateViewType.class, new Function1<StateViewType, Unit>() {
            @Override
            public Unit invoke(StateViewType item) {
                if (null != item) {
                    switch (item.type) {
                        case StateViewType.LAYOUT_CONTENT_TYPE:
                            helperController.restore();
                            break;
                        case StateViewType.LAYOUT_LOADING_TYPE:
                            if (helperController.isShowLoadingView()) {
                                helperController.setLoadingView(item.value);
                            } else {
                                helperController.showLoadingView();
                            }
                            break;
                        case StateViewType.LAYOUT_ERROR_TYPE:
                            helperController.showErrorView();
                            break;
                        case StateViewType.LAYOUT_SUCCESS_TYPE:
                            helperController.showSuccessView();
                            break;
                    }
                }
                return null;
            }
        });

        return helperController;
    }

    public class CustomChromeClient extends InjectedChromeClient {

        public CustomChromeClient(String injectedName, Class injectedCls) {
            super(injectedName, injectedCls);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (!TextUtils.isEmpty(title) && !title.contains("title") && !title.contains("htt") && !title.contains("www.") && title.length() <= 10) {
                if (TextUtils.isEmpty(webTitle) && null != titleBar) titleBar.setTitleText(title);
            }
        }

        @Override
        public View getVideoLoadingProgressView() {
            FrameLayout frameLayout = new FrameLayout(AuthWebActivity.this);
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
            new AlertDialog.Builder(AuthWebActivity.this).
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
            if (progressBar.getProgress() < progressBar.getFirstProgress()) {
                progressBar.passProgressAnim(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        progressBar.animate().setStartDelay(100).alpha(0f);
                        return null;
                    }
                });
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
            webView.getClass().getMethod("onResume").invoke(webView, (Object[]) null);
            webView.onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        try {
            webView.getClass().getMethod("onPause").invoke(webView, (Object[]) null);
            webView.onPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        RxBus.INSTANCE.unSubscribeItems(this);
        super.onDestroy();
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
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if(BuildConfig.DEBUG) Log.e("测试", "====onPageStarted====  url:"+url);
            try {
                progressBar.setProgress(0);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                    ViewCompat.animate(progressBar).alpha(1f).setDuration(100);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            if(BuildConfig.DEBUG) Log.e("测试", "====onLoadResource====  url:"+url);
            super.onLoadResource(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(BuildConfig.DEBUG) Log.e("测试", "====shouldOverrideUrlLoading====  url:"+url);
            if(null!=webReturnUrl && webReturnUrl.equals(url)) helperController.showLoadingView();
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            progressBar.setVisibility(View.GONE);
        }


        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if(BuildConfig.DEBUG) Log.e("测试", "====onPageFinished====  url:"+url);
            super.onPageFinished(view, url);
            if (null == webView)
                return;
            //用JS 禁止弹出手机键盘
            String javascript="var inputs = document.getElementsByTagName('input');\n" +
                    "for (var i = inputs.length - 1; i >= 0; i--) {\n" +
                    "    inputs[i].onfocus = function () {\n" +
                    "         inputs[i].readOnly = true;\n" +
                    "    }\n" +
                    "}";
            if(null!=webReturnUrl && webReturnUrl.equals(url)) webView.loadUrl("javascript:" + javascript);
            //注入返回的js代码
            if (!TextUtils.isEmpty(webJavaScript)) webView.loadUrl("javascript:" + webJavaScript);
            String webViewTitle = webView.getTitle();
            if (!TextUtils.isEmpty(webViewTitle) && !webViewTitle.contains("{") && !webViewTitle.contains("www.") && webViewTitle.length() <= 15) {
                titleBar.setTitleText(webViewTitle);
            }
            progressBar.setVisibility(View.GONE);
        }
    }


    boolean canGoBack = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (customView != null) {
                HideCustomView();
            } else if (canGoBack = webView.canGoBack()) {
                webView.goBack();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private View customView;
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;


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
        /**
         * 视频全屏参数
         */
        FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
        webView.setVisibility(View.VISIBLE);
    }

}
