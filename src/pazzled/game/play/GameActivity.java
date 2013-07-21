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
package pazzled.game.play;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import pazzled.game.atomic.R;
import pazzled.game.utils.GridCellAnimation;
import pazzled.game.utils.GridCellAnimation.Direction;
import pazzled.game.utils.HighScore;
import pazzled.game.utils.HighScoreListener;
import pazzled.game.utils.ImageAdapter;
import pazzled.game.utils.ImageAdapterInterface;
import pazzled.game.utils.KVStoreListener;
import pazzled.game.utils.Popup;
import pazzled.game.utils.PopupListener;
import pazzled.game.utils.ads.AdsDisplay;
import pazzled.game.utils.ads.AdsDisplayInterface;
import pazzled.game.utils.common.Log;
import pazzled.game.utils.common.StopWatch;
import pazzled.game.utils.common.Utils;
import pazzled.game.utils.common.Utils.HighScoreButton;
import pazzled.game.utils.music.BackgroundAudioTrack;
import pazzled.game.utils.security.RSA;
import pazzled.game.utils.social.Analytics;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class GameActivity extends Activity implements OnItemClickListener,
		AnimationListener, OnClickListener, OnTouchListener,
		ImageAdapterInterface, HighScoreListener, AdsDisplayInterface,
		PopupListener, KVStoreListener {
	private Dialog molecule_display;
	@SuppressWarnings("unused")
	private static final String TAG = GameActivity.class.getSimpleName();
	private ImageAdapter mAdapter;
	private HashMap<Point, Object> atom_location;
	private List<Point> historyFrom;
	private List<Point> historyTo;
	private int currentItemInHistory;
	int level;

	private String[] level_play;
	private Pattern[] molecule_regex;
	private int molecule_max_size;

	private String wall;
	private String empty_cells;
	private String action_cells;
	private String atom_cells;

	private int score;
	private boolean completed;
	private Point focusedPoint;
	private Point AnimationEndPoint;
	private int numActionsHappening;
	private StopWatch clock;
	private AdsDisplay ad;
	private Analytics analytics;
	Popup dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Log.d(TAG, "Game Initializing");
		completed = false;
		numActionsHappening = 0;
		currentItemInHistory = 0;
		score = -1;
		AnimationEndPoint = null;
		focusedPoint = null;
		clock = new StopWatch();
		setContentView(R.layout.game);

		SharedPreferences prefs = this.getSharedPreferences("pazzled.game.play",
				Context.MODE_PRIVATE);
		level = prefs.getInt("pazzled.game.play.level", 0);
		int restart = prefs.getInt("pazzled.game.play.restart", 0);
		if (restart == 1) {
			prefs.edit().putInt("pazzled.game.play.restart", 0).commit();
			int max_completed_manually = prefs.getInt(
					"pazzled.game.play.max_completed_manually", 1);
			if (level > max_completed_manually + 1)
				level = Utils.min(max_completed_manually, level);
		}

		if (level == 0)
			resetCheckPoint();
		// level = 0;
		//Log.d(TAG, "Loading level " + level);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int value = Utils.max(metrics.heightPixels, metrics.widthPixels);
		//Log.d(TAG, "value - " + value);
		LinearLayout game_layout = (LinearLayout) findViewById(R.id.game_layout);
		TextView horizontal_space = new TextView(this);
		horizontal_space.setVisibility(View.INVISIBLE);
		horizontal_space.setLayoutParams(new ViewGroup.LayoutParams(value,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		game_layout.addView(horizontal_space, 0);

		int numColumns = getResources().getInteger(R.integer.numColumns);
		int numRows = getResources().getInteger(R.integer.numRows);
		GridView gridview = (GridView) findViewById(R.id.level);
		gridview.setAdapter(new ImageAdapter(this, this, numColumns, numRows));
		gridview.setOnItemClickListener(this);

		mAdapter = (ImageAdapter) gridview.getAdapter();
		GridCellAnimation.init(mAdapter.getCellWidth(),
				mAdapter.getCellHeight());
		increaseScoreDisplay();

		wall = getResources().getString(R.string.wall_cells);
		action_cells = getResources().getString(R.string.action_cells);
		atom_cells = getResources().getString(R.string.atom_cells);
		empty_cells = getResources().getString(R.string.empty_cells);

		String param_name = "array/level_" + level;
		int param_id = Utils.getIdentifier(this, param_name);
		level_play = getResources().getStringArray(param_id);
		loadFromCheckPoint();

		param_name = "integer/level_" + level + "_molecule_max_size";
		param_id = Utils.getIdentifier(this, param_name);
		molecule_max_size = getResources().getInteger(param_id);

		param_name = "array/level_" + level + "_molecule_regex";
		param_id = Utils.getIdentifier(this, param_name);
		String[] regex = getResources().getStringArray(param_id);

		param_name = "string/level_" + level + "_molecule_name";
		param_id = Utils.getIdentifier(this, param_name);
		String molecule_name = getResources().getString(param_id);

		TextView display_name = (TextView) findViewById(R.id.molecule_name);
		display_name.setText(molecule_name);
		display_name.setOnClickListener(this);

		ImageButton undo = (ImageButton) findViewById(R.id.undo);
		ImageButton redo = (ImageButton) findViewById(R.id.redo);
		ImageButton settings = (ImageButton) findViewById(R.id.m_settings);
		ImageView show_structure = (ImageView) findViewById(R.id.show_structure);

		undo.bringToFront();
		redo.bringToFront();
		settings.bringToFront();
		show_structure.bringToFront();
		display_name.bringToFront();

		undo.setOnClickListener(this);
		redo.setOnClickListener(this);
		settings.setOnClickListener(this);
		show_structure.setOnClickListener(this);

		molecule_regex = new Pattern[regex.length];
		for (int i = 0; i < regex.length; i++) {
			molecule_regex[i] = Pattern.compile(regex[i]);
		}

		Point first_cell = null;
		historyFrom = new LinkedList<Point>();
		historyTo = new LinkedList<Point>();
		atom_location = new HashMap<Point, Object>();
		int min_x = mAdapter.getNumRows();
		int min_y = mAdapter.getNumColumns();
		for (int i = 0; i < level_play.length; i++) {
			for (int j = 0; j < level_play[0].length(); j++) {
				Point pt = new Point(i, j);
				if (isWall(pt)) {
					min_x = Utils.min(min_x, i);
					min_y = Utils.min(min_y, j);
				}
			}
		}
		first_cell = new Point(min_x, min_y);

		displaceLevelView(first_cell.x, first_cell.y);
		mAdapter.notifyDataSetChanged();

		ad = null;

		if (level > 0) {
			ad = new AdsDisplay(this);
			displayMoleculeStructure();
			dialog = null;
		} else {
			dialog = new Popup(this, this, 1, new String(
					"Hi! Welcome to the world of puzzles."));
		}

		BackgroundAudioTrack.loadSound(this);
		BackgroundAudioTrack.playMusicLoop();

		RSA.InitializeKeyPair(this);
		analytics = new Analytics();
		Analytics.Initialize(this, this);

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//Log.d(TAG, "ON Touch");
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			return true;
		}
		return false;
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(ACTIVITY_SERVICE, "On Start");

		resetCheckPoint();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.game_menu, menu);

		if (level == 1 || level == 0) {
			MenuItem previous = menu.getItem(1);
			previous.setEnabled(false);
		}

		MenuItem volume_control = menu.getItem(3);
		if (BackgroundAudioTrack.isSound()) {
			volume_control.setTitle(R.string.mute);
		} else {
			volume_control.setTitle(R.string.sound_on);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection

		switch (item.getItemId()) {
		case R.id.next_level:
			SwitchLevel(level + 1, true);
			return true;
		case R.id.previous_level:
			SwitchLevel(level - 1, false);
			return true;
		case R.id.highscore:
			HighScore high_score = new HighScore(this, this, level);
			if (ad != null) {
				ad.stopAds();
			}
			high_score.showDialog(getWindow().getDecorView());
			return true;
		case R.id.hide_ad:
			if (ad != null) {
				if (ad.getAd_pause_time() == ad.getdefault_pause_time()) {
					ad.disableAds(this);
					item.setTitle(R.string.show_ad);
				} else {
					ad.enableAds(this);
					item.setTitle(R.string.hide_ad);
				}
			}
			return true;
		case R.id.restart_level:
			SwitchLevel(level, true);
			return true;
		case R.id.mute:
			if (BackgroundAudioTrack.isSound()) {
				BackgroundAudioTrack.pauseMusic();
				BackgroundAudioTrack.setSound(false);
				item.setTitle(R.string.sound_on);
			} else {
				BackgroundAudioTrack.setSound(true);
				BackgroundAudioTrack.playMusicLoop();
				item.setTitle(R.string.mute);
			}

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void displayMoleculeStructure() {
		if (ad != null)
			ad.stopAds();

		molecule_display = new Dialog(this,
				android.R.style.Theme_Translucent_NoTitleBar_Fullscreen) {
			public boolean onTouchEvent(MotionEvent event) {
				if (this.isShowing()) {
					//Log.d(TAG, "Touch outside the dialog");
					// this.dismiss();
					// clock.Start();

					// if (level == 0) {
					// guideTouch(0);
					// }
				}
				return false;
			}
		};
		molecule_display.setContentView(R.layout.moleculepopup);

		TableLayout molecule_structure = (TableLayout) molecule_display
				.findViewById(R.id.molecule_structure);
		String param_name = "array/level_" + GameActivity.this.level
				+ "_molecule";
		int param_id = Utils.getIdentifier(this, param_name);
		String[] molecule = getResources().getStringArray(param_id);

		for (int i = 0; i < molecule.length; i++) {
			TableRow row = new TableRow(this);
			for (int j = 0; j < molecule[i].length(); j++) {
				String uri = "drawable/" + molecule[i].charAt(j);
				int res_id = Utils.getIdentifier(this, uri);
				ImageView atom = new ImageView(this);
				atom.setLayoutParams(new LayoutParams(mAdapter.getCellWidth(),
						mAdapter.getCellHeight()));
				atom.setScaleType(ImageView.ScaleType.CENTER_CROP);
				atom.setPadding(1, 1, 1, 1);
				atom.setImageResource(res_id);
				row.addView(atom);
			}
			molecule_structure.addView(row);
		}

		LinearLayout level_target = (LinearLayout) molecule_display
				.findViewById(R.id.level_target);
		level_target.setOnClickListener(this);
		ImageView next = (ImageView) molecule_display
				.findViewById(R.id.next_popup);
		next.setOnClickListener(this);

		LinearLayout display = (LinearLayout) molecule_display
				.findViewById(R.id.molecule_popup);
		// molecule_display.setOn
		display.setOnClickListener(this);
		molecule_structure.setOnClickListener(this);
		molecule_display.show();
	}

	@Override
	protected void onResume() {

		// The activity has become visible (it is now "resumed").

		super.onResume();

		if (ad != null && clock.getTimeElapsedSeconds() > 5)
			ad.enableAds(this);
		clock.Start();
		Log.d(ACTIVITY_SERVICE, "On Resume " + level);
		BackgroundAudioTrack.playMusic();
	}

	@Override
	protected void onPause() {

		// Another activity is taking focus (this activity is about to be
		// "paused").

		Log.d(ACTIVITY_SERVICE, "On Pause " + level);
		super.onPause();
		if (ad != null)
			ad.disableAds(this);
		clock.Stop();
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
		BackgroundAudioTrack.pauseMusic();
		System.gc();
	}

	@Override
	protected void onStop() {

		// The activity is no longer visible (it is now "stopped")

		Log.d(ACTIVITY_SERVICE, "On Stop " + level);
		super.onStop();
		if (ad != null)
			ad.disableAds(this);
		clock.Stop();
		// BackgroundAudioTrack.pauseMusic();
		if(!completed)
			checkPointLevel();
		System.gc();
	}

	@Override
	protected void onDestroy() {

		// The activity is about to be destroyed.

		super.onDestroy();
		cleanup();
		if (ad != null)
			ad.disableAds(this);
		BackgroundAudioTrack.release();
		System.gc();
		Log.d(ACTIVITY_SERVICE, "On Destroy " + level);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		//Log.d(TAG, "Clicked item args " + position + " " + arg3);
		Point cell = mAdapter.getGridCoord(position);
		if (isAtom(cell)) {
			setFocus(cell);

		} else if (isAction(cell)) {
			char action = CharAt(cell);
			removeFocus(focusedPoint);
			Direction d = getDirection(action);
			//Log.d(TAG, "moving to direction " + d);
			DoAction(focusedPoint, null, d, true);

		} else if (focusedPoint != null) {
			Direction d = getApproximateDirection(cell);
			if (d != Direction.MOVE_INVALID) {
				//Log.d(TAG, "Appx direction " + d);
				if (checkMove(focusedPoint, d) != null) {
					removeFocus(focusedPoint);
					DoAction(focusedPoint, null, d, true);
				} else {
					//Log.d(TAG, "move not possible");
				}
			}
		}
	}

	private Point checkMove(Point current, Direction d) {
		Point next = null;
		for (int i = 1; i < Utils.max(mAdapter.getNumColumns(),
				mAdapter.getNumRows()); i++) {
			Point pt = getithCell(current, d, i);
			if (pt != null && isEmpty(pt)) {
				next = pt;
			} else
				break;
		}
		return next;
	}

	private void increaseScoreDisplay() {
		score += 1;
		String score_display = getResources().getString(
				R.string.score_display_string);
		TextView score_view = (TextView) findViewById(R.id.score_count);
		score_view.setText(score_display + " " + score);
		return;
	}

	private void decreaseScoreDisplay() {
		score -= 1;
		String score_display = getResources().getString(
				R.string.score_display_string);
		TextView score_view = (TextView) findViewById(R.id.score_count);
		score_view.setText(score_display + " " + score);
		return;
	}

	private boolean isValid(Point cell) {
		if (cell.x > 0 && cell.x < level_play.length) {
			if (cell.y > 0 && cell.y < level_play[0].length()) {
				return true;
			}
		}
		return false;
	}

	private char CharAt(Point cell) {
		return level_play[cell.x].charAt(cell.y);
	}

	private void setFocus(Point cell) {
		removeFocus(focusedPoint);
		AddFocus(cell);
	}

	private void AddFocus(Point cell) {
		//Log.d(TAG, "Setting Focus on row, column, position " + cell.x + ", "+ cell.y);
		addAction(cell);
		mAdapter.notifyDataSetChanged();
		focusedPoint = cell;
	}

	private Point getithCell(Point cell, Direction d, int i) {
		Point next = null;
		int row = -1, column = -1;
		switch (d) {
		case MOVE_LEFT: {
			column = cell.y - i;
			row = cell.x;
			if (column < 0) {
				column = -1;
			}
			break;
		}
		case MOVE_RIGHT: {
			column = cell.y + i;
			row = cell.x;
			if (column > mAdapter.getNumColumns()) {
				column = -1;
			}
			break;
		}
		case MOVE_UP: {
			row = cell.x - i;
			column = cell.y;
			if (row < 0) {
				row = -1;
			}
			break;
		}
		case MOVE_DOWN: {
			row = cell.x + i;
			column = cell.y;
			if (row > mAdapter.getNumRows()) {
				row = -1;
			}
			break;
		}
		default:
			break;
		}

		if (row != -1 && column != -1) {
			next = new Point(row, column);
		}
		return next;
	}

	private void addAction(Point cell) {
		Point next = getithCell(cell, Direction.MOVE_LEFT, 1);
		if (next != null) {
			if (isEmpty(next)) {
				level_play[next.x] = Utils.insertChar(level_play[next.x],
						next.y, 'e');
				getViewAt(next).invalidate();
			}
		}
		next = getithCell(cell, Direction.MOVE_RIGHT, 1);
		if (next != null) {
			if (isEmpty(next)) {
				level_play[next.x] = Utils.insertChar(level_play[next.x],
						next.y, 'r');
				getViewAt(next).invalidate();
			}
		}
		next = getithCell(cell, Direction.MOVE_UP, 1);
		if (next != null) {
			if (isEmpty(next)) {
				level_play[next.x] = Utils.insertChar(level_play[next.x],
						next.y, 'u');
				getViewAt(next).invalidate();
			}
		}
		next = getithCell(cell, Direction.MOVE_DOWN, 1);
		if (next != null) {
			if (isEmpty(next)) {
				level_play[next.x] = Utils.insertChar(level_play[next.x],
						next.y, 'd');
				getViewAt(next).invalidate();
			}
		}
	}

	private void removeFocus(Point cell) {
		if (cell == null)
			return;

		//Log.d(TAG, "Remove Focus from " + cell.x + " " + cell.y);
		removeActions(cell);
		mAdapter.notifyDataSetChanged();
	}

	private void removeActions(Point cell) {
		Point next = getithCell(cell, Direction.MOVE_LEFT, 1);
		if (next != null) {
			if (isAction(next)) {
				level_play[next.x] = Utils.insertChar(level_play[next.x],
						next.y, empty_cells.charAt(0));
				getViewAt(next).invalidate();
			}
		}
		next = getithCell(cell, Direction.MOVE_RIGHT, 1);
		if (next != null) {
			if (isAction(next)) {
				level_play[next.x] = Utils.insertChar(level_play[next.x],
						next.y, empty_cells.charAt(0));
				getViewAt(next).invalidate();
			}
		}
		next = getithCell(cell, Direction.MOVE_UP, 1);
		if (next != null) {
			if (isAction(next)) {
				level_play[next.x] = Utils.insertChar(level_play[next.x],
						next.y, empty_cells.charAt(0));
				getViewAt(next).invalidate();
			}
		}
		next = getithCell(cell, Direction.MOVE_DOWN, 1);
		if (next != null) {
			if (isAction(next)) {
				level_play[next.x] = Utils.insertChar(level_play[next.x],
						next.y, empty_cells.charAt(0));
				getViewAt(next).invalidate();
			}
		}
	}

	public View getViewAt(Point cell) {
		GridView gridview = (GridView) findViewById(R.id.level);
		return gridview.getChildAt(mAdapter.getPosition(cell));
	}

	@Override
	public void onAnimationEnd(Animation animation) {
//		Utils.LogMemoryUsage(this);

		if (numActionsHappening == 1) {
			numActionsHappening -= 1;
			changeCell(focusedPoint, AnimationEndPoint);
			mAdapter.notifyDataSetChanged();

			if (isLevelComplete()) {
				completed = true;
				//Log.d(TAG, "Hurray Level Completed");
				SharedPreferences prefs = this.getSharedPreferences(
						"pazzled.game.play", Context.MODE_PRIVATE);

				int max_completed_manually = prefs.getInt(
						"pazzled.game.play.max_completed_manually", 0);
				max_completed_manually = Utils.max(max_completed_manually,
						level);
				prefs.edit()
						.putInt("pazzled.game.play.max_completed_manually",
								max_completed_manually).commit();

				clock.Stop();
				if (ad != null) {
					ad.stopAds();
				}
				long num_seconds = clock.getTimeElapsedSeconds();
				addAndSendAnalytics(
						"completed",
						Long.toString(num_seconds) + '~'
								+ Integer.toString(score) + '~'
								+ Integer.toString(level));
				HighScore high_score = new HighScore(this, this, level, score,
						(int) num_seconds);
				high_score.showDialog(getWindow().getDecorView());
			} else {
				AddFocus(AnimationEndPoint);
			}
		} else {
			//Log.d(TAG, "Something wrong here.");
		}
		//Log.d(TAG, "Animation ended");
	}

	private void SwitchLevel(int _level, boolean force_next) {
		SharedPreferences prefs = this.getSharedPreferences("pazzled.game.play",
				Context.MODE_PRIVATE);
		int max_completed = prefs.getInt("pazzled.game.play.max_completed", 1);

		if (_level <= max_completed || force_next) {
			prefs.edit().putInt("pazzled.game.play.level", _level).commit();
			prefs.edit()
					.putInt("pazzled.game.play.max_completed",
							Utils.max(_level - 1, max_completed)).commit();
			final Intent game = new Intent("android.intent.action.MAIN");
			game.setComponent(new ComponentName(this, GameActivity.class));
			finish();
			startActivity(game);
			resetCheckPoint();
//			System.gc();
			// BackgroundAudioTrack.release();
		} else {
			//Log.d(TAG, "You have to complete this level to proceed.!");
		}
	}

	public void cleanup() {
		return;

	}

	public boolean isLevelComplete() {
		Iterator<Point> keys = atom_location.keySet().iterator();
		Point min = new Point(level_play.length, level_play[0].length());
		Point max = new Point(0, 0);
		for (; keys.hasNext();) {
			Point pt = keys.next();
			min.x = Utils.min(pt.x, min.x);
			min.y = Utils.min(pt.y, min.y);
			max.x = Utils.max(pt.x, max.x);
			max.y = Utils.max(pt.y, max.y);
		}
		int size = molecule_max_size;
		int x_diff = max.x - min.x + 1;
		int y_diff = max.y - min.y + 1;
		if (x_diff > size || y_diff > size || (x_diff < size && y_diff < size))
			return false;

		char buffer[][] = new char[size][size];
		for (int bi = 0, i = min.x; i < min.x + size; bi++, i++) {
			for (int bj = 0, j = min.y; j < min.y + size; bj++, j++) {
				Point pt = new Point(i, j);
				if (isValid(pt))
					buffer[bi][bj] = CharAt(pt);
				else
					buffer[bi][bj] = empty_cells.charAt(0);
			}
		}
		char[][] matrix = buffer;
		for (int i = 0; i < 4; i++) {
			if (Utils.equals(matrix, molecule_regex)) {
				return true;
			}
			matrix = Utils.RotateMatrix(matrix, size);
		}
		return false;
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}

	@Override
	public void onAnimationStart(Animation animation) {
		//Log.d(TAG, "Animation Started");
	}

	public boolean isEnabled(int row, int column) {
		Point cell = new Point(row, column);

		if (isAtom(cell) || isAction(cell)) {
			//Log.d(TAG, "IsEnabled " + row + ", " + column);
			return true;
		}
		//Log.d(TAG, "Is not Enabled " + row + ", " + column);
		return true;
	}

	public boolean isAtom(Point cell) {
		char current_item = level_play[cell.x].charAt(cell.y);
		return isAtom(current_item);
	}

	public boolean isAtom(char current_item) {
		for (int i = 0; i < atom_cells.length(); i++) {
			if (atom_cells.charAt(i) == current_item) {
				return true;
			}
		}
		return false;
	}

	public boolean isAction(Point cell) {
		char current_item = CharAt(cell);
		for (int i = 0; i < action_cells.length(); i++) {
			if (action_cells.charAt(i) == current_item) {
				return true;
			}
		}
		return false;
	}

	public boolean isWall(Point cell) {
		char current_item = CharAt(cell);
		for (int i = 0; i < wall.length(); i++) {
			if (wall.charAt(i) == current_item) {
				return true;
			}
		}
		return false;
	}

	public boolean isEmpty(Point cell) {
		char current_item = CharAt(cell);
		for (int i = 0; i < empty_cells.length(); i++) {
			if (empty_cells.charAt(i) == current_item) {
				return true;
			}
		}
		for (int i = 0; i < action_cells.length(); i++) {
			if (action_cells.charAt(i) == current_item) {
				return true;
			}
		}
		return false;
	}

	public int getImage(int row, int column) {
		char img = level_play[row].charAt(column);
		String uri = "drawable/" + img;

		return Utils.getIdentifier(this, uri);
	}

	private void changeCell(Point old, Point next) {
		char temp = CharAt(old);
		level_play[old.x] = Utils.insertChar(level_play[old.x], old.y,
				CharAt(next));
		level_play[next.x] = Utils.insertChar(level_play[next.x], next.y, temp);

		View v = getViewAt(old);
		v.invalidate();
		v = getViewAt(next);
		v.invalidate();
		atom_location.remove(old);
		atom_location.put(next, CharAt(next));
		return;
	}

	private Direction getApproximateDirection(Point cell) {
		int xd = cell.x - focusedPoint.x;
		int yd = cell.y - focusedPoint.y;
		int mod_xd = (xd < 0) ? -xd : xd;
		int mod_yd = (yd < 0) ? -yd : yd;

		Direction d = Direction.MOVE_INVALID;

		if (mod_xd <= 1 && yd >= 2) {
			d = Direction.MOVE_RIGHT;
		} else if (mod_yd <= 1 && xd >= 2) {
			d = Direction.MOVE_DOWN;
		} else if (mod_yd <= 1 && xd <= -2) {
			d = Direction.MOVE_UP;
		} else if (mod_xd <= 1 && yd <= -2) {
			d = Direction.MOVE_LEFT;
		}
		return d;
	}

	private Direction getDirection(char action) {
		Direction d = Direction.MOVE_INVALID;
		switch (action) {
		case 'e': {
			d = Direction.MOVE_LEFT;
			break;
		}
		case 'r': {
			d = Direction.MOVE_RIGHT;
			break;
		}
		case 'u': {
			d = Direction.MOVE_UP;
			break;
		}
		case 'd': {
			d = Direction.MOVE_DOWN;
			break;
		}
		default:
			break;
		}
		return d;

	}

	public void DoAction(Point current, Point next, Direction d,
			boolean addToHistory) {
		if (numActionsHappening == 0) {
			numActionsHappening += 1;
			if (next == null) {
				for (int i = 1; i < Utils.max(mAdapter.getNumColumns(),
						mAdapter.getNumRows()); i++) {
					Point pt = getithCell(current, d, i);
					if (pt != null && isEmpty(pt)) {
						next = pt;
					} else
						break;
				}
			}

			AnimationEndPoint = next;
			if (addToHistory) {
				increaseScoreDisplay();
				if (historyFrom.size() != currentItemInHistory) {
					for (int i = historyFrom.size(); i > currentItemInHistory; i--) {
						historyFrom.remove(i - 1);
						historyTo.remove(i - 1);
					}
				}
				historyTo.add(next);
				historyFrom.add(current);
				currentItemInHistory += 1;
			}
			onAnimationEnd(null);
		}
	}

	@Override
	public void onClick(View v) {

		// if(clickDisabled) {
		// guideTouch(v.getId());
		// return;
		// }
		
		switch (v.getId()) {
		case R.id.undo: {

			if (completed)
				return;
			
			if (currentItemInHistory == 0)
				return;
			if (currentItemInHistory > historyFrom.size()) {
				currentItemInHistory = historyFrom.size();
				return;
			}

			decreaseScoreDisplay();
			currentItemInHistory -= 1;

			Point next = historyFrom.get(currentItemInHistory);
			Point current = historyTo.get(currentItemInHistory);
			removeFocus(focusedPoint);
			focusedPoint = current;
			DoAction(current, next, null, false);

			break;
		}
		case R.id.redo: {

			if (completed)
				return;
			
			if (currentItemInHistory == historyFrom.size())
				return;

			increaseScoreDisplay();
			Point current = historyFrom.get(currentItemInHistory);
			Point next = historyTo.get(currentItemInHistory);
			currentItemInHistory += 1;
			removeFocus(focusedPoint);
			focusedPoint = current;
			DoAction(current, next, null, false);

			break;
		}
		case R.id.show_structure:
		case R.id.molecule_name: {
			displayMoleculeStructure();
			break;
		}

		case R.id.molecule_popup:
		case R.id.next_popup:
		case R.id.level_target:
		case R.id.molecule_structure: {
			if (molecule_display.isShowing()) {
				molecule_display.dismiss();
				if (ad != null)
					ad.enableAds(this);
				if (level == 0) {
					guideTouch(0);
				}
				/*
				 * if (ad != null) ad.showInterval();
				 */
				// clock.Start();
			}
			break;
		}
		case R.id.m_settings: {
			//Log.d(TAG, "clicked on settings button");
			openOptionsMenu();
			break;
		}
		default:
			break;
		}
	}

	public void OnShow() {

	}

	private void displaceLevelView(int x, int y) {
		int removed_x = Utils.max(x - 1, 0);
		int removed_y = y;

		int new_x = level_play.length - removed_x;
		int new_y = level_play[0].length() - removed_y;
		int numRows = mAdapter.getNumRows();
		int numCols = mAdapter.getNumColumns();

		String[] scroll_level_play;
		scroll_level_play = new String[numRows];
		for (int i = 0; i < numRows; i++) {
			StringBuilder builderString = new StringBuilder(numCols);
			for (int j = 0; j < numCols; j++) {
				char ch;
				if (i >= new_x || j >= new_y) {
					ch = empty_cells.charAt(0);
				} else {
					ch = level_play[(i + removed_x)].charAt((j + removed_y));
				}
				Point pt = new Point(i, j);
				if (isAtom(ch)) {
					atom_location.put(pt, ch);
				}
				builderString.append(ch);
			}
			scroll_level_play[i] = builderString.toString();
		}
		level_play = scroll_level_play;
	}

	@Override
	public void OnDismiss(HighScoreButton button) {
		switch (button) {
		case NEXT: {
			SwitchLevel(level + 1, true);
			break;
		}
		case RESTART: {
			SharedPreferences prefs = this.getSharedPreferences(
					"pazzled.game.play", Context.MODE_PRIVATE);
			int max_completed = prefs
					.getInt("pazzled.game.play.max_completed", 1);

			prefs.edit()
					.putInt("pazzled.game.play.max_completed",
							Utils.max(level, max_completed)).commit();

			SwitchLevel(level, true);
			break;
		}
		default:
			break;
		}
	}

	private void checkPointLevel() {

		//Log.d(TAG, "Level Checkpoint Save " + score);

		SharedPreferences prefs = this.getSharedPreferences("pazzled.game.play",
				Context.MODE_PRIVATE);

		prefs.edit().putInt("pazzled.game.play.checkpoint_level", level).commit();
		prefs.edit()
				.putInt("pazzled.game.play.checkpoint_rows_count",
						level_play.length).commit();
		prefs.edit().putInt("pazzled.game.play.checkpoint_score", score).commit();
		for (int i = 0; i < level_play.length; i++) {
			prefs.edit()
					.putString("pazzled.game.play.checkpoint_row_" + i,
							level_play[i]).commit();
		}

		if (focusedPoint != null) {
			prefs.edit()
					.putInt("pazzled.game.play.checkpoint_focused_x",
							focusedPoint.x).commit();
			prefs.edit()
					.putInt("pazzled.game.play.checkpoint_focused_y",
							focusedPoint.y).commit();
		} else {
			prefs.edit().putInt("pazzled.game.play.checkpoint_focused_x", -1)
					.commit();
			prefs.edit().putInt("pazzled.game.play.checkpoint_focused_y", -1)
					.commit();
		}
		prefs.edit().putInt("pazzled.game.play.isCheckPointAvailable", 1)
				.commit();

		//Log.d(TAG, "Level Checkpoint Save Done" + score);
	}

	private boolean isCheckPointAvailable() {
		SharedPreferences prefs = this.getSharedPreferences("pazzled.game.play",
				Context.MODE_PRIVATE);
		if (prefs.getInt("pazzled.game.play.isCheckPointAvailable", 0) == 0)
			return false;
		else
			return true;
	}

	private void resetCheckPoint() {
		//Log.d(TAG, "Removing Checkpoint");
		SharedPreferences prefs = this.getSharedPreferences("pazzled.game.play",
				Context.MODE_PRIVATE);
		prefs.edit().putInt("pazzled.game.play.isCheckPointAvailable", 0)
				.commit();
	}

	private void loadFromCheckPoint() {

		if (!isCheckPointAvailable())
			return;

		//Log.d(TAG, "Level Load Checkpoint " + score);

		SharedPreferences prefs = this.getSharedPreferences("pazzled.game.play",
				Context.MODE_PRIVATE);

		level = prefs.getInt("pazzled.game.play.checkpoint_level", level);
		int num_rows = prefs.getInt("pazzled.game.play.checkpoint_rows_count", 0);
		String[] level_p = new String[num_rows];
		for (int i = 0; i < num_rows; i++) {
			level_p[i] = prefs.getString("pazzled.game.play.checkpoint_row_" + i,
					"");
		}
		level_play = level_p;
		score = prefs.getInt("pazzled.game.play.checkpoint_score", 0) - 1;
		increaseScoreDisplay();

		int fx = prefs.getInt("pazzled.game.play.checkpoint_focused_x", -1);
		int fy = prefs.getInt("pazzled.game.play.checkpoint_focused_y", -1);
		if (fx == -1 && fy == -1)
			focusedPoint = null;
		else
			focusedPoint = new Point(fx, fy);

		resetCheckPoint();
	}

	protected void guideTouch(int id) {
		// TODO Auto-generated method stub
		//Log.d(TAG, "guide touch");
		boolean clickable = false;
		OnItemClickListener listener = null;

		class DummyListener implements OnItemClickListener {

			private Point enabledCell = null;
			private OnItemClickListener orig;
			private int id = 0;

			public DummyListener(Point cell, OnItemClickListener l, int tour_id) {
				enabledCell = cell;
				orig = l;
				id = tour_id;
			}

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				//Log.d(TAG, "Clicked item args " + position + " " + arg3);
				Point cell = mAdapter.getGridCoord(position);

				if ((cell.x == enabledCell.x && cell.y == enabledCell.y)
						|| id > 1) {
					//Log.d(TAG, "Got a click on enabled cell");
					orig.onItemClick(arg0, arg1, position, arg3);
					guideTouch(id + 1);
				} else {
					//Log.d(TAG, "clicked on cell (" + cell.x + ", " + cell.y + ")");
				}
			}
		}

		Point cell, prev_cell;
		switch (id) {
		case 0:
			cell = new Point(10, 2);
			SetFlashing(cell);
			listener = new DummyListener(cell, this, id);
			break;
		case 1:
			prev_cell = new Point(10, 2);
			cell = new Point(10, 1);
			StopFlashing(prev_cell);
			SetFlashing(cell);
			listener = new DummyListener(cell, this, id);
			break;
		case 2:
			prev_cell = new Point(10, 1);
			cell = new Point(9, 1);
			StopFlashing(prev_cell);
			// SetFlashing(cell);
			listener = new DummyListener(cell, this, id);
			new Popup(this, null, 0,
					"Bring the oxygen atoms together to form the level target.");
			break;

		default:
			// prev_cell = new Point(9, 1);
			// StopFlashing(prev_cell);
			listener = this;
			clickable = true;
//			System.gc();
			break;
		}

		GridView l = (GridView) findViewById(R.id.level);
		l.setOnItemClickListener(listener);

		View view = findViewById(R.id.main_game);
		view.setClickable(clickable);

		view = findViewById(R.id.undo);
		view.setClickable(clickable);

		view = findViewById(R.id.redo);
		view.setClickable(clickable);

		view = findViewById(R.id.m_settings);
		view.setClickable(clickable);

		view = findViewById(R.id.show_structure);
		view.setClickable(clickable);

		view = findViewById(R.id.molecule_name);
		view.setClickable(clickable);
	}

	@SuppressWarnings("deprecation")
	public boolean StopFlashing(Point cell) {
		View frame = getViewAt(cell);
		frame.setBackgroundDrawable(null);
		return true;
	}

	@SuppressWarnings("deprecation")
	public boolean SetFlashing(Point cell) {

		final int DELAY = 200;
		AnimationDrawable a = new AnimationDrawable();

		for (int i = 0xff000000; i <= 0xffff0000; i += 0xff400000) {
			ColorDrawable f = new ColorDrawable(i);
			a.addFrame(f, DELAY);
		}

		View frame = getViewAt(cell);
		frame.setBackgroundDrawable(a);
		a.start();
		return true;
	}

	@Override
	public void OnDismiss(int id) {

		//Log.d(TAG, "Popup closed - " + id);
		switch (id) {
		case 1:
			dialog = new Popup(
					this,
					this,
					2,
					"Atomic is a fun game built around molecular geometry. A molecule is disassembled into its separate atoms and scattered around the playing field. You must reassemble the molecule in order to complete the current level and move up to the next one.");
			break;
		case 2:
			dialog = new Popup(
					this,
					this,
					3,
					"Game Rules: \n\n"
							+ "1. Game pieces can only move in one direction at a time.\n"
							+ "2. Once an atom begins moving it will not stop until it meets either a wall or another piece.\n");
			break;
		case 3:
			dialog = new Popup(
					this,
					this,
					4,
					"Level Target: \n\n Bring the O atoms closer to create Oxygen molecule. Click on the atom to move.");
			break;
		default:
			if (ad != null)
				ad.enableAds(this);
			displayMoleculeStructure();
			break;
		}
	}

	@Override
	public void OnKVCreate() {
		addAndSendAnalytics("playing", Integer.toString(level));
	}

	public void addAndSendAnalytics(String key, String value) {
		Analytics.add(key, value);
		analytics.send(getResources().getString(R.string.analytics_url), this);
	}
}
