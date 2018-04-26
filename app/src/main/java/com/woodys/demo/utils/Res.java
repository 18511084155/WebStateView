package com.woodys.demo.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.File;

/**
 *
 * 
 */
public class Res {

	private static Resources appResource;
	private static Context appContext;
	private static String packageName;

	static {
		appContext = getContext();
	}

	public static Context getContext() {
		if (appContext == null) {
			try {
				Application application=(Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
				appContext = application.getApplicationContext();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return appContext;
	}


	private static String packageName() {
		if (packageName == null) {
			Context context = getContext();
			packageName = context.getPackageName();
		}
		return packageName;
	}

	public static Resources getResources() {
		if (null==appResource) {
			Context context = getContext();
			appResource = context.getResources();
		}
		return appResource;
	}

	private static int identifier(String name, String type) {
		Resources resources = getResources();
		return resources.getIdentifier(name, type, packageName());
	}

	public static int string(String name) {
		return identifier(name, "string");
	}

	public static int drawable(String name) {
		return identifier(name, "drawable");
	}

	public static int id(String name) {
		return identifier(name, "id");
	}

	public static int attr(String name) {
		return identifier(name, "attr");
	}

	public static int layout(String name) {
		return identifier(name, "layout");
	}

	public static int menu(String name) {
		return identifier(name, "menu");
	}

	public static int style(String name) {
		return identifier(name, "style");
	}

	public static int integer(String name) {
		return identifier(name, "integer");
	}

	public static int anim(String name) {
		return identifier(name, "anim");
	}

	public static int raw(String name) {
		return identifier(name, "raw");
	}

	public static int color(String name) {
		return identifier(name, "color");
	}

	public static int array(String name) {
		return identifier(name, "array");
	}

	public static int getItemId(String name) {
		return identifier(name, "id");
	}

	public static int getLayoutId(String name) {
		return identifier(name, "layout");
	}

	public static int getIdentifier(String name, String type) {
		return identifier(name, type);
	}

	public static int getDrawableId(String name) {
		return identifier(name, "drawable");
	}


	public static int getWidth() {
		return getDisplayMetrics().widthPixels;
	}

	public static int getHeight() {
		return getDisplayMetrics().heightPixels;
	}

	public static float getAspectRatioWidth(float ratio) {
		return getWidth()*ratio;
	}

	public static DisplayMetrics getDisplayMetrics() {
		return getContext().getResources().getDisplayMetrics();
	}


	public static SharedPreferences getSharedPreferences(String name) {
		Context context = getContext();
		return context.getSharedPreferences(name, Context.MODE_PRIVATE);
	}

	public static Drawable getDrawable(@DrawableRes int drawable) {
		Context context = getContext();
		return context.getResources().getDrawable(drawable);
	}

	public static String getString(int resId, Object... formatArgs) {
		Context context = getContext();
		return context.getString(resId, formatArgs);
	}

	public static int getColor(int id) {
		Context context = getContext();
		return context.getResources().getColor(id);
	}

	public static String[] getStringArray(int array) {
		Context context = getContext();
		return context.getResources().getStringArray(array);
	}

	public static float getDimension(int dimension) {
		Resources resources = getResources();
		return resources.getDimension(dimension);
	}

	public static int[] getIntArray(int id) {
		return getResources().getIntArray(id);
	}

	public static int dp2px(float value) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
	}


	public static int px2dp(float pxValue) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int sp2px(float pxValue) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, pxValue, getResources().getDisplayMetrics());
	}

	public static float px2sp(float px) {
		float scaledDensity = getResources().getDisplayMetrics().scaledDensity;
		return px / scaledDensity;
	}

	public static float getDimentionAttr(@AttrRes int attr) {
		int[] attribute = new int[]{attr};
		TypedArray array = getContext().obtainStyledAttributes(null, attribute);
		return array.getDimensionPixelSize(0, -1);
	}

	public static int getAttrColor(@AttrRes int res) {
		TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(res, typedValue, true);
		return typedValue.data;
	}

	public static File getCacheDir(){
		Context context = getContext();
		return context.getCacheDir();
	}


}
