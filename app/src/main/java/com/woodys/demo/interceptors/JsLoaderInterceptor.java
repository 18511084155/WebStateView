package com.woodys.demo.interceptors;

import android.content.Context;
import android.webkit.WebResourceResponse;

public class JsLoaderInterceptor extends BaseInterceptor {
    private boolean isInterceptRequestByJs = false;

    public JsLoaderInterceptor(Context context) {
        super(context);
    }

    @Override
    public boolean isInterceptRequestUrl(String url) {
        if (url.contains("https://buyertrade.taobao.com/trade/itemlist/list_bought_items.htm")) {
            isInterceptRequestByJs = true;
        }
        if ((isInterceptRequestByJs && url.contains(".js"))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public WebResourceResponse handle(String url) {
        return new WebResourceResponse(null, null, null);
    }
}
