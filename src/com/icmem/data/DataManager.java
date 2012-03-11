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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.icmem.game.MemoryApp;

public class DataManager {

	final public static int NOT_EXIST = -1;
	
	final public static Integer HIGH_SCORE_TITLE_ID = 1;
	final public static Integer HIGH_SCORE_USER_ID = 2;
	final public static Integer HIGH_SCORE_TIME_ID = 3;


	final private Context context;
	final private static DataManager instance = new DataManager(MemoryApp.getApplication()); 
	private GameDataHandler dbHandler = null;
	
	
	
	private DataManager(Context context) {
		this.context = context;
		dbHandler = new GameDataHandler(context);
	}

	public static DataManager getDataManager() {
		return instance;
	}
	
	public Map<Integer, String> getGameMap() {
		return new GameDataHandler(context).getGameMap();		
	}
	
	private String getDefaultGames() {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new InputStreamReader(context.getAssets().open("DefaultGames.txt")));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
		finally {
			try {
				if (br != null) {
					br.close();
				}
			}
			catch(IOException ex) {
                ex.printStackTrace();
            }
		}
		
		return sb.toString();
	}

	public void setDefaultGames(UpdateListener listener) {
		String defaultData = getDefaultGames();
		JSONObject jUpdate = null;
		listener.onUpdate("Setting default games");
		try {
			jUpdate = new JSONObject(defaultData);
			updateGames(jUpdate.getJSONArray(GameProtocol.J_ID_GAMES_ARRAY));
			listener.onUpdate("Update default games are set"); 
		}
		catch(JSONException e) {
			Log.e(MemoryApp.DBG_STR, "Damn JSON exception" + e);
		}			
	}
	/*
	public void updateGames(UpdateListener listener) {
		Status status;
		String sUpdate = "";
		int nextChunkNumber = 0;
		JSONObject jUpdate = null;
		while (nextChunkNumber > -1) {
			listener.onUpdate("Getting update " + nextChunkNumber);
			sUpdate = getChunk(nextChunkNumber);
			try {
				jUpdate = new JSONObject(sUpdate);
				status = updateGames(jUpdate.getJSONArray(J_ID_GAMES_ARRAY));
				listener.onUpdate("Update " + nextChunkNumber + " processed");
				nextChunkNumber = getNextChunkNumber(jUpdate); 
			}
			catch(JSONException e) {
				// failed to parse json object
			}			
		}
		listener.onUpdate("Update Completed");
	}
	
	private String getChunk(int cNumber) {
		return "";
	}
	
	private int getNextChunkNumber(JSONObject jUpdate) {
		int current;
		int total;
		try {
			current = jUpdate.getInt(J_ID_CHUNK_NUMBER);
			total = jUpdate.getInt(J_ID_CHUNK_TOTAL);
			if (current < total) {
				return current + 1;
			}
		}
		catch(JSONException e) {
			// This is an empty exception because it merges with the default result (-1)
		}
		return -1;
	}
	*/
	
	private int getCurrentGameVersion(int id) {
		return dbHandler.getGameVersion(id);
	}
	
	private void storeGame(int gId, int gVersion, String gTitle, String gDesc, Map<String, String>mPairs) {
		dbHandler.addGame(gId, gVersion, gTitle, gDesc);
		dbHandler.setGamePairs(gId, mPairs);
	}

	private Status updateGames (JSONArray games_arr) throws JSONException{
		for (int i = 0; i < games_arr.length(); ++i) {
			String gOp, gTitle, gDesc;
			int gId, gVersion;
			//JSONArray gPairs;
			JSONObject gPairs;
			JSONObject game = games_arr.getJSONObject(i);
			Map<String, String> mPairs;
			gId = game.getInt(GameProtocol.J_ID_GAME_ID);
			gVersion = game.getInt(GameProtocol.J_ID_GAME_VERSION);
			if (gVersion > getCurrentGameVersion(gId)) {
				gTitle = game.getString(GameProtocol.J_ID_GAME_TITLE);
				gDesc = game.has(GameProtocol.J_ID_GAME_DESC) ? game.getString(GameProtocol.J_ID_GAME_DESC) : null;  
				gOp = game.has(GameProtocol.J_ID_GAME_OPERATION) ? game.getString(GameProtocol.J_ID_GAME_OPERATION) : "update";
				// if operation is delete - no need to read pairs, just remove the old pairs
				if (GameProtocol.OPERATION_DELETE.equalsIgnoreCase(gOp)) {
					dbHandler.removeGame(gId);
					continue;
				}
				gPairs = game.getJSONObject(GameProtocol.J_ID_GAME_PAIRS);
				mPairs = new HashMap<String, String>();
				
				Iterator<?> it = gPairs.keys();
				while(it.hasNext()) {
					String key = (String)it.next();
			        String value = gPairs.getString(key);
			        mPairs.put(key,value);
				}
				storeGame(gId, gVersion, gTitle, gDesc, mPairs);
			}
			
		}
		return Status.STATUS_ERROR_MISSING_MANDATORY_FIELD;
	}
	
	public Map<String, String> getWordsForGame(int gId) {
		return dbHandler.getWordsForGame(gId);
	}
	
	public boolean setNewScore(int gId, int time, String name) {
		int currentBestTime = dbHandler.getGameBestTime(gId);
		if (currentBestTime == NOT_EXIST) {
			dbHandler.setBestTime(gId, time, name);
		}
		else if (time < currentBestTime) {
			dbHandler.updateBestTime(gId, time, name);
		}
		else { 
			return false;
		}
		return true;
	}
	
	public Map<Integer, List<?>>getAllHighScores() {
		return dbHandler.getAllHighScores();
	}

	enum Status {
		STATUS_OK								(0x00000000),
		STATUS_MORE_DATA_AVAILABLE				(0x00000001),
		STATUS_ERROR_JSON_SYNTAX 				(0x00001001),
		STATUS_ERROR_MISSING_MANDATORY_FIELD 	(0x00001002)
		;

		private final int value;
		Status(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}

}