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


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.google.gson.Gson;

public class NetworkRequest implements WifiStateListener, AsyncNetworkLisener {

	@SuppressWarnings("unused")
	private static final String TAG = NetworkRequest.class.getSimpleName();
	private WifiStateObserver wifiObserver = null;
	private NetworkRequestListener listener;
	private NameValuePair[] extraParams;
	private String url;

	public void doPost(String url, NameValuePair[] extraParams,
			NetworkRequestListener listener, Context context) {

		if(wifiObserver == null)
			wifiObserver = new WifiStateObserver(context, this);
		this.listener = listener;
		this.extraParams = extraParams;
		this.url = url;
 		if (wifiObserver.isWifiConnected()) {
			//Log.d(TAG, "wifi is connected");
			execute();
		} else {
			wifiObserver.register();
		}
	}

	private void execute() {
		AsyncNetwork session = new AsyncNetwork();
		session.setCallBackListener(this);
		session.setPostData(listener.getParams());
		Gson gson = new Gson();

		//Log.d(TAG, "posting content");
		NameValuePair[] post_request = new NameValuePair[3];
		post_request[0] = new BasicNameValuePair("url", url);

		post_request[1] = new BasicNameValuePair("data", gson.toJson(listener
				.getParams()));
		post_request[2] = new BasicNameValuePair("extra_data",
				gson.toJson(extraParams));

		session.execute(post_request);
	}

	@Override
	public void onWifiConnect() {
		//Log.d(TAG, "sending on wifi connection");
		execute();
//		wifiObserver.unregister();
	}

	@Override
	public void onWifiDisconnect() {
		//Log.d(TAG, "wifi disabled");

	}

	@Override
	public void onComplete(Long result, NameValuePair[] post_data) {
		listener.onComplete(result, post_data);
	}
}
