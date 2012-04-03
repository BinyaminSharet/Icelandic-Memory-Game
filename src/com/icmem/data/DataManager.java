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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.icmem.game.MemoryApp;
import com.icmem.game.R;

public class DataManager {

	final public static int NOT_EXIST = -1;
	
	final public static Integer HIGH_SCORE_TITLE_ID = 1;
	final public static Integer HIGH_SCORE_USER_ID = 2;
	final public static Integer HIGH_SCORE_TIME_ID = 3;
	
	final private static int MAX_RETRIES = 3;

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

	public void updateGames(UpdateListener listener) {
		Status status;
		String sUpdate = "";
		int nextChunkNumber = 0;
		JSONObject jUpdate = null;
		int retries = 0;
		while (nextChunkNumber != NOT_EXIST) {
			Log.d(MemoryApp.DBG_STR, "Looking for chunk number " + nextChunkNumber);
			++retries;
			try {
				listener.onUpdate("Getting update " + nextChunkNumber);
				sUpdate = getChunk(nextChunkNumber);
				Log.d(MemoryApp.DBG_STR, "Got chunk");
				jUpdate = new JSONObject(sUpdate);
				status = updateGames(jUpdate.getJSONArray(GameProtocol.J_ID_GAMES_ARRAY));
				Log.d(MemoryApp.DBG_STR, "Updated object");
				if (status != Status.STATUS_OK) {
					listener.onUpdate("Failed to parse update." + status.name());
				}
				listener.onUpdate("Update " + nextChunkNumber + " processed");
				nextChunkNumber = getNextChunkNumber(jUpdate);
				retries = 0;
			}
			catch(JSONException e) {
				Log.d(MemoryApp.DBG_STR, "JSONException " + e);
			} catch (IOException e) {
				Log.d(MemoryApp.DBG_STR, "IOException " + e);
			} catch (URISyntaxException e) {
				Log.d(MemoryApp.DBG_STR, "URISyntaxException " + e);
			}
			if (retries == MAX_RETRIES)
				break;
		}
		listener.onUpdate("Update Completed");
	}
	
	private String getChunk(int cNumber) throws IOException, URISyntaxException {
		String res = null;
		BufferedReader in = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			String urlStr = context.getString(R.string.server_base_url) + context.getString(R.string.server_update_api_page);
			urlStr += "?chunk=" + cNumber;
			request.setURI(new URI(urlStr));
			Log.d(MemoryApp.DBG_STR, "Requesting chunk: " + urlStr);
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuilder sb = new StringBuilder();
			while((res = in.readLine()) != null) {
				sb.append(res);
			}
			res = sb.toString();
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch(IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return res;
	}

	private int getNextChunkNumber(JSONObject jUpdate) throws JSONException{
		int current, total;
		if (hasNextChunkInfo(jUpdate)) {
			current = jUpdate.getInt(GameProtocol.J_ID_CHUNK_NUMBER);
			total = jUpdate.getInt(GameProtocol.J_ID_CHUNK_TOTAL);
			if (current < total) {
				return current + 1;
			}
		}
		return NOT_EXIST;
	}
	
	private boolean hasNextChunkInfo(JSONObject jUpdate) {
		return jUpdate.has(GameProtocol.J_ID_CHUNK_NUMBER) && jUpdate.has(GameProtocol.J_ID_CHUNK_TOTAL);
	}
	
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
				
				// Is there a better way to specify the iterator generic type in this case?
				Iterator<?> it = gPairs.keys();
				while(it.hasNext()) {
					String key = (String)it.next();
			        String value = gPairs.getString(key);
			        mPairs.put(key,value);
				}
				storeGame(gId, gVersion, gTitle, gDesc, mPairs);
			}
			
		}
		return Status.STATUS_OK;
	}
	
	public Map<String, String> getWordsForGame(int gId) {
		return dbHandler.getPairs(gId);
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
		return dbHandler.getAllGamesBestTimes();
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