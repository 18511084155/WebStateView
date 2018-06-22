package com.woodys.demo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.financial.quantgroup.v2.bus.RxBus;
import com.google.gson.JsonObject;
import com.quant.titlebar.TitleBarActivity;
import com.woodys.demo.entity.DataStateType;
import com.woodys.demo.entity.RefreshStatus;
import com.woodys.demo.entity.StateViewType;
import com.woodys.demo.utils.PackageUtils;
import com.woodys.demo.utils.Res;
import com.woodys.demo.utils.systembar.SystemBarTintUtils;
import com.woodys.keyboard.InputMethodHolder;
import com.woodys.keyboard.OnInterceptMethodListener;
import com.woodys.stateview.ViewHelperController;

import java.io.File;
import java.lang.reflect.Method;

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
        WebViewUseReduceTime.initUseReduceTime();
        //初始化view
        titleBar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivity();
            }
        });
        webView = (WebView) findViewById(R.id.web_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        //获取数据信息
        Bundle arguments = getIntent().getExtras();
        String userAgent = null;
        if (null != arguments) {
            webUrl = arguments.getString("url");
            if (BuildConfig.DEBUG) Log.e("测试", "====webUrl====  url:" + webUrl);
            webTitle = arguments.getString("title");
            webType = arguments.getString("type");
            webJavaScript = arguments.getString("javascript");
            webReturnUrl = arguments.getString("returnUrl");
            userAgent = arguments.getString("userAgent");
        }
        if (!TextUtils.isEmpty(webTitle)) titleBar.setTitleText(webTitle);

        //设置类型
        webView.setTag(webType);
        //设置webview的配置信息
        if (TextUtils.isEmpty(userAgent)) {
            userAgent = userAgent + " xyqb/" + PackageUtils.getAppVersion();
        }
        initWebSettings(webView, userAgent);
        //设置webview的事件监听操作
        webView.setWebViewClient(new MyWebViewClient());
        webView.setDownloadListener(new MyWebViewDownLoadListener());
        webView.setWebChromeClient(new CustomChromeClient("xyqbNative", AuthHostJsScope.class));

        //初始化状态控制器
        helperController = getViewHelperController();
        //进行webview的地址跳转操作
        if (!TextUtils.isEmpty(webUrl)) {
            RxBus.INSTANCE.post(new DataStateType(webType, "BEGIN", null, null));
            //显示内容
            helperController.restore();
            progressBar.startProgressAnim();
            webView.loadUrl(webUrl);
        } else {
            //显示空页面试图
            helperController.showEmptyView();
        }

        InputMethodHolder.setOnInterceptMethodListener(new OnInterceptMethodListener() {
            @Override
            public Pair<Boolean, Object> onIntercept(Object obj, Method method, Object result) {
                Pair<Boolean, Object> objectPair = null;
                if (helperController.getCurrentView() != helperController.getContentView()) {
                    String methodName = method.getName();
                    if ("showSoftInput".equals(methodName)) {
                        objectPair = new Pair(true, true);
                    }
                }
                return objectPair;
            }
        });
    }

    /**
     * 设置webview的配置信息
     *
     * @param webView
     * @param userAgent
     */
    private void initWebSettings(WebView webView, String userAgent) {
        WebSettings webSetting = webView.getSettings();

        //如果访问的页面中有Javascript，则webview必须设置支持Javascript
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setLoadWithOverviewMode(true);
        // 设置可以访问文件
        webSetting.setAllowFileAccess(true);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(false);
        webSetting.setCacheMode(WebSettings.LOAD_NO_CACHE);
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
        webSetting.setAppCacheEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                webView.removeJavascriptInterface("searchBoxJavaBridge_");
                webView.removeJavascriptInterface("accessibility");
                webView.removeJavascriptInterface("accessibilityTraversal");
            } catch (Throwable tr) {
                tr.printStackTrace();
            }
        }

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
                                int progress = item.value;
                                helperController.setLoadingView(item.value);
                                //当前假如进度是100，就延迟700ms显示加载成功
                                if (progress >= 100) {
                                    titleBar.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            helperController.showSuccessView();
                                            setDownTimerschedule(4 * 1000, 2 * 1000);
                                        }
                                    }, 500);
                                }
                            } else {
                                helperController.showLoadingView();
                            }
                            break;
                        case StateViewType.LAYOUT_ERROR_TYPE:
                            helperController.showErrorView();
                            setDownTimerschedule(4 * 1000, 2 * 1000);
                            break;
                        case StateViewType.LAYOUT_SUCCESS_TYPE:
                            helperController.showSuccessView();
                            setDownTimerschedule(4 * 1000, 2 * 1000);
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
            if (!TextUtils.isEmpty(title) && !title.contains("title") && !title.contains("http") && !title.contains("www.") && title.length() <= 10) {
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
        public void onRequestFocus(WebView view) {
            super.onRequestFocus(view);
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
    public void onBackPressed() {
        finishActivity();
    }

    /**
     * 方便统计是否是用户主动关闭的
     */
    public void finishActivity() {
        RxBus.INSTANCE.post(new DataStateType(webType, "FINISH", null,new JsonCallback() {
            @Override
            public String convertData(JsonObject jsonObject) {
                if (null == jsonObject) return null;
                jsonObject.addProperty("time", WebViewUseReduceTime.getUseReduceTimeByReplace());
                return jsonObject.toString();
            }
        }));
        RxBus.INSTANCE.post(new RefreshStatus(webType));
        finish();
    }

    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        InputMethodHolder.clearOnInterceptMethodListener();
        RxBus.INSTANCE.unSubscribeItems(this);
        super.onDestroy();
    }

    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent,
                                    String contentDisposition, String mimetype, long contentLength) {
            if(webView != null && webView.getWindowToken()!=null) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }

    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (!url.startsWith("tmall://") && !url.startsWith("tbopen://")) {
                super.onPageStarted(view, url, favicon);
            }
            if (BuildConfig.DEBUG) Log.e("测试", "====onPageStarted====  url:" + url);
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

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();// 接受所有网站的证书
        }


        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (BuildConfig.DEBUG) Log.e("测试", "====shouldOverrideUrlLoading====  url:" + url);
            if (null != webReturnUrl && webReturnUrl.equals(url)) {
                helperController.showLoadingView();
            }
            if (!url.startsWith("tmall://") && !url.startsWith("tbopen://")) {
                view.loadUrl(url);
            }
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
        public void onPageFinished(WebView view, final String url) {
            super.onPageFinished(view, url);
            if (BuildConfig.DEBUG) Log.e("测试", "====onPageFinished====  url:" + url);
            if (null == view) return;
            //注入返回的js代码
            if (!TextUtils.isEmpty(webJavaScript)) {
                webView.loadUrl("javascript:" + webJavaScript);
            }
            progressBar.setVisibility(View.GONE);
            if (null != webReturnUrl && webReturnUrl.equals(url)) {
                try {
                    //认证成功
                    final String userAgentString = webView.getSettings().getUserAgentString();
                    RxBus.INSTANCE.post(new DataStateType(webType, "DATA", null, new JsonCallback() {
                        @Override
                        public String convertData(JsonObject jsonObject) {
                            if (null == jsonObject) return null;
                            jsonObject.addProperty("userAgent", userAgentString);
                            CookieManager cookieManager = CookieManager.getInstance();
                            String cookieStr = cookieManager.getCookie(url);
                            jsonObject.addProperty("cookie", cookieStr);
                            return jsonObject.toString();
                        }
                    }));
                } catch (Exception e) {
                }
            }
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

    /**
     * 设置倒计时操作
     *
     * @param millis
     */
    private void setDownTimerschedule(long millis, final long timeMillis) {
        final boolean[] isTimeMillis = {false};
        /** 倒计时3秒，一次1秒 */
        CountDownTimer timer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isTimeMillis[0] && millisUntilFinished <= timeMillis) {
                    isTimeMillis[0] = true;
                    RxBus.INSTANCE.post(new RefreshStatus(webType));
                }
            }

            @Override
            public void onFinish() {
                //倒计时完毕了
                finish();
            }
        }.start();
    }
}
