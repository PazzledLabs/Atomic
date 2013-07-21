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


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KVStore extends SQLiteOpenHelper {

	@SuppressWarnings("unused")
	private static final String TAG = KVStore.class.getSimpleName();
	private static final int DATABASE_VERSION = 2;
	private static final String TABLE_NAME = "KVStore";
	private static final String KEY = "key";
	private static final String VALUE = "value";

	private KVStoreListener listener;
	private SQLiteDatabase db = null;

	private static final String CREATE_QUERY = "CREATE TABLE "
			+ "IF NOT EXISTS " + TABLE_NAME
			+ " (ID INTEGER PRIMARY KEY AUTOINCREMENT, TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " + KEY + " TEXT, "
			+ VALUE + " BLOB );";
	private static final String DATABASE_NAME = "pazzled.db";

	public KVStore(Context context, KVStoreListener listener) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.listener = listener;

	}

	public void createDatabase() {
		SQLiteDatabase temp = getWritableDatabase();
		if (db == null) {
			if (temp != null) {
				db = temp;
				listener.OnKVCreate();
			} else {
				//Log.d(TAG, "Something wrong, this should not happen");
			}
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_QUERY);
		//Log.d(TAG, "creating table " + TABLE_NAME);
		this.db = db;
		listener.OnKVCreate();
	}

	public void add(String key, String value) {
		ContentValues values = new ContentValues();
		values.put(KEY, key);
		values.put(VALUE, value);
		db.insertWithOnConflict(TABLE_NAME, null, values,
				SQLiteDatabase.CONFLICT_REPLACE);
	}

	public long count() {
		return DatabaseUtils.queryNumEntries(db, TABLE_NAME);
	}

	public String get(int id) {
		String selection = "ID = ?";
		String[] selectionArgs = { Integer.toString(id) };
		Cursor c = db.query(TABLE_NAME, null, selection, selectionArgs, null,
				null, null);

		boolean result = c.moveToFirst();

		if (result) {
			return c.getString(0);
		}
		return null;
	}

	public void remove(int id) {
		String whereClause = "ID =?";
		String[] whereArgs = { Integer.toString(id) };
		db.delete(TABLE_NAME, whereClause, whereArgs);
	}

	public NameValuePair[] getAll() {
		String sql = "SELECT ID, TIME, " + KEY + ", " + VALUE + " FROM " + TABLE_NAME;
		Cursor cursor = db.rawQuery(sql, null);
		NameValuePair[] results = new NameValuePair[cursor.getCount()];
		int iter = 0;
		if (cursor.moveToFirst()) {
			do {
				results[iter++] = new BasicNameValuePair(cursor.getString(0),
						cursor.getString(1) +  "~" +cursor.getString(2) + "~" + cursor.getString(3));
			} while (cursor.moveToNext());
			if (cursor != null && !cursor.isClosed())
				cursor.close();
		}
		return results;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
}
