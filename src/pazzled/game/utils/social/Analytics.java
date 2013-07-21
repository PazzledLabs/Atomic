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


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import pazzled.game.utils.KVStore;
import pazzled.game.utils.KVStoreListener;
import pazzled.game.utils.network.NetworkRequest;
import pazzled.game.utils.network.NetworkRequestListener;

import android.content.Context;

public class Analytics implements NetworkRequestListener {

	@SuppressWarnings("unused")
	private static final String TAG = Analytics.class.getSimpleName();
	private static KVStore KVStore = null;

	public static void Initialize(Context context, KVStoreListener listener) {
		KVStore = new KVStore(context, listener);
		KVStore.createDatabase();
	}

	public static void add(String key, String value) {
		KVStore.add(key, value);
	}

	public static String get(int id) {
		return KVStore.get(id);
	}

	public static void remove(int id) {
		KVStore.remove(id);
	}

	private static NameValuePair[] getAll() {
		return KVStore.getAll();
	}

	public Analytics() {

	}

	public void send(String url, Context context) {
		NameValuePair[] xtra = new NameValuePair[4];
		xtra[0] = new BasicNameValuePair("deviceId",
				SocialConnect.getDeviceId(context));
		xtra[1] = new BasicNameValuePair("userId",
				SocialConnect.getName(context));
		xtra[2] = new BasicNameValuePair("IpAddress",
				SocialConnect.getIpAddress(context));
		xtra[3] = new BasicNameValuePair("time", Long.toString(System
				.currentTimeMillis()));

		NetworkRequest request = new NetworkRequest();
		request.doPost(url, xtra, this, context);
	}

	@Override
	public void onComplete(Long result, NameValuePair[] post_data) {
		//Log.d(TAG, "analytics post complete");
		for(int i = 0; i < post_data.length; i++) {
			Analytics.remove(Integer.parseInt(post_data[i].getName()));
		}
	}

	public NameValuePair[] getParams() {
		return getAll();
	}
}
