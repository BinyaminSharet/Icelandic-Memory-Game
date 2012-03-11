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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.icmem.data.DataManager;

public class BestResultsAdapter extends ArrayAdapter<String> {
	
	final private Activity context;
	private List<String> lgames;
	private List<String> lnames;
	private List<Integer> ltimes;
	
	public BestResultsAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		this.context = (Activity) context;
		fillLists();
	}
	
	@SuppressWarnings("unchecked")
	private void fillLists() {
		Map<Integer, List<?>> allHighScores = DataManager.getDataManager().getAllHighScores();
		if (allHighScores != null) {
			lgames = (List<String>)allHighScores.get(DataManager.HIGH_SCORE_TITLE_ID);
			lnames = (List<String>)allHighScores.get(DataManager.HIGH_SCORE_USER_ID);
			ltimes = (List<Integer>)allHighScores.get(DataManager.HIGH_SCORE_TIME_ID);
		}
		else {
			lgames = Arrays.asList( "No games played yet");
			lnames = Arrays.asList( "");
			ltimes = Arrays.asList( 0 );
		}
	}
	
	@Override
	public int getCount() {
		return ltimes.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.best_results_list_layout, null, true);
		TextView tv_user = (TextView)rowView.findViewById(R.id.best_results_user_name);
		TextView tv_game = (TextView)rowView.findViewById(R.id.best_results_game_name);
		TextView tv_time = (TextView)rowView.findViewById(R.id.best_results_time);
		tv_game.setText(lgames.get(position));
		tv_user.setText(lnames.get(position));
		tv_time.setText("\t"+Util.getTimeRepresentation(ltimes.get(position)));
		return rowView;
	}
}
