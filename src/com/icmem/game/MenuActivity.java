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
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.icmem.data.DataManager;
import com.icmem.data.UpdateListener;

public class MenuActivity extends Activity {

	private static final String SHARED_PREF_NAME = "MemoryApp"; 
	private static final String SHARED_PREF_USER = "username";
	private EditText username_et;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_layout);
		username_et = (EditText) findViewById(R.id.user_name_et);
		Button b = (Button)findViewById(R.id.game_list_button);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String username = username_et.getText().toString();
				Intent intent = new Intent(MenuActivity.this, GameSelection.class);
				intent.putExtra(getString(R.string.user_name), username_et.getText().toString());
				startActivity(intent);
				storeUserName(username);
			}
		});
		initDefaults.execute();
		loadUserName();
	}
	
	AsyncTask<String, String, Void> initDefaults = new AsyncTask<String, String, Void>() {

		ProgressDialog pd;
		@Override
		protected void onPostExecute(Void result) {
			pd.dismiss();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(MenuActivity.this);
			pd.setTitle("Setting default games");
			pd.show();
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			pd.setMessage(values[0]);
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(String... arg0) {
			DataManager.getDataManager().setDefaultGames(new UpdateListener(){
				@Override
				public void onUpdate(String message) {
					publishProgress(message);
				}
			});
			return null;
		}
		
	};
	
	private void storeUserName(String username) {
		SharedPreferences pref = this.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(SHARED_PREF_USER, username);
		editor.commit();
	}
	
	private void loadUserName() {
		SharedPreferences pref = this.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
		String username = pref.getString(SHARED_PREF_USER, null);
		if (username != null) {
			username_et.setText(username);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_item_high_scores:
	    {
	    	Intent intent = new Intent(this, BestResults.class);
	    	startActivity(intent);
	        return true;
	    }
	    case R.id.menu_item_about:
	    {
	    	Intent intent = new Intent(this, AboutActivity.class);
	    	startActivity(intent);
	    	return true;
	    }
	    case R.id.menu_item_update:
	    {
	    	updateFromServer();
	    }
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private void updateFromServer() {
		AsyncTask<Void, String, Void> updateTask = new AsyncTask<Void, String, Void>(){
			ProgressDialog dialog;
			@Override
			protected Void doInBackground(Void... arg0) {
				DataManager.getDataManager().updateGames(new UpdateListener(){
					@Override
					public void onUpdate(String message) {
						publishProgress(message);
					}
				});
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				dialog.dismiss();
				super.onPostExecute(result);
			}

			@Override
			protected void onPreExecute() {
				dialog = new ProgressDialog(MenuActivity.this);
				dialog.setTitle("Updating from Server");
				super.onPreExecute();
			}

			@Override
			protected void onProgressUpdate(String... values) {
				dialog.setMessage(values[0]);
				super.onProgressUpdate(values);
			}
			
		};
		updateTask.execute();
	}
}
