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
package pazzled.game.utils.ads;

import pazzled.game.atomic.R;

import java.util.Timer;
import java.util.TimerTask;

import pazzled.game.utils.common.Log;

import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class AdsDisplay implements OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = AdsDisplay.class.getSimpleName();
	private AdView adView;
	private AdsDisplayInterface nterface;
	private Timer ad_timer;
	private int ad_show_time;
	private int ad_pause_time;
	private int default_pause_time;

	public int getdefault_pause_time() {
		return default_pause_time;
	}

	public int getAd_show_time() {
		return ad_show_time;
	}

	public void setAd_show_time(int ad_show_time) {
		this.ad_show_time = ad_show_time;
	}

	public int getAd_pause_time() {
		return ad_pause_time;
	}

	public void setAd_pause_time(int ad_pause_time) {
		this.ad_pause_time = ad_pause_time;
	}

	public void disableAds(AdsDisplayInterface nterface) {
		TimerTask pollTask = getPollTask();
		hide();

		setAd_pause_time(20 * 60 * 1000);
		if (ad_timer != null)
			ad_timer.cancel();
		ad_timer = new Timer();
		ad_timer.schedule(pollTask, ad_pause_time);
	}

	public void enableAds(AdsDisplayInterface nterface) {
		TimerTask pollTask = getPollTask();
		show();
		if (ad_timer != null)
			ad_timer.cancel();
		ad_timer = new Timer();
		ad_timer.schedule(pollTask, ad_show_time);
		setAd_pause_time(default_pause_time);
	}
	
	public void stopAds() {
//		TimerTask pollTask = getPollTask();
		hide();
		if (ad_timer != null)
			ad_timer.cancel();
//		ad_timer = new Timer();
//		ad_timer.schedule(pollTask, ad_show_time);
//		setAd_pause_time(default_pause_time);
	}

	public AdsDisplay(AdsDisplayInterface nterface) {
		default_pause_time = 20 * 1000;
		ad_show_time = default_pause_time;
		ad_pause_time = default_pause_time;
		this.nterface = nterface;
		adView = (AdView) nterface.findViewById(R.id.adView);
		hide();
		adView.bringToFront();
		adView.setOnClickListener(this);
//		showInterval();
	}

	public void showInterval() {
		TimerTask pollTask = getPollTask();
		ad_timer = new Timer();
		ad_timer.schedule(pollTask, ad_pause_time);
	}

	private TimerTask getPollTask() {
		TimerTask pollTask;
		final Handler handler = new Handler();
		pollTask = new TimerTask() {
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						toggle();
						Log.d("TIMER", "Timer set off");
					}
				});
			}
		};
		return pollTask;
	}

	private void toggle() {

		TimerTask pollTask = getPollTask();
		switch (adView.getVisibility()) {
		case View.GONE:
			show();
			ad_timer.cancel();
			//Log.d(TAG, "Going to show in " + ad_show_time + " seconds");
			ad_timer = new Timer();
			ad_timer.schedule(pollTask, ad_show_time);
			break;
		case View.VISIBLE:
			hide();
			ad_timer.cancel();
			//Log.d(TAG, "Going to pause in " + ad_pause_time + " seconds");
			ad_timer = new Timer();
			ad_timer.schedule(pollTask, ad_pause_time);
			break;
		default:
			break;
		}
	}

	public void hide() {

		adView.setVisibility(View.GONE);
	}

	public void show() {

		adView.setVisibility(View.VISIBLE);
		AdRequest adRequest = new AdRequest();
		adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
		adRequest.addTestDevice("42E7DA5AE3A68168212939D8B3708E98");

		adView.loadAd(adRequest);
		nterface.OnShow();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.adView:
			//Log.d(TAG, "Ad Clicked");
			break;
		default:
			break;
		}
	}
}
