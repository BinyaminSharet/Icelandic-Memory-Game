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

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.GridView;
import android.widget.Toast;

public class BoardActivity extends Activity {
	/** Called when the activity is first created. */
	public static final String GAME_TYPE_STRING = "GAME_TYPE_STRING";
	public static final String GAME_NUMBERS_WORDS = "GAME_NUMERS_WORDS";
	public static final String BOARD_COMPLETION_TIME = "completion time";
	public static final String BOARD_ID_GAME_ID = "game id";
	public static final String USER_NAME_ID = "user name";
	public static final int CODE_FINISHED_WITH_TIME = 1;
	private int seconds = -1;
	private int game_id;
	private String username;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.board_layout);
		setGreetingTitle();

		final GridView gv = (GridView) findViewById(R.id.board_grid_gv);
		final BoardGridAdapter adapter = new BoardGridAdapter(this);
		game_id = getIntent().getExtras().getInt(BOARD_ID_GAME_ID);
		new AsyncTask<Void, Void, Void>() {
			Toast t;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				t = Toast.makeText(BoardActivity.this, "Loading Board...", Toast.LENGTH_LONG);
				t.show();
			}

			@Override
			protected Void doInBackground(Void... arg0) {
				adapter.init(finishHandler, game_id);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				t.cancel();
				gv.setAdapter(adapter);
				activateTimer();
			}
		}.execute();
	}

	private void setGreetingTitle() {
		username = getIntent().getStringExtra(BoardActivity.USER_NAME_ID);
		if (username.equals("")) {
			username = "guest";
		}
		setTitle("Hello " + username + ", let's start playing");
	}

	private Handler finishHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			timerHandler.removeCallbacks(mUpdateTimeTask);
			Intent i = new Intent();
			i.putExtra(BoardActivity.BOARD_COMPLETION_TIME, seconds);
			i.putExtra(BoardActivity.BOARD_ID_GAME_ID, game_id);
			i.putExtra(BoardActivity.USER_NAME_ID, username);
			BoardActivity.this.setResult(CODE_FINISHED_WITH_TIME, i);
			finish();
		}
	};

	private Handler timerHandler = new Handler();

	Runnable mUpdateTimeTask;

	private void activateTimer() {
		mUpdateTimeTask = new Runnable() {
			public void run() {
				seconds++;
				int minutes = seconds / 60;
				int _seconds = seconds % 60;

				setTitle(String.format("%d:%02d", minutes, _seconds));
				timerHandler.postDelayed(this, 1000);
			}
		};
		timerHandler.postDelayed(mUpdateTimeTask, 0);
	}
}