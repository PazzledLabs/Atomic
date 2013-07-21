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
package pazzled.game.utils.social;

import pazzled.game.atomic.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import pazzled.game.utils.Popup;
import pazzled.game.utils.common.Utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SocialConnect {
	@SuppressWarnings("unused")
	private static final String TAG = SocialConnect.class.getSimpleName();

	public static List<String> getUsername(Context context) {
		List<String> possibleEmails = new LinkedList<String>();
		try {
			AccountManager manager = AccountManager.get(context);
			Account[] accounts = manager.getAccountsByType("com.google");

			for (Account account : accounts) {
				if (account.name != null)
					possibleEmails.add(account.name);
			}
		} catch (SecurityException e) {
			//Log.d(TAG, "Unable to get Username");
			possibleEmails.add("Ashwin");
		}
		return possibleEmails;
	}

	public static String getName(Context c) {
		SharedPreferences prefs = c.getSharedPreferences("pazzled.game.utils",
				Context.MODE_PRIVATE);
		String name = prefs.getString("pazzled.game.utils.name", "");
		String empty = new String("");

		if (name.equals(empty)) {
			Pattern emailPattern = Patterns.EMAIL_ADDRESS;

			Account[] accounts = AccountManager.get(c).getAccountsByType(
					"com.google");
			if (accounts.length == 0) {
				accounts = AccountManager.get(c).getAccounts();
			}
			for (Account account : accounts) {
				if (emailPattern.matcher(account.name).matches()) {
					name = account.name;
					break;
				}
			}
			prefs.edit().putString("pazzled.game.utils.email", name).commit();
			String[] parts = name.split("@");
			if (parts.length > 0 && parts[0] != null)
				name = parts[0];
			else
				name = "";
		}
		return name;

	}

	public static void setName(Context context, String name) {
		SharedPreferences prefs = context.getSharedPreferences(
				"pazzled.game.utils", Context.MODE_PRIVATE);
		prefs.edit().putString("pazzled.game.utils.name", name).commit();
	}

	public static String getDeviceId(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return wm.getConnectionInfo().getMacAddress();
	}

	public static String getIpAddress(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		String ip = intToIp(ipAddress);

		return ip;
	}

	public static String intToIp(int i) {

		return ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
				+ ((i >> 8) & 0xFF) + "." + (i & 0xFF);
	}

	public static void onClickApp(Context c, View v, String pkg, View level_view) {
		Intent waIntent = new Intent(Intent.ACTION_SEND);
		waIntent.setType("image/png");
		File png = null;
		File dir = new File(Environment.getExternalStorageDirectory(), "atomic");
		dir.mkdirs();
		png = new File(dir.toString(), "atomic.png");

		View rootView = v.getRootView().getRootView();
		int w = rootView.getWidth();
		int h = rootView.getHeight();
		Bitmap screenshot = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(screenshot);
		level_view.draw(canvas);
		rootView.draw(canvas);
		// screenshot = level_capture;

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(png);
			screenshot.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (waIntent != null) {
				Uri screencastUri = Uri.parse("file://" + png.getPath());
				waIntent.putExtra(Intent.EXTRA_STREAM, screencastUri);
				c.startActivity(Intent.createChooser(waIntent, "Share using"));
			}
		}
	}

	public static void initSocialIcons(Dialog dialog, Context c,
			OnClickListener l) {
		LinearLayout social_layout = (LinearLayout) dialog
				.findViewById(R.id.social_layout);

		LinearLayout layout = (LinearLayout) dialog
				.findViewById(R.id.social_bar);

		ImageView share_icon = new ImageView(c);
		share_icon.setImageResource(android.R.drawable.ic_menu_share);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				Utils.getPixels(c, 35), Utils.getPixels(c, 35));
		layoutParams.setMargins(0, 0, Utils.getPixels(c, 20), 0);
		share_icon.setLayoutParams(layoutParams);
		share_icon.setOnClickListener(l);
		share_icon.setId(R.id.share);
		share_icon.setBackgroundResource(R.drawable.border);
		layout.addView(share_icon);
		layout.setOnClickListener(l);
		social_layout.setOnClickListener(l);
		//Log.d(TAG, "setting share icon");
	}

	public static void setRateAppBtn(OnClickListener c, Activity a) {
		ImageView img = (ImageView) a.findViewById(R.id.android_rate);
		img.setOnClickListener(c);
	}

	public static void rateApp(View v, Activity a) {
		final Uri uri = Uri.parse("market://details?id="
				+ a.getApplicationContext().getPackageName());
		final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

		if (a.getPackageManager().queryIntentActivities(rateAppIntent, 0)
				.size() > 0) {
			a.startActivity(rateAppIntent);
		} else {
			new Popup(a, null, 0,
					"Sorry we cannot find market application on your phone. Rate us from browser.");
		}
	}

	public static int getCountryId() {
		return 0;
	}
}
