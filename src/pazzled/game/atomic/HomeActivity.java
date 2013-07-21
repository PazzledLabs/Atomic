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

import pazzled.game.utils.common.Log;
import pazzled.game.atomic.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class HomeActivity extends Activity implements AnimationListener {

	@SuppressWarnings("unused")
	private static final String TAG = HomeActivity.class.getSimpleName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(ACTIVITY_SERVICE, "On Create");

		setContentView(R.layout.home);
		Animation home = AnimationUtils.loadAnimation(this, R.anim.home);
		TextView company = (TextView) findViewById(R.id.company_name);
		home.setAnimationListener(this);
		company.setAnimation(home);
		Log.d(ACTIVITY_SERVICE, "Displaying Logo Animation");

		// GridView gridview = (GridView) findViewById(R.id.level);
		Log.d(ACTIVITY_SERVICE, "On Create Complete");
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		//Log.d(TAG, "Animation End, Starting game");
		SharedPreferences prefs = this.getSharedPreferences(
				"pazzled.game.play", Context.MODE_PRIVATE);
		prefs.edit().putInt("pazzled.game.play.restart", 1).commit();
		
		final Intent menu = new Intent("android.intent.action.MAIN");
		menu.setComponent(new ComponentName(this, MenuActivity.class));
		startActivity(menu);
		finish();
		// Intent game = new Intent(HomeActivity.this, GameActivity.class);
		// startActivity(game);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(ACTIVITY_SERVICE, "On Start");
		// The activity is about to become visible.
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(ACTIVITY_SERVICE, "On Resume");
		// The activity has become visible (it is now "resumed").
	}

	@Override
	protected void onPause() {
		Log.d(ACTIVITY_SERVICE, "On Pause");
		super.onPause();
		// Another activity is taking focus (this activity is about to be
		// "paused").
	}

	@Override
	protected void onStop() {
		Log.d(ACTIVITY_SERVICE, "On Stop");
		super.onStop();
		// The activity is no longer visible (it is now "stopped")
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(ACTIVITY_SERVICE, "On Destroy");
		// The activity is about to be destroyed.
	}
}
