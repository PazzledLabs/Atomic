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
package pazzled.game.utils;

import pazzled.game.utils.common.Utils;
import pazzled.game.utils.common.Utils.HighScoreButton;
import pazzled.game.utils.social.SocialConnect;
import pazzled.game.atomic.R;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class HighScore implements OnClickListener, OnEditorActionListener {

	@SuppressWarnings("unused")
	private static final String TAG = HighScore.class.getSimpleName();
	private Dialog high_score;
	private Context mContext;
	private SharedPreferences prefs;
	private int level;
	private int max_rows_to_display;
	private int rank;
	private highScoreRecord current_record;
	private int current_record_id;
	private TextView name_current;
	private EditText name_edit;
	private TableRow current_row;
	private boolean readOnly;
	private View level_view;

	public void setMaxRowsToDisplay(int max_rows_to_display) {
		this.max_rows_to_display = max_rows_to_display;
	}

	private HighScoreListener game;

	private class highScoreRecord {
		String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getNumSeconds() {
			return num_seconds;
		}

		public int getScore() {
			return score;
		}

		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}

		int num_seconds;
		int score;
		int country;
		int level;
		int id;
		int rank;

		public highScoreRecord(int level, int id, String name, int score,
				int num_seconds, int country, int rank) {
			this.name = name;
			this.id = id;
			this.level = level;
			this.score = score;
			this.country = country;
			this.num_seconds = num_seconds;
			this.rank = rank;
		}

		public highScoreRecord(int level, int id) {
			this.id = id;
			this.level = level;
			try {
				this.name = prefs.getString("pazzled.game.play.record_name_"
						+ level + "_" + id, null);
				this.score = prefs.getInt("pazzled.game.play.record_score_"
						+ level + "_" + id, -1);
				this.num_seconds = prefs.getInt(
						"pazzled.game.play.record_num_seconds_" + level + "_"
								+ id, -1);
				this.country = prefs.getInt(
						"pazzled.game.play.record_country_" + level + "_"
								+ id, -1);
				this.rank = prefs.getInt("pazzled.game.play.record_rank_"
						+ level + "_" + id, -1);
			} catch (ClassCastException e) {
				//Log.d(TAG, "record " + id + " for level " + level + " does not exist");
			}
		}

		private void commit() {
			prefs.edit()
					.putString(
							"pazzled.game.play.record_name_" + level + "_"
									+ id, this.name).commit();
			prefs.edit()
					.putInt("pazzled.game.play.record_score_" + level + "_"
							+ id, this.score).commit();
			prefs.edit()
					.putInt("pazzled.game.play.record_num_seconds_" + level
							+ "_" + id, this.num_seconds).commit();
			prefs.edit()
					.putInt("pazzled.game.play.record_country_" + level + "_"
							+ id, this.country).commit();
			prefs.edit()
					.putInt("pazzled.game.play.record_id_" + level + "_" + id,
							this.id).commit();
			prefs.edit()
					.putInt("pazzled.game.play.record_rank_" + level + "_"
							+ id, this.rank).commit();
		}
	}

	public HighScore(Context c, HighScoreListener game, int level, int score,
			int num_seconds) {
		mContext = c;
		max_rows_to_display = 10;
		current_record_id = 0;
		this.game = game;
		readOnly = false;
		prefs = mContext.getSharedPreferences("pazzled.game.play",
				Context.MODE_PRIVATE);

		this.level = level;
		setHighScoreRecord(level, score, num_seconds);
	}

	public HighScore(Context c, HighScoreListener game, int level) {
		mContext = c;
		max_rows_to_display = 10;
		current_record_id = 0;
		readOnly = true;
		this.game = game;
		prefs = mContext.getSharedPreferences("pazzled.game.play",
				Context.MODE_PRIVATE);
		this.level = level;
		this.rank = -1;
	}

	public void showDialog(View rootView) {
		// View view = View.inflate(mContext, R.layout.game, false);
		// mContext.get
		level_view = rootView;
		high_score = new Dialog(mContext,
				android.R.style.Theme_Translucent_NoTitleBar_Fullscreen) {
			public boolean onTouchEvent(MotionEvent event) {
				if (this.isShowing()) {
					//Log.d(TAG, "Touch outside the dialog");
					if (readOnly) {
						this.dismiss();
						game.OnDismiss(HighScoreButton.INVALID);
					}
				}
				return false;
			}
		};
		high_score.setContentView(R.layout.high_score);
		SocialConnect.initSocialIcons(high_score, mContext, this);
		TableLayout score_table = (TableLayout) high_score
				.findViewById(R.id.score_table);
		ImageButton restart = (ImageButton) high_score
				.findViewById(R.id.restart);
		ImageButton next = (ImageButton) high_score.findViewById(R.id.next);
		restart.setOnClickListener(this);
		next.setOnClickListener(this);

		highScoreRecord[] records = getRecords(level);
		for (int i = 0; i < Utils.min(records.length, max_rows_to_display); i++) {
			TableRow row = getRow(records, i);
			score_table.addView(row);
		}

		if (this.rank > max_rows_to_display) {
			TableRow row = getRow(records, this.rank);
			score_table.addView(row);
		}

		if (readOnly) {
			TableLayout score_actions = (TableLayout) high_score
					.findViewById(R.id.score_actions);
			score_actions.setVisibility(View.INVISIBLE);
		}

		score_table.setOnClickListener(this);

		if (records.length > 0) {
			int current_record_idx = 0;
			int level = this.level + 1;
			if (this.rank != -1) {
				current_record_idx = this.rank;
			}
			int seconds = records[current_record_idx].getNumSeconds();
			if (seconds < (level * 3 * 20)) {
				// 3 stars
				ImageView star = (ImageView) high_score
						.findViewById(R.id.star_1);
				star.setImageResource(android.R.drawable.btn_star_big_on);

				star = (ImageView) high_score.findViewById(R.id.star_2);
				star.setImageResource(android.R.drawable.btn_star_big_on);

				star = (ImageView) high_score.findViewById(R.id.star_3);
				star.setImageResource(android.R.drawable.btn_star_big_on);

			} else if (seconds < (level * 5 * 30)) {
				// 2 stars
				ImageView star = (ImageView) high_score
						.findViewById(R.id.star_1);
				star.setImageResource(android.R.drawable.btn_star_big_on);

				star = (ImageView) high_score.findViewById(R.id.star_2);
				star.setImageResource(android.R.drawable.btn_star_big_on);
			} else {
				// 1 star
				ImageView star = (ImageView) high_score
						.findViewById(R.id.star_1);
				star.setImageResource(android.R.drawable.btn_star_big_on);
			}
		}

		high_score.show();
		return;
	}

	private TableRow getRow(highScoreRecord[] records, int i) {
		TableRow row = new TableRow(mContext);
		EditText name_e = null;

		TextView rank = (TextView) new TextView(mContext);
		rank.setText(records[i].getRank() + 1 + " ");
		row.addView(rank);

		TextView name = (TextView) new TextView(mContext);
		name.setText(records[i].getName());
		name.setEllipsize(TruncateAt.END);

		TextView score = (TextView) new TextView(mContext);
		score.setText(records[i].getScore() + " ");

		TextView numSeconds = (TextView) new TextView(mContext);
		numSeconds.setText(records[i].getNumSeconds() + " ");

		if (i == this.rank) {
			name_e = new EditText(mContext);
			name_e.setText(records[i].getName());
			name_e.setSingleLine(true);
			// name_e.setTextColor(R.color.black);
			name_e.setImeOptions(EditorInfo.IME_ACTION_DONE);
			name_e.setOnEditorActionListener(this);
			// name_e.setOnKeyListener(this);

			if (records[i].getName() == "") {
				row.addView(name_e);
			} else {
				row.addView(name);
			}

			name_edit = name_e;
			name_current = name;
			current_row = row;

			rank.setTypeface(Typeface.DEFAULT_BOLD);
			score.setTypeface(Typeface.DEFAULT_BOLD);
			numSeconds.setTypeface(Typeface.DEFAULT_BOLD);
			name.setTypeface(Typeface.DEFAULT_BOLD);
			name.setTextAppearance(mContext, R.style.current_item);
			current_record = records[i];

			current_record_id = row.getId();
			row.setOnClickListener(this);

		} else {
			row.addView(name);
		}

		row.addView(score);
		row.addView(numSeconds);

		return row;
	}

	private void setHighScoreRecord(int level, int score, int num_seconds) {
		int country = SocialConnect.getCountryId();
		int num_rows = getNumberOfHighScores(level);
		int rank = getRank(level, score, num_seconds);
		// String name = Utils.getUsername(mContext).get(0);
		String name = SocialConnect.getName(mContext);
		highScoreRecord[] records = getRecords(level);

		for (int i = num_rows - 1; i >= rank; i--) {
			highScoreRecord record = records[i];
			record.setRank(record.getRank() + 1);
			record.commit();
		}
		highScoreRecord current_record = new highScoreRecord(level, num_rows,
				name, score, num_seconds, country, rank);
		current_record.commit();
		setNumberOfHighScores(level, num_rows + 1);
		this.rank = rank;
	}

	private int getRank(int level, int score, int num_seconds) {
		highScoreRecord[] records = getRecords(level);
		int rank = records.length;
		for (int i = 0; i < records.length; i++) {
			highScoreRecord record = records[i];
			if (record.getScore() > score) {
				rank = i;
				break;
			} else if (record.getScore() == score
					&& record.getNumSeconds() > num_seconds) {
				rank = i;
				break;
			}
		}
		return rank;
	}

	private highScoreRecord[] getRecords(int level) {
		int num_rows = getNumberOfHighScores(level);
		highScoreRecord[] records = new highScoreRecord[num_rows];
		for (int i = 0; i < num_rows; i++) {
			highScoreRecord record = new highScoreRecord(level, i);
			records[record.getRank()] = record;
		}
		return records;
	}

	private int getNumberOfHighScores(int level) {
		int num_high_scores = prefs.getInt(
				"pazzled.game.play.num_high_score_" + level, 0);
		return num_high_scores;
	}

	private void setNumberOfHighScores(int level, int num_of_high_score) {
		prefs.edit()
				.putInt("pazzled.game.play.num_high_score_" + level,
						num_of_high_score).commit();
		return;
	}

	@Override
	public void onClick(View v) {
		int view_id = v.getId();
		switch (view_id) {
		case R.id.score_table: {
			if (high_score.isShowing()) {
				if (readOnly) {
					high_score.dismiss();
					game.OnDismiss(HighScoreButton.INVALID);
				}
			}
			break;
		}
		case R.id.restart: {
			high_score.dismiss();
			game.OnDismiss(HighScoreButton.RESTART);
			break;
		}
		case R.id.next: {
			high_score.dismiss();
			game.OnDismiss(HighScoreButton.NEXT);
			break;
		}
		case R.id.social_layout:
		case R.id.social_bar:
		case R.id.share:
		case R.id.facebook:
		case R.id.twitter:
		case R.id.whatsapp: {
			//Log.d(TAG, "Clicked on whatsapp");
			SocialConnect.onClickApp(mContext, v, mContext.getResources()
					.getString(view_id), level_view);
			break;
		}
		default:
			if (view_id == current_record_id) {
				//Log.d(TAG, "Switching to next view");
				// name_switcher.showNext();
				TableRow row = (TableRow) v;
				// TextView name = (TextView) row.getChildAt(1);
				row.removeViewAt(1);
				row.addView(name_edit, 1);
			}
			break;
		}
	}

	public void reset() {
		setNumberOfHighScores(level, 0);
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

		//Log.d(TAG, "OnEditorAction " + actionId);
		String name = v.getText().toString();

		if (actionId == EditorInfo.IME_ACTION_DONE
				|| actionId == EditorInfo.IME_ACTION_UNSPECIFIED
				|| actionId == KeyEvent.KEYCODE_ENTER) {
			InputMethodManager imm = (InputMethodManager) v.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

			current_row.removeViewAt(1);
			current_row.addView(name_current, 1);
			name_current.setText(name);
			current_record.setName(name);
			current_record.commit();
			SocialConnect.setName(mContext, name);

			return true;
		}

		//Log.d(TAG, "Editor event " + actionId + " " + event.getAction());
		//Log.d(TAG, name);
		return false;
	}

	/*
	 * @Override public boolean onKey(View v, int keyCode, KeyEvent event) { //
	 * TODO Auto-generated method stub onEditorAction((TextView)v,
	 * event.getAction(), event); return false; }
	 */
}
