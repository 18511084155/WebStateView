package com.woodys.demo.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.List;

public class PackageUtils {

    private static String packageName;

    static {
        packageName = getPackageName();
    }


    private static Uri marketUri(String pkg) {
        return Uri.parse("market://details?id=" + pkg);
    }

    /**
     * 打开指定resolve的Intent
     *
     * @param info
     * @param context
     */
    private static void startIntentByResolve(final ResolveInfo info, final Context context, final Uri uri) {
        RunUtils.run(new Runnable() {
            @Override
            public void run() {
                ActivityInfo activity = info.activityInfo;
                ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                        activity.name);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                i.setComponent(name);
                i.setData(uri);
                context.startActivity(i);
            }
        });
    }


    public static String getPackageName() {
        if (packageName == null) {
            Context context = Res.getContext();
            if (null != context) {
                packageName = context.getPackageName();
            } else {
                try {
                    final Class<?> activityThreadClass = PackageUtils.class.getClassLoader().loadClass("android.app.ActivityThread");
                    final Method currentPackageName = activityThreadClass.getDeclaredMethod("currentPackageName");
                    packageName = (String) currentPackageName.invoke(null);
                } catch (final Exception e) {
                    RunUtils.e(e);
                }
            }
        }
        return packageName;
    }

    public static ApplicationInfo getAppInfo() {
        ApplicationInfo appInfo = null;
        try {
            Context appContext = Res.getContext();
            appInfo = appContext.getPackageManager().getApplicationInfo(appContext.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            RunUtils.e(e);
        }
        return appInfo;
    }

    /**
     * 获得string mataData数据
     *
     * @param key
     * @return
     */
    public static String getStringMataData(String key) {
        ApplicationInfo appInfo = getAppInfo();
        String value = null;
        if (null != appInfo) {
            value = appInfo.metaData.getString(key);
        }
        return value;
    }

    /**
     * 获得boolean mataData数据
     *
     * @param key
     * @return
     */
    public static boolean getBooleanMataData(String key) {
        ApplicationInfo appInfo = getAppInfo();
        boolean value = false;
        if (null != appInfo) {
            try {
                value = appInfo.metaData.getBoolean(key);
            } catch (Exception e) {
                RunUtils.e(e);
            }
        }
        return value;
    }

    /**
     * 获得软件版本
     *
     * @return
     */
    public static String getAppVersion() {
        String appVersion = "";
        try {
            Context context = Res.getContext();
            appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            RunUtils.e(e);
        }
        return appVersion;
    }

    /**
     * 获得软件版本
     *
     * @return
     */
    public static int getAppVersionCode() {
        int appVersionCode = -1;
        try {
            Context context = Res.getContext();
            appVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            RunUtils.e(e);
        }
        return appVersionCode;
    }

    /**
     * 获得软件名称
     *
     * @return
     */
    public static String getApplicationName() {
        String appName = null;
        try {
            Context context = Res.getContext();
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            CharSequence label = packageManager.getApplicationLabel(applicationInfo);
            if (!TextUtils.isEmpty(label)) {
                appName = label.toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }


    /**
     * 检查某个应用是否安装
     *
     * @param packageName
     */
    public static boolean appIsInstall(String packageName) {
        boolean install = false;
        Context context = Res.getContext();
        if (!TextUtils.isEmpty(packageName) && null != context) {
            try {
                install = (null != context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES));
            } catch (PackageManager.NameNotFoundException e) {
                install = false;
            }
        }
        return install;
    }

    /**
     * 获得启动activity对象
     *
     * @return
     */
    public static String getLancherActivity() {
        Context context = Res.getContext();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(getPackageName());
        ComponentName component = launchIntent.getComponent();
        return component.getClassName();
    }

    /**
     * 启用设置界面
     *
     * @param context
     */
    public static void startSetting(Context context) {
        if (null != context) {
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                context.startActivity(intent);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 判断是否主进程
     *
     * @return
     */
    public static boolean isMainProcess(Context context) {
        boolean result = false;
        String mainProcessName = context.getPackageName();
        if (mainProcessName.equals(getProcessName(context, android.os.Process.myPid()))) {
            result = true;
        }
        return result;
    }

    public static String getProcessName(Context cxt, int pid) {
        String result = null;
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (!runningApps.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    result = procInfo.processName;
                    break;
                }
            }
        }
        return result;
    }
}
