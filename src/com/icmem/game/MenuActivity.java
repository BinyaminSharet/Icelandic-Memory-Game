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
				MemoryApp app = MemoryApp.getApplication();
				app.setCurrentUserName(username_et.getText().toString());
				Intent intent = new Intent(MenuActivity.this, GameSelection.class);
				intent.putExtra(getString(R.string.user_name), username_et.getText().toString());
				startActivity(intent);
			}
		});
		initDefaults.execute();
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
	
	
	
	
	
/*
	private void launchGame(String game) {
		Intent i = new Intent(MenuActivity.this, BoardActivity.class);
		String username = username_et.getText().toString();
		i.putExtra(getString(R.string.user_name), username);
		i.putExtra(getString(R.string.board_size), 4);
		i.putExtra(BoardActivity.GAME_TYPE_STRING, game);
		startActivityForResult(i, 0);
		saveToSharedPreferences(getString(R.string.user_name), username);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1) {
			int seconds = data.getIntExtra(getString(R.string.time_taken), 0);
			String game_type = data.getStringExtra(getString(R.string.game_type));
			String user_name = data.getStringExtra(getString(R.string.user_name));
			if (updateBestResults(user_name, game_type, seconds)) {
				StringBuilder message = new StringBuilder(200);
				message.append("You've set a new high score for the ");
				message.append(game_type);
				message.append(" game. Your new best time is ");
				message.append(Util.getTimeRepresentation(seconds));				
				Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private boolean updateBestResults(String user_name, String game_type, int seconds) {
		SharedPreferences pref = this.getSharedPreferences(BestResults.BEST_RESULTS_SHARED_PREFERENCES, MODE_PRIVATE);
		int prev_seconds = pref.getInt(game_type, 1500);
		if (seconds < prev_seconds) {
			Log.d("Binyamin", "Updating high scores " + game_type + "/" + seconds + "/" + user_name);
			SharedPreferences.Editor editor = pref.edit();
			editor.putInt(game_type, seconds);
			editor.putString(game_type + "user", user_name);
			editor.commit();
			return true;
		}
		return false;
	}

	private void saveToSharedPreferences(String key, String value) {
		SharedPreferences pref = this.getSharedPreferences(packageName, MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	private void setUserName() {
		SharedPreferences pref = this.getSharedPreferences(packageName,
				MODE_PRIVATE);
		String username = pref.getString(getString(R.string.user_name), null);
		if (username != null) {
			username_et.setText(username);
		}
	}

	private void initAllMaps() {
		SharedPreferences pref = this.getSharedPreferences(packageName,
				MODE_PRIVATE);
//		if (!pref.getBoolean("Maps Initiated", false)) {
			new DataInitiator().execute("");
	//	}
	}

	private class DataInitiator extends AsyncTask<String, Integer, Void> {
		private ProgressDialog dialog = new ProgressDialog(MenuActivity.this);
		int lines = 0;

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.setTitle("Updating Game Pairs");
			dialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (values[0] < lines) {
				dialog.setMessage("Saving pair set " + values[0] + " out of "
						+ lines);
			} else {
				dialog.setMessage("Saving updates");
			}
		}

		@Override
		protected Void doInBackground(String... params) {
			try {
				lines = Util.getNumberOfLines(getAssets().open("Pairs.txt"), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (lines <= 0) {
				return null;
			}
			int i = 1;
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						getAssets().open("Pairs.txt")));
				String line;
				SharedPreferences pref = getSharedPreferences(packageName,
						MODE_PRIVATE);
				SharedPreferences.Editor editor = pref.edit();
				while ((line = br.readLine()) != null) {
					publishProgress(i++);
					String[] lineParts = line.split("=");
					editor.putString(lineParts[0], lineParts[1]);
				}
				publishProgress(i++);
				editor.putBoolean("Maps Initiated", true);
				editor.commit();
				br.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	private View.OnClickListener launcher = new View.OnClickListener() {
		public void onClick(View v) {
			launchGame(buttons.get(v.getId()));
		}
	};
	*/
	
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
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
