package com.woodys.demo.interceptors;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;

public abstract class BaseInterceptor {
    protected Context mContext;

    public BaseInterceptor(Context context) {
        this.mContext = context;
    }

    public boolean canHandle(String url) {
        if (!TextUtils.isEmpty(url) && Uri.parse(url).getScheme() != null && isInterceptRequestUrl(url)) {
            String scheme = Uri.parse(url).getScheme().trim();
            if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) {
                try {
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    abstract boolean isInterceptRequestUrl(String url);
    public abstract WebResourceResponse handle(String url);
}
