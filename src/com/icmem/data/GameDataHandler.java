/*
* Copyright (C) 2012 Binyamin Sharet
*
* This file is part of IcelandicMemoryGame.
* 
* IcelandicMemoryGame is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* IcelandicMemoryGame is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with IcelandicMemoryGame. If not, see <http://www.gnu.org/licenses/>.
*/

package com.icmem.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameDataHandler extends SQLiteOpenHelper {

	// All Static variables
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "Games";
	private static final String GAME_TABLE = "Games";
	private static final String HIGH_SCORE_TABLE = "High_Scores";
	private static final String BASE_PAIR_TABLE = "Pairs_";
	
	private static final String GAME_COL_ID = "gId";
	private static final String GAME_COL_TITLE = "gTitle";
	private static final String GAME_COL_DESC = "gDesc";
	private static final String GAME_COL_VERSION = "gVersion";
	
	private static final String HIGH_SCORE_COL_ID = GAME_COL_ID;
	private static final String HIGH_SCORE_COL_TIME = "gTime";
	private static final String HIGH_SCORE_COL_USER = "hUser";
	
	private static final String PAIR_COL_FIRST = "pFirst";
	private static final String PAIR_COL_SECOND = "pSecond";

	public GameDataHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createGameTable = 	"CREATE TABLE " + GAME_TABLE +
									"(" + 
									GAME_COL_ID + " INTEGER PRIMARY KEY, " +
									GAME_COL_TITLE + " TEXT, " +
									GAME_COL_DESC + " TEXT, " +
									GAME_COL_VERSION + " INTEGER" +
									")";
		db.execSQL(createGameTable);
		String createHSTable = 	"CREATE TABLE " + HIGH_SCORE_TABLE +
				"(" + 
				HIGH_SCORE_COL_ID + " INTEGER PRIMARY KEY, " +
				HIGH_SCORE_COL_TIME + " INTEGER, " +
				HIGH_SCORE_COL_USER + " TEXT" + 
				")";
		db.execSQL(createHSTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + GAME_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + HIGH_SCORE_TABLE);
	}
	
	public int getGameVersion(int gId) {
		int res;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(GAME_TABLE, new String[] { GAME_COL_VERSION,
                }, GAME_COL_ID + "=?",
                new String[] { String.valueOf(gId) }, null, null, null, null);
		if ((cursor != null) && (cursor.getCount() > 0)) {
			cursor.moveToFirst();
			res = cursor.getInt(cursor.getColumnIndex(GAME_COL_VERSION));
		}
		else {
			res = DataManager.NOT_EXIST;
		}
		if (cursor != null) {
			cursor.close();
		}
		return res;
	}
	
	public boolean gameExists(int gId) {
		boolean res = false;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(GAME_TABLE, new String[] { GAME_COL_ID,
                }, GAME_COL_ID + "=?",
                new String[] { String.valueOf(gId) }, null, null, null, null);
		if (cursor != null) {
			res = cursor.getCount() > 0;
			cursor.close();
		}
		return res;
	}
	
	public void removeGame(int gId) {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(GAME_TABLE, GAME_COL_ID + " = ?", new String[] { String.valueOf(gId)});
		db.close();
	}
	
	public void addGame(int gId, int gVersion, String gTitle, String gDesc) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(GAME_COL_ID, gId);
		values.put(GAME_COL_TITLE, gTitle);
		values.put(GAME_COL_DESC, gDesc);
		values.put(GAME_COL_VERSION, gVersion);
		if (gameExists(gId)) {
			db.update(GAME_TABLE, values, GAME_COL_ID + " = ?", new String[] { String.valueOf(gId)});
		}
		else {
			db.insert(GAME_TABLE, null, values);
		}
		db.close();
	}
	
	public void setGamePairs(int gId, Map<String, String> mPairs) {
		String pairTable = BASE_PAIR_TABLE + gId;
		SQLiteDatabase db = getWritableDatabase();
		// delete the table
		db.execSQL("DROP TABLE IF EXISTS " + (pairTable));
		// re-create the table
		String createPairTable = 	"CREATE TABLE " + pairTable +
				"(" + 
				PAIR_COL_FIRST + " TEXT, " +
				PAIR_COL_SECOND + " TEXT " +
				")";
		db.execSQL(createPairTable);
		// insert pairs TBD
		ContentValues values = new ContentValues();
		for(Map.Entry<String, String> pair : mPairs.entrySet()) {
			values.clear();
			values.put(PAIR_COL_FIRST, pair.getKey());
			values.put(PAIR_COL_SECOND, pair.getValue());
			db.insert(pairTable, null, values);
		}
		db.close();
	}
	
	public Map<String, String> getWordsForGame(int gId) {
		String pairTable = BASE_PAIR_TABLE + gId;
		SQLiteDatabase db = getReadableDatabase();
		Map<String, String> mPairs = new HashMap<String, String>();
		Cursor cursor = db.query(pairTable, new String[] { PAIR_COL_FIRST, PAIR_COL_SECOND }, null, null, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				int fIndex = cursor.getColumnIndex(PAIR_COL_FIRST);
				int sIndex = cursor.getColumnIndex(PAIR_COL_SECOND);
				do {
					mPairs.put(cursor.getString(fIndex), cursor.getString(sIndex));
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		db.close();
		return mPairs;
	}
	
	public Map<Integer, String> getGameMap() {
		SQLiteDatabase db = getWritableDatabase();
		Map<Integer, String> resMap = null;
		//String query = "SELECT  * FROM " + GAME_TABLE;
		//Cursor c = db.rawQuery(query, null);
		Cursor c = db.query(GAME_TABLE, new String[]{GAME_COL_ID, GAME_COL_TITLE}, null, null, null, null, null);
		if (c != null) {
			if (c.getCount() > 0) {
				resMap = new HashMap<Integer, String>();
				c.moveToFirst();
				int id = c.getColumnIndex(GAME_COL_ID);
				int title = c.getColumnIndex(GAME_COL_TITLE);
				do {
					resMap.put(c.getInt(id), c.getString(title));
				} while(c.moveToNext());		
			}
			c.close();
		}
		db.close();
		return resMap;		
	}
	
	public int getGameBestTime(int gId) {
		SQLiteDatabase db = getReadableDatabase();
		int res = DataManager.NOT_EXIST;
		Cursor cursor = db.query(HIGH_SCORE_TABLE, new String[]{ HIGH_SCORE_COL_TIME }, HIGH_SCORE_COL_ID + "=?",
                new String[] { String.valueOf(gId) }, null, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				res = cursor.getInt(cursor.getColumnIndex(HIGH_SCORE_COL_TIME));
			}
			cursor.close();
		}
		db.close();
		return res;
	}
	
	public void setBestTime(int gId, int time, String name) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(HIGH_SCORE_COL_ID, gId);
		values.put(HIGH_SCORE_COL_TIME, time);
		values.put(HIGH_SCORE_COL_USER, name);
		db.insert(HIGH_SCORE_TABLE, null, values);
		db.close();
	}
	
	public void updateBestTime(int gId, int time, String name) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(HIGH_SCORE_COL_ID, gId);
		values.put(HIGH_SCORE_COL_TIME, time);
		values.put(HIGH_SCORE_COL_USER, name);
		db.insert(HIGH_SCORE_TABLE, null, values);
		db.update(HIGH_SCORE_TABLE, values, HIGH_SCORE_COL_ID + "=?", new String[] { String.valueOf(gId)});
		db.close();
	}
	
	public Map<Integer, List<?>> getAllGamesBestTimes() {
		int count = 0;
		List<Integer> lTime = new ArrayList<Integer>();
		List<String> lUser = new ArrayList<String>();
		List<String> lTitle = new ArrayList<String>();
		SQLiteDatabase db = getReadableDatabase();
		String rawQuery = 	"SELECT " + GAME_COL_TITLE + " , " + HIGH_SCORE_COL_TIME + " , " + HIGH_SCORE_COL_USER + 
							" FROM " + HIGH_SCORE_TABLE + " INNER JOIN " + GAME_TABLE + " ON " +
							HIGH_SCORE_TABLE + "." + HIGH_SCORE_COL_ID + " = " + GAME_TABLE + "." + GAME_COL_ID;
		Cursor cursor = db.rawQuery(rawQuery, null);
		if (cursor != null){
			if((count = cursor.getCount()) > 0) {
				int timeId = cursor.getColumnIndex(HIGH_SCORE_COL_TIME);
				int userId = cursor.getColumnIndex(HIGH_SCORE_COL_USER);
				int titleId = cursor.getColumnIndex(GAME_COL_TITLE);
				cursor.moveToFirst();
				do {
					lTime.add(cursor.getInt(timeId));
					lUser.add(cursor.getString(userId));
					lTitle.add(cursor.getString(titleId));
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		db.close();
		Map<Integer, List<?>> allList = null;
		if (count > 0) {
			allList = new HashMap<Integer, List<?>>(3);
			allList.put(DataManager.HIGH_SCORE_TITLE_ID, lTitle);
			allList.put(DataManager.HIGH_SCORE_USER_ID, lUser);
			allList.put(DataManager.HIGH_SCORE_TIME_ID, lTime);	
		}
		return allList;
	}

}
