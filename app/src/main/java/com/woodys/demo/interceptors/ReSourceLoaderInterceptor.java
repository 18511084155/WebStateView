package com.woodys.demo.interceptors;

import android.content.Context;
import android.webkit.WebResourceResponse;

public class ReSourceLoaderInterceptor extends BaseInterceptor {
    private boolean isInterceptRequestByResource = false;
    private String webReturnUrl = null;

    public ReSourceLoaderInterceptor(Context context, String webReturnUrl) {
        super(context);
        this.webReturnUrl = webReturnUrl;
    }

    @Override
    public boolean isInterceptRequestUrl(String url) {
        if (null != webReturnUrl && webReturnUrl.equals(url)) {
            isInterceptRequestByResource = true;
        }
        if (isInterceptRequestByResource && (url.contains(".css") || url.contains(".png") || url.contains(".gif") || url.contains(".ico"))) {
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
