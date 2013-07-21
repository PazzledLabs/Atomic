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
import android.app.Activity;
import android.os.Bundle;

public class AboutActivity extends Activity{

	@SuppressWarnings("unused")
	private static final String TAG = AboutActivity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d(TAG, "On Create");

	}

	@Override
	protected void onStart() {
		super.onStart();
		//Log.d(TAG, "On Start");
		// The activity is about to become visible.

		setContentView(R.layout.about);
	}
}
