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

import pazzled.game.play.LevelActivity;
import pazzled.game.utils.Popup;
import pazzled.game.utils.PopupListener;
import pazzled.game.utils.social.SocialConnect;
import pazzled.game.atomic.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class TutorialActivity extends Activity implements PopupListener, OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = TutorialActivity.class.getSimpleName();
	private Popup dialog;

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

		setContentView(R.layout.tutorial);
		dialog = new Popup(
				this,
				this,
				1,
				"Atomic is a fun game built around molecular geometry. Hope you will enjoy as much as we do.",
				true);
	}
	
	@Override
	protected void onPause() {
		//Log.d(TAG, "On Pause");
		super.onPause();
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	@Override
	public void OnDismiss(int id) {
		// TODO Auto-generated method stub
		//Log.d(TAG, "Popup closed - " + id);
		switch (id) {
		case 1:
			dialog = new Popup(
					this,
					this,
					2,
					"Your Target: \n\n A molecule is disassembled into its atoms and scattered around the playing field. " +
					"You must reassemble the molecule in order to complete the current level and move up to the next one.",
					true);
			break;
		case 2:
			dialog = new Popup(
					this,
					this,
					3,
					"Game Play: \n\n To see how the molecule appears, click the atomic icon at bottom right.\n To play, " +
					"click on an atom. You will see arrows pointing in the directions where atom can move. To move the atom, " +
					"click on the desired arrow. When an atom starts moving, it will not stop until it hits another atom or a" + 
					" wall, so make sure you think before you do your next move. You can assemble your molecule wherever you " +
					"like on the game board, but some places are easier to access than others. When the molecule is assembled,"+ 
					" you can move to the next level.",
					true);
			break;
		case 3:
			dialog = new Popup(
					this,
					this,
					4,
					"Game Rules: \n\n"
							+ "1. Game pieces can only move in one direction at a time.\n"
							+ "2. Once an atom begins moving it will not stop until it meets either a wall or another piece.\n",
					true);
			break;
		case 4:
			dialog = new Popup(
					this,
					this,
					5,
					"Strategies and Tips: \n\n"
							+ "1. Always review the complete molecule using the reference screen before making any moves. \n"
							+ "2. Next, study the play field and plan your moves. Remember, once a piece is moved it may not" +
								" be possible to return it into the starting position.\n"
							+ "3. Think through your every move and try to visualize the trajectory piece will follow once a" +
								" directional arrow is clicked.\n"
							+ "4. To make game play better we have various options for undo/redo. Try using them.",
					true);
			break;
		case 5:
			dialog = new Popup(
					this,
					this,
					6,
					"Please rate us if you enjoy the game as much as we do. \n\n",
					true, 0);
			LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			View childLayout = inflater.inflate(R.layout.app_like,
					(ViewGroup) findViewById(R.layout.app_like));
			
			dialog.addView(childLayout);
			ImageView img = (ImageView) dialog.findViewById(R.id.android_rate);

//			PlusOneButton plus_one = (PlusOneButton) dialog.findViewById(R.id.plus_one_button);
//			dialog.removeView(plus_one);
			
			img.setOnClickListener(this);
			dialog.show();
			break;
		default:
			final Intent game = new Intent("android.intent.action.MAIN");
			game.setComponent(new ComponentName(this, LevelActivity.class));
			startActivity(game);
//			System.gc();
			finish();
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.android_rate:
			SocialConnect.rateApp(v, this);
			break;

		default:
			break;
		}
	}

}
