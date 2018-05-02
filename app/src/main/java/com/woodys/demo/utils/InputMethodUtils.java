package com.woodys.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class InputMethodUtils {

	/**
	 * 强制显示输入法键盘
	 *
	 * @param view   EditText
	 */
	public static void showKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager) view.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			view.requestFocus();
			imm.showSoftInput(view, 0);
		}
	}

	/**
	 * 强制隐藏输入法键盘
	 *
	 * @param view   EditText
	 */
	public static void hideKeyboard(View view){
		InputMethodManager imm = (InputMethodManager) view.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(),0);
		}
	}

	/**
	 * 隐藏输入法
	 */
	public static void hideKeyboard(Activity activity) {
		if (null != activity && activity.getCurrentFocus() != null) {
			((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 切换键盘的弹出和隐藏
	 *
	 * @param context
	 */
	public static void toggleSoftInput(Context context){
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.toggleSoftInput(0,0);
		}
	}
}
