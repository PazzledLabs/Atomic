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
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	private Context mContext;
	private ImageAdapterInterface gridInterface;
	@SuppressWarnings("unused")
	private static final String TAG = ImageAdapter.class.getSimpleName();
	private int numColumns;
	private int numRows;
	private int cellWidth;
	private int cellHeight;

	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	public int getNumColumns() {
		return numColumns;
	}

	public int getNumRows() {
		return numRows;
	}

	public int getCellWidth() {
		return cellWidth;
	}

	public int getCellHeight() {
		return cellHeight;
	}

	public int getNumCells() {
		return numColumns * numRows;
	}

	public Point getGridCoord(int position) {
		int row = position / numColumns;
		int column = position % numColumns;
		return new Point(row, column);
	}

	public int getPosition(Point cell) {
		return cell.x * numColumns + cell.y;
	}

	public ImageAdapter(Context c, ImageAdapterInterface gridInterface, int numColumns, int numRows) {
		mContext = c;
		this.gridInterface = gridInterface;
		Resources res = mContext.getResources();

		DisplayMetrics metrics = res.getDisplayMetrics();
		//metrics.
		this.numColumns = numColumns;
		this.numRows = numRows;
		int max_size = Utils.max(metrics.widthPixels, metrics.heightPixels);
		//Log.d(TAG, "metrics height - " + metrics.heightPixels + " width - " + metrics.widthPixels);
		cellWidth = (max_size / numColumns) ;
		cellHeight = (max_size / numRows) ;
		//Log.d(TAG, "num of Columns is " + numColumns);
		//Log.d(TAG, "num of Columns is " + numRows);
		//Log.d(TAG, "width of each columns is " + cellWidth);
		//Log.d(TAG, "height of each columns is " + cellHeight);
	}

	public int getCount() {
		return numRows * numColumns;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public ImageView getEmptyImage() {
		ImageView imageView;
		imageView = new ImageView(mContext);
		imageView.setLayoutParams(new GridView.LayoutParams((int)(cellWidth),
				cellHeight));
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		return imageView;
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = getEmptyImage();
		} else {
			imageView = (ImageView) convertView;
		}

		int resid = getImage(position);
		if (resid != 0) {
			imageView.setImageResource(resid);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(1, 1, 1, 1);
		} else {
			imageView = getEmptyImage();
		}
		

		return imageView;
	}

	private int getImage(int position) {
		int row = position / numColumns;
		int column = position % numColumns;

		int res_id = gridInterface.getImage(row, column);
		return res_id;
	}

	public boolean isEnabled(int position) {
		int row = position / numColumns;
		int column = position % numColumns;

		return gridInterface.isEnabled(row, column);
	}
}
