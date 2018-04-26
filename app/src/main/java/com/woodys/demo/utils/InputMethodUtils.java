package com.woodys.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.os.ResultReceiver;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Method;

public class InputMethodUtils {
	private static Runnable mShowImeRunnable;

	/**
	 * 设置软键盘是否显示
	 * 
	 * @param visible
	 */
	public static void setImeVisibility(final View view, final boolean visible) {
		if (null == mShowImeRunnable) {
			mShowImeRunnable = new Runnable() {
				@Override
				public void run() {
					InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

					if (imm != null) {
						showSoftInputUnchecked(view, imm, 0);
					}
				}
			};
		}
		if (visible) {
			view.post(mShowImeRunnable);
		} else {
			view.removeCallbacks(mShowImeRunnable);
			InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}
	}

	/**
	 * 强制显示输入法
	 * 
	 * @param view
	 * @param imm
	 * @param flags
	 */
	private static void showSoftInputUnchecked(View view, InputMethodManager imm, int flags) {
		try {
			Method method = imm.getClass().getMethod("showSoftInputUnchecked", int.class, ResultReceiver.class);
			method.setAccessible(true);
			method.invoke(imm, flags, null);
		} catch (Exception e) {
			imm.showSoftInput(view, flags);
		}
	}

	/**
	 * 隐藏输入法
	 * 
	 * @param context
	 * @param view
	 */
	public static void hideSoftInput(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}

	/**
	 * 显示输入法
	 */
	public static void showSoftInput(Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	/**
	 * 隐藏输入法
	 */
	public static void hideSoftInput(Activity activity) {
		if (null != activity && activity.getCurrentFocus() != null) {
			((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
