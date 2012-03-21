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

package com.icmem.game;

import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.icmem.data.DataManager;

public class GameSelection extends ListActivity {

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DataManager dm = DataManager.getDataManager();
		Map<Integer, String> mGames = dm.getGameMap();
		final int mapSize = mGames.size();
		final String[] titles = new String[mapSize];
		final Integer[] ids = new Integer[mapSize];
		final String username = getIntent().getExtras().getString(BoardActivity.USER_NAME_ID);
		
		int i = 0;
		for (Map.Entry<Integer, String> entry : mGames.entrySet()) {
			titles[i] = entry.getValue();
			ids[i] = entry.getKey();
			++i;
		}
		
		setListAdapter(new ArrayAdapter<String>(this, R.layout.game_list_item, titles));
		ListView listView = getListView();
		listView.setTextFilterEnabled(true);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(GameSelection.this, BoardActivity.class);
				intent.putExtra(BoardActivity.BOARD_ID_GAME_ID, ids[position]);
				intent.putExtra(BoardActivity.USER_NAME_ID, username);
				startActivityForResult(intent, 0);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent retIntent) {
		if (resultCode == BoardActivity.CODE_FINISHED_WITH_TIME) {
			Bundle data = retIntent.getExtras();
			final int time = data.getInt(BoardActivity.BOARD_COMPLETION_TIME);
			final int gId = data.getInt(BoardActivity.BOARD_ID_GAME_ID);
			final String user = data.getString(BoardActivity.USER_NAME_ID);
			if (DataManager.getDataManager().setNewScore(gId, time, user)) {
				String text = "Congrats, you've set a new highscore: " +
						Util.getTimeRepresentation(time);
				Toast.makeText(this, text, Toast.LENGTH_LONG).show();
				//show message if new high score
			}
		}
		super.onActivityResult(requestCode, resultCode, retIntent);
	}
}
