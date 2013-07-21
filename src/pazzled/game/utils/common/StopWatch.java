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

public class StopWatch {

	@SuppressWarnings("unused")
	private static final String TAG = StopWatch.class.getSimpleName();
	private long start_time;
	private long time_elapsed;
    private boolean isRunning;
    
	public StopWatch() {
		start_time = 0;
		time_elapsed = 0;
		isRunning = false;
	}
	
	public void Start() {
		if (isRunning) {
			//Log.d(TAG, "Started without stopping");
			return;
		}
		isRunning = true;
		start_time = System.currentTimeMillis();
	}

	public void Stop() {
		if(!isRunning) {
			//Log.d(TAG, "Watch stopped without starting. Arbit result");
		}
		isRunning = false;
		time_elapsed += System.currentTimeMillis() - start_time;
	}

	public long getTimeElapsedSeconds() {
		return time_elapsed / 1000;
	}
}
