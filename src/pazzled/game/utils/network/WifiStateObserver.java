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
package pazzled.game.utils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class WifiStateObserver {

	@SuppressWarnings("unused")
	private static final String TAG = WifiStateObserver.class.getSimpleName();

	private BroadcastReceiver mWifiStateChangedReceiver = null;
	private WifiStateListener listener;
	private Context context;

	public WifiStateObserver(Context context, WifiStateListener listener) {
		this.setListener(listener);
		this.context = context;
	}

	public void unregister() {
		context.unregisterReceiver(mWifiStateChangedReceiver);
		mWifiStateChangedReceiver = null;
	}

	public void register() {

		if (mWifiStateChangedReceiver != null)
			return;

		mWifiStateChangedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				int extraWifiState = intent.getIntExtra(
						WifiManager.EXTRA_WIFI_STATE,
						WifiManager.WIFI_STATE_UNKNOWN);

				switch (extraWifiState) {
				case WifiManager.WIFI_STATE_DISABLED:
				case WifiManager.WIFI_STATE_DISABLING:
					//Log.d(TAG, "wifi disabled");
					getListener().onWifiDisconnect();
					break;
				case WifiManager.WIFI_STATE_ENABLED:
					int trials = 0;
					while (!isWifiConnected() && trials <= 5) {
						trials++;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if (trials >= 6) {
						//Log.d(TAG, "failed 5 times, wifi is on though");
					} else {
						//Log.d(TAG, "wifi switched on!");
						getListener().onWifiConnect();
					}
					break;
				case WifiManager.WIFI_STATE_ENABLING:
					break;
				case WifiManager.WIFI_STATE_UNKNOWN:
					break;
				}
			}
		};
		context.registerReceiver(mWifiStateChangedReceiver, new IntentFilter(
				WifiManager.WIFI_STATE_CHANGED_ACTION));
	}

	private WifiStateListener getListener() {
		return listener;
	}

	private void setListener(WifiStateListener listener) {
		this.listener = listener;
	}

	public Boolean isWifiConnected() {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null) {
			boolean isConnected = activeNetwork.isConnectedOrConnecting();
			boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

			return isConnected && isWiFi;
		}
		return false;
	}

}
