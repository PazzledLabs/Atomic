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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import pazzled.game.utils.security.RSA;

import android.os.AsyncTask;

public class AsyncNetwork extends AsyncTask<NameValuePair, Integer, Long> {

	@SuppressWarnings("unused")
	private static final String TAG = AsyncNetwork.class.getSimpleName();
	private AsyncNetworkLisener listener = null;
	private NameValuePair[] post_data = null;

	@Override
	protected Long doInBackground(NameValuePair... post_data) {
		// TODO Auto-generated method stub
		doPost(post_data);
		return null;
	}

	protected void onPostExecute(Long result) {
		//Log.d(TAG, "Upload Complete " + result + " bytes");
		if (listener != null) {
			listener.onComplete(result, post_data);
			listener = null;
			post_data = null;
		}
	}

	private HttpResponse doPost(NameValuePair[] post_data) {
		HttpResponse response = null;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = null;

		if (post_data.length >= 1 && post_data[0].getName().equals("url")) {
			httppost = new HttpPost(post_data[0].getValue());
		}

		try {
			int length = post_data.length - 1;
			if (length <= 0 || httppost == null) {
				//Log.d(TAG, "post_data does not have urls");
			} else {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						length);
				for (int iter = 1; iter < post_data.length; iter++) {
					NameValuePair c = post_data[iter];
					NameValuePair n = new BasicNameValuePair(c.getName(), RSA.Encrypt(c.getValue()));
					nameValuePairs.add(n);
				}
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = httpclient.execute(httppost);
			}

		} catch (ClientProtocolException e) {
			//Log.d(TAG, "ClientProtocolException");
		} catch (IOException e) {
			//Log.d(TAG, "IOException");
		}
		return response;
	}

	public void setCallBackListener(AsyncNetworkLisener listener) {
		this.listener = listener;
	}

	public void setPostData(NameValuePair[] params) {
		this.post_data = params;
	}
}
