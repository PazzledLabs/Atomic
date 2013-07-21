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

import pazzled.game.atomic.HomeActivity;
import pazzled.game.utils.common.Utils;
import pazzled.game.atomic.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class LevelActivity extends Activity implements OnClickListener {

	@SuppressWarnings("unused")
	private static final String TAG = HomeActivity.class.getSimpleName();

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

		int cellWidth, cellHeight;
		int numColumns = getResources().getInteger(R.integer.numColumns);
		int numRows = getResources().getInteger(R.integer.numRows);
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int max_size = Utils.max(metrics.widthPixels, metrics.heightPixels);

		//Log.d(TAG, "metrics height - " + metrics.heightPixels + " width - "+ metrics.widthPixels);
		cellWidth = (max_size / numColumns);
		cellHeight = (max_size / numRows);

		setContentView(R.layout.levels);
		LinearLayout levels_table = (LinearLayout) findViewById(R.id.levels_table);


		SharedPreferences prefs = this.getSharedPreferences(
				"pazzled.game.play", Context.MODE_PRIVATE);
		int max_completed_manually = prefs.getInt(
				"pazzled.game.play.max_completed_manually", 0) + 1;

		int max_level = getResources().getInteger(R.integer.number_of_levels);
		for (int iter = 1; iter < max_level; iter++) {
			TableRow level_row = new TableRow(this);
			level_row.setGravity(Gravity.CENTER);

			TableLayout molecule_structure = new TableLayout(this);
			molecule_structure.setGravity(Gravity.CENTER);

			String param_name = "array/level_" + iter + "_molecule";
			int param_id = Utils.getIdentifier(this, param_name);
			String[] molecule = getResources().getStringArray(param_id);
			int width = Utils.max(metrics.widthPixels / 3 + 1, (cellWidth / 2 + 1) * molecule[0].length() ), height = Utils.max(
					metrics.heightPixels / 7 + 1, (cellHeight / 2 + 1)
							* molecule.length);
			for (int i = 0; i < molecule.length; i++) {
				TableRow row = new TableRow(this);
				row.setGravity(Gravity.CENTER);
				for (int j = 0; j < molecule[i].length(); j++) {
					String uri = "drawable/" + molecule[i].charAt(j);
					int res_id = Utils.getIdentifier(this, uri);
					ImageView atom = new ImageView(this);
					atom.setLayoutParams(new LayoutParams(cellWidth / 2 + 1,
							cellHeight / 2 + 1));
					atom.setScaleType(ImageView.ScaleType.CENTER_CROP);
					atom.setImageResource(res_id);
					row.addView(atom);
				}
				molecule_structure.addView(row);
			}
			molecule_structure.setLayoutParams(new LayoutParams(width, height));

			param_name = "string/level_" + iter + "_molecule_name";
			param_id = Utils.getIdentifier(this, param_name);
			String molecule_name = getResources().getString(param_id);
			TextView mol_name = new TextView(this);
			mol_name.setGravity(Gravity.CENTER);
			mol_name.setText(molecule_name);
			mol_name.setLayoutParams(new LayoutParams(width, height));
			
			ImageView status = new ImageView(this);
			status.setLayoutParams(new LayoutParams(cellWidth * 2 / 3, cellHeight * 2 / 3));
			status.setPadding(5, 5, 5, 5);
			
			level_row.addView(mol_name);
			level_row.addView(molecule_structure);
			level_row.addView(status);
			if(iter < max_completed_manually) {
				molecule_structure.setTag(iter);
				molecule_structure.setOnClickListener(this);
				status.setImageResource(R.drawable.right);
			} else if (iter == max_completed_manually) {
				molecule_structure.setTag(iter);
				molecule_structure.setOnClickListener(this);
				status.setImageResource(R.drawable.e);
			}
			else {
				status.setImageResource(R.drawable.wrong);
			}
				
			level_row.setTag(iter);
			levels_table.addView(level_row);

			TableRow row_border = new TableRow(this);
			row_border.setGravity(Gravity.CENTER);

			for (int iteri = 0; iteri < 3; iteri++) {
				ImageView cell = new ImageView(this);
				cell.setImageResource(R.drawable.brick);
				cell.setLayoutParams(new LayoutParams(cellWidth / 5 + 1,
						cellHeight / 5 + 1));
				cell.setScaleType(ImageView.ScaleType.CENTER_CROP);
				cell.setPadding(2, 1, 2, 1);
				row_border.addView(cell);
			}
			row_border.setPadding(5, 5, 5, 5);
			levels_table.addView(row_border);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int level = (Integer) v.getTag();
		//Log.d(TAG,  "On Click for row" + level);
		
		SharedPreferences prefs = this.getSharedPreferences(
				"pazzled.game.play", Context.MODE_PRIVATE);
		
		prefs.edit().putInt("pazzled.game.play.level", level).commit();
		prefs.edit().putInt("pazzled.game.play.isCheckPointAvailable", 0).commit();
		
		final Intent game = new Intent("android.intent.action.MAIN");
		game.setComponent(new ComponentName(this, GameActivity.class));
		startActivity(game);
		finish();
//		System.gc();
	}
}
