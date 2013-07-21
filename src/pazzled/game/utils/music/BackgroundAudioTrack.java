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
package pazzled.game.utils.music;

import pazzled.game.atomic.R;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class BackgroundAudioTrack {
	@SuppressWarnings("unused")
	private static final String TAG = BackgroundAudioTrack.class.getSimpleName();
	private static SoundPool sounds;
	private static int select;
	private static MediaPlayer music;
	private static boolean sound = true;
	private static int num_loading = 0;

	public static boolean isSound() {
		return sound;
	}

	public static void setSound(boolean sound) {
		BackgroundAudioTrack.sound = sound;
	}

	public static void loadSound(Context context) {
		num_loading += 1;
		//Log.d(TAG, "num Loading " + num_loading);
		if(num_loading > 1) {
			return;
		}
		sounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		select = sounds.load(context, R.raw.background, 1);
		music = MediaPlayer.create(context, R.raw.background);
	}

	public static void playSelect() {
		if (!sound)
			return; // if sound is turned off no need to continue
		sounds.play(select, 1, 1, 1, 0, 1);
	}

	public static void playMusicLoop() {
		music.setLooping(true);
		playMusic();
	}

	public static final void playMusic() {
		if (!sound)
			return;
		if (!music.isPlaying()) {
			music.seekTo(0);
			music.start();
		}
	}

	public static final void pauseMusic() {
		if (!sound)
			return;
		if (music.isPlaying())
			music.pause();
	}

	public static final void release() {
		
		//Log.d(TAG, "num Loading " + num_loading);
		num_loading -= 1;
		if( num_loading != 0) {
			return;
		}
		if (!sound)
			return;
		pauseMusic();
		sounds.release();
		music.stop();
		music.release();
	}
}
