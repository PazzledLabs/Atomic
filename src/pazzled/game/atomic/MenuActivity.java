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
package pazzled.game.atomic;

import pazzled.game.atomic.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import pazzled.game.play.GameActivity;
import pazzled.game.play.LevelActivity;
import pazzled.game.utils.common.Utils;
import pazzled.game.utils.social.SocialConnect;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusOneButton;
import com.google.android.gms.plus.PlusOneButton.OnPlusOneClickListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;

@TargetApi(16)
public class MenuActivity extends Activity implements OnClickListener,
		AnimationListener, ConnectionCallbacks, OnConnectionFailedListener,
		OnPlusOneClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = HomeActivity.class.getSimpleName();
	private int number_of_rows;
	private int clicked;
	ArrayList<TextView> menu_items;

	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

	private ProgressDialog mConnectionProgressDialog;
	private PlusClient mPlusClient;
	private PlusOneButton mPlusOneButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d(TAG, "On Create");

		setContentView(R.layout.menu);

		mPlusClient = new PlusClient.Builder(this, this, this).clearScopes()
				.build();

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		LinearLayout PlusOneButton = (LinearLayout) inflater.inflate(
				R.layout.google_plus,
				(ViewGroup) findViewById(R.layout.google_plus));
		LinearLayout like_bar = (LinearLayout) findViewById(R.id.like_bar);
		like_bar.addView(PlusOneButton);
		mPlusOneButton = (PlusOneButton) findViewById(R.id.plus_one_button);
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Signing in...");
	}

	@Override
	protected void onStart() {
		super.onStart();
		//Log.d(TAG, "On Start");
		// The activity is about to become visible.
		clicked = -1;

		TableLayout menu_actions = (TableLayout) findViewById(R.id.menu_actions);
		menu_actions.setVisibility(View.VISIBLE);
		number_of_rows = menu_actions.getChildCount();

		//Log.d(TAG, "Number of menu items " + number_of_rows);
		menu_items = new ArrayList<TextView>(number_of_rows);
		for (int i = 0; i < number_of_rows; i++) {
			String param_name = "id/menu_item" + (i + 1);
			int param_id = Utils.getIdentifier(this, param_name);
			TextView menu_item = (TextView) findViewById(param_id);
			menu_item.setOnClickListener(this);
			menu_item.setVisibility(View.VISIBLE);

			menu_items.add(menu_item);
			Animation menu = AnimationUtils
					.loadAnimation(this, R.anim.slide_in);
			menu.setStartOffset(150 * i);
			menu_item.startAnimation(menu);
		}
		SocialConnect.setRateAppBtn(this, this);

		mPlusClient.connect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Log.d(TAG, "On Resume");
		// The activity has become visible (it is now "resumed").
		String url = "https://market.android.com/details?id=" + getPackageName();
		mPlusOneButton.initialize(mPlusClient, url, 1);
	}

	@Override
	protected void onPause() {
		//Log.d(TAG, "On Pause");
		super.onPause();
		// Another activity is taking focus (this activity is about to be
		// "paused").
	}

	@Override
	protected void onStop() {
		//Log.d(TAG, "On Stop");
		super.onStop();
		// The activity is no longer visible (it is now "stopped")
		mPlusClient.disconnect();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//Log.d(TAG, "On Destroy");
		// The activity is about to be destroyed.
	}

	@Override
	public void onClick(View v) {
		int _id = -1;
		switch (v.getId()) {
		case R.id.menu_item1:
			_id = 0;
			break;
		case R.id.menu_item2:
			_id = 1;
			break;
		case R.id.menu_item3:
			_id = 2;
			break;
		case R.id.menu_item4:
			_id = 3;
			break;
		case R.id.menu_item5:
			_id = 4;
			break;
		case R.id.android_rate:
			SocialConnect.rateApp(v, this);
			break;
		default:
			break;
		}

		clicked = _id;
		if (_id >= 0) {
			int i, j;
			for (i = _id, j = 0; j < number_of_rows; i++, j++) {

				i = i % number_of_rows;
				TextView menu_item = menu_items.get(i);

				Animation menu = AnimationUtils.loadAnimation(this,
						R.anim.slide_out);
				menu.setStartOffset(50 * j);
				menu_item.startAnimation(menu);

				if (j == 0) {
					menu.setAnimationListener(this);
				}
			}
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {

		for (int i = 0; i < number_of_rows; i++) {
			menu_items.get(i).setVisibility(View.INVISIBLE);
		}

		//Log.d(TAG, "Animation End");

		switch (clicked) {
		case 0: {
			// Play
			final Intent game = new Intent("android.intent.action.MAIN");
			game.setComponent(new ComponentName(this, GameActivity.class));
			startActivity(game);
//			System.gc();
			// finish();
			break;
		}
		case 1: {
			// Levels
			final Intent game = new Intent("android.intent.action.MAIN");
			game.setComponent(new ComponentName(this, LevelActivity.class));
			startActivity(game);
//			System.gc();
			// finish();
			break;
		}
		case 2: {
			// LeaderBoard
			String url = getResources().getString(R.string.leaderboard_url);
			List<NameValuePair> params = new LinkedList<NameValuePair>();
			params.add( new BasicNameValuePair("user_id", SocialConnect.getName(this)));
			
			String paramString = URLEncodedUtils.format(params, "utf-8");
			
			final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url + "?" + paramString));
			startActivity(intent);
			System.gc();
			break;
		}
		case 3: {
			// Tutorial
			final Intent game = new Intent("android.intent.action.MAIN");
			game.setComponent(new ComponentName(this, TutorialActivity.class));
			startActivity(game);
//			System.gc();

			break;
		}
		case 4: {
			final Intent game = new Intent("android.intent.action.MAIN");
			game.setComponent(new ComponentName(this, AboutActivity.class));
			startActivity(game);
//			System.gc();
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		//Log.d(TAG, "Animation Repeat");
	}

	@Override
	public void onAnimationStart(Animation animation) {
		//Log.d(TAG, "Animation Start");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (mConnectionProgressDialog.isShowing()) {
			// The user clicked the sign-in button already. Start to resolve
			// connection errors. Wait until onConnected() to dismiss the
			// connection dialog.
			if (result.hasResolution()) {
				try {
					result.startResolutionForResult(this,
							REQUEST_CODE_RESOLVE_ERR);
				} catch (SendIntentException e) {
					mPlusClient.connect();
				}
			}
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		// We've resolved any connection errors.
		// String accountName = mPlusClient.getAccountName();
		// Toast.makeText(this, accountName + " is connected.",
		// Toast.LENGTH_LONG)
		// .show();
		mConnectionProgressDialog.dismiss();
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		//Log.d(TAG, "disconnected");

	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		if (requestCode == REQUEST_CODE_RESOLVE_ERR
				&& responseCode == RESULT_OK) {
			mPlusClient.connect();
		}
	}

	@Override
	public void onPlusOneClick(Intent arg0) {
		// TODO Auto-generated method stub

		//Log.d(TAG, "Plus one clicked");

	}
}
