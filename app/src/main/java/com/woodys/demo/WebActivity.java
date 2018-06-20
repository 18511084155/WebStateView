package com.woodys.demo;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;

import com.financial.quantgroup.v2.bus.RxBus;
import com.woodys.demo.entity.DataStateType;
import com.woodys.demo.entity.RefreshStatus;
import com.woodys.demo.entity.StateViewType;
import com.woodys.libsocket.sdk.ConnectionInfo;
import com.woodys.libsocket.sdk.OkSocket;
import com.woodys.libsocket.sdk.OkSocketOptions;
import com.woodys.libsocket.sdk.SocketActionAdapter;
import com.woodys.libsocket.sdk.bean.IPulseSendable;
import com.woodys.libsocket.sdk.bean.ISendable;
import com.woodys.libsocket.sdk.bean.OriginalData;
import com.woodys.libsocket.sdk.connection.IConnectionManager;
import com.woodys.libsocket.sdk.connection.NoneReconnect;
import com.woodys.libsocket.sdk.protocol.IHeaderProtocol;
import com.woodys.stateview.ViewHelperController;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SafeWebViewBridge.InjectedChromeClient;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static android.widget.Toast.LENGTH_SHORT;


public class WebActivity extends Activity {
    public static final int REFRESH_AUTH_STATUS_CODE = 0x0010;
    WebView webView = null;

    private IConnectionManager mManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);

        webView=(WebView) findViewById(R.id.web_view);
        WebSettings webSetting = webView.getSettings();
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
        webSetting.setAppCacheEnabled(true);
        webSetting.setAppCacheMaxSize(1024 * 1024 * 8);
        //启用地理定位
        webSetting.setGeolocationEnabled(true);
        String PICASSO_CACHE = "picasso-cache";
        webSetting.setGeolocationDatabasePath(PICASSO_CACHE);
        webSetting.setAppCacheEnabled(true);
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

        //String url = "http://192.168.28.30:8080/test/demo.html";
        String url = "http://192.168.28.30:8080/test/index.html";
        //String url ="http://192.168.28.30:7020/auth-page";
        //String url = "http://www.baidu.com/";
        webView.loadUrl(url);

        //webView.loadUrl("file:///android_asset/index.html");


        //初始化SocketClient，打开通道
        initSocketClient();


        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(webView.getContext(), OtherWebviewActivity.class);
                //用Bundle携带数据
                Bundle bundle = new Bundle();
                bundle.putString("turl", "https://m.xyqb.com/app-landing?registerFrom=217&channelId=1&token=c15623e8-7ca5-421c-9806-291328e0d898&appChannel=ceshi&appName=xinyongqianbao&authPage=auth-page&bindingPage=loan/card-binding");
                bundle.putString("type", "1");
                intent.putExtras(bundle);
                startActivityForResult(intent, Constant.RESULTZHIFUBAO);

            }
        });
    }

    public void initSocketClient() {
        ConnectionInfo connectionInfo = new ConnectionInfo("172.30.220.7", 59227);
        OkSocketOptions okSocketOptions = new OkSocketOptions.Builder(OkSocketOptions.getDefault())
                .setReconnectionManager(new NoneReconnect())
                .build();

        //设置自定义解析头
        OkSocketOptions.Builder okOptionsBuilder = new OkSocketOptions.Builder(okSocketOptions);
        okOptionsBuilder.setHeaderProtocol(new IHeaderProtocol() {
            @Override
            public int getHeaderLength() {
                //返回自定义的包头长度,框架会解析该长度的包头
                return 0;
            }

            @Override
            public int getBodyLength(byte[] header, ByteOrder byteOrder) {
                //从header(包头数据)中解析出包体的长度,byteOrder是你在参配中配置的字节序,可以使用ByteBuffer比较方便解析
                return 0;
            }
        });


        RxBus.INSTANCE.subscribe(this, DataStateType.class, new Function1<DataStateType, Unit>() {
            @Override
            public Unit invoke(DataStateType item) {
                if (null != item) {
                    //上传信息
                    SendMessageUtils.sendMessage(mManager,item.type,item.event,item.value,item.messageCallback);
                }
                return null;
            }
        });

        RxBus.INSTANCE.subscribe(this, RefreshStatus.class, new Function1<RefreshStatus, Unit>() {
            @Override
            public Unit invoke(RefreshStatus item) {
                if (null != item) refreshAuthStatus(item.type);
                return null;
            }
        });


        //将新的修改后的参配设置给连接管理器
        mManager = OkSocket.open(connectionInfo, okOptionsBuilder.build());
        mManager.registerReceiver(socketActionAdapter);

        //开启SocketClient通道
        if (!mManager.isConnect()) mManager.connect();
    }

    private SocketActionAdapter socketActionAdapter = new SocketActionAdapter() {

        @Override
        public void onSocketConnectionSuccess(Context context, ConnectionInfo info, String action) {
            //上传设备信息
            //SendMessageUtils.sendDeviceInfoMessage(mManager,WebActivity.this,"FINGERPRINT",null);
        }

        @Override
        public void onSocketDisconnection(Context context, ConnectionInfo info, String action, Exception e) {
            if (e != null) {
                Toast.makeText(context, "异常断开:" + e.getMessage(), LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "正常断开", LENGTH_SHORT).show();
            }
        }

        @Override
        public void onSocketConnectionFailed(Context context, ConnectionInfo info, String action, Exception e) {
            Toast.makeText(context, "连接失败" + e.getMessage(), LENGTH_SHORT).show();
        }

        @Override
        public void onSocketReadResponse(Context context, ConnectionInfo info, String action, OriginalData data) {
            super.onSocketReadResponse(context, info, action, data);
            String str = new String(data.getBodyBytes(), Charset.forName("utf-8"));
        }

    };

    /**
     * 网页爬虫授权完成后返回上级页面刷新状态
     */
    private void refreshAuthStatus(String type){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("event", "webViewRefreshAuthStatus");
            Map<String,String> stringMap = new HashMap<String,String>();
            stringMap.put("type",type);
            jsonObject.put("data",stringMap);
            webView.loadUrl("javascript:xyqbNativeEvent(" + jsonObject + ")");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mManager != null) {
            mManager.disconnect();
            mManager.unRegisterReceiver(socketActionAdapter);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(REFRESH_AUTH_STATUS_CODE==requestCode && resultCode==RESULT_OK ){
            //网页爬虫授权完成后返回上级页面刷新状态
            //webView.reload();
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
