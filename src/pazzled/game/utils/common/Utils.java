/*******************************************************************************
 * Copyright (c) 2013 venkat@pazzled.com.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     venkat@pazzled.com - Venkat
 ******************************************************************************/
package pazzled.game.utils.common;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pazzled.game.utils.HighScore;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.TypedValue;
import android.view.View;

public class Utils {

	public static enum HighScoreButton {
		INVALID, RESTART, NEXT
	};

	@SuppressWarnings("unused")
	private static final String TAG = HighScore.class.getSimpleName();

	public static int getIdentifier(Context c, String uri) {
		Resources res = c.getResources();
		return res.getIdentifier(uri, null, c.getPackageName());
	}

	public static String insertChar(String old, int index, char ch) {
		StringBuilder builderString = new StringBuilder(old);
		builderString.setCharAt(index, ch);
		return builderString.toString();
	}

	public static int max(int x, int y) {
		if (x > y)
			return x;
		return y;
	}

	public static int min(int x, int y) {
		if (x > y)
			return y;
		return x;
	}

	public static char[][] RotateMatrix(char[][] matrix, int n) {
		char[][] ret = new char[n][n];

		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < n; ++j) {
				ret[i][j] = matrix[n - j - 1][i];
			}
		}

		return ret;
	}

	public static boolean equals(char[][] matrix, Pattern[] regex) {
		boolean started = false;
		int mi = 0, i = 0;
		for (i = 0, mi = 0; mi < regex.length && i < matrix.length; i++) {
			// String row = matrix[i].toString();
			String row = new String(matrix[i]);
			Matcher m = regex[mi].matcher(row);
			if (m.find()) {
				started = true;
				mi++;
			} else if (started) {
				started = false;
				break;
			}
		}
		if (mi != regex.length)
			return false;

		return started;
	}


	public static void LogMemoryUsage(Context c) {
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) c
				.getSystemService(Context.ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		//long availableMegs = mi.availMem / 1048576L;
		//Log.d(TAG, "Available Mbs :" + availableMegs);

	}

	public static int getPixels(Context c, int dipValue) {
		Resources r = c.getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				dipValue, r.getDisplayMetrics());
		return px;
	}

	public static Bitmap getScreenShot(View v) {
		View rootView = v.getRootView().getRootView();
		int w = rootView.getWidth();
		int h = rootView.getHeight();
		Bitmap screenshot = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(screenshot);
		rootView.draw(canvas);
		return screenshot;

	}


}
