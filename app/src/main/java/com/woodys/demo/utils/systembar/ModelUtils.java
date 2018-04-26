package com.woodys.demo.utils.systembar;

import android.os.Build;

import java.io.IOException;

/**
 * Created by cz on 10/31/16.
 */

public class ModelUtils {
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    public static boolean isMIUI() {
        try {
            final BuildProperties prop = BuildProperties.newInstance();
            return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        } catch (final IOException e) {
            return false;
        }
    }

    public static boolean isFlyme() {
        boolean result=false;
        if("flyme".equals(Build.USER)||"Meizu".equals(Build.MANUFACTURER)){
            result=true;
        }
        return result;
    }
}
