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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.icmem.data.DataManager;

public class BoardGridAdapter extends BaseAdapter {

	private final Context context;
	private List<MemoryButton> buttons;
	private MemoryButton clicked = null;
	private MemoryListener listener = new MemoryListener();

	private ColorScheme colors= new ColorScheme();
	Handler finishHandler;

	public BoardGridAdapter(Context context) {
		this.context = context;
	}

	private void setColors() {
		colors.back_color	= 0xFFFF0000;
		colors.front_color	= 0xFFFFFFFF;
		colors.match_color	= 0xFF0000FF;
	}
	
	public void init(Handler finishHandler, int gId) {
		this.finishHandler = finishHandler;
		setColors();

		Map<String, String> wordsMap = DataManager.getDataManager().getWordsForGame(gId);
		/*	Now choose up to 12 pairs from the map and put them in a list	*/
		List<Map.Entry<String, String>> wordsList = 
			new ArrayList<Map.Entry<String, String>>(wordsMap.entrySet());
		Collections.shuffle(wordsList);
		wordsList = wordsList.subList(0, Math.min(12, wordsList.size()));
		buttons = new ArrayList<MemoryButton>(wordsList.size() * 2);
		for (Map.Entry<String, String> e : wordsList) {
			MemoryButton bk = new MemoryButton(context, e.getKey(), e.getValue());
			MemoryButton bv = new MemoryButton(context, e.getValue(), e.getKey());
			bk.setOnClickListener(listener);
			bv.setOnClickListener(listener);
			bk.setBackgroundColor(colors.back_color);
			bv.setBackgroundColor(colors.back_color);
			buttons.add(bk);
			buttons.add(bv);
		}

		Collections.shuffle(buttons);


	}

	@Override
	public int getCount() {
		return buttons.size();
	}

	@Override
	public Object getItem(int position) {
		return buttons.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MemoryButton b;
		if (convertView == null) {
			b = buttons.get(position);
			b.setLayoutParams(new GridView.LayoutParams(70, 70));
			b.setPadding(2,2,2,2);
		}
		else {
			b = (MemoryButton)convertView;
		}
		return b;
	}

	private class MemoryButton extends Button {
		final protected String word, matchingWord;
		private boolean found = false;
		public MemoryButton(Context context, String word, String matchingWord) {
			super(context);
			this.word = word;
			this.matchingWord = matchingWord;
		}
		public void matchFound() {
			found = true;
		}
		public boolean isFound() {
			return found;
		}
	}

	private class MemoryListener implements View.OnClickListener {

		private void setAllButtonsState(boolean enabled) {
			for (MemoryButton b : buttons) {
				b.setEnabled(enabled);
				if (!enabled)
				    b.setTextColor(0xFF000000);
			}
		}
		@Override
		public void onClick(View v) {
			final MemoryButton mb = (MemoryButton)v;
			if (!mb.isFound()) {
				mb.setText(mb.word);
				mb.setBackgroundColor(colors.front_color);
				if (clicked != null) { // another button is pressed
					if (clicked.matchingWord.equalsIgnoreCase(mb.word)) { // this is a match
						mb.matchFound();
						clicked.matchFound();
						mb.setBackgroundColor(colors.match_color);
						clicked.setBackgroundColor(colors.match_color);
						clicked = null;
						checkIfDone();
					}
					else { // no match
						this.setAllButtonsState(false);
						Handler handler = new Handler();
						Runnable mRun = new Runnable(){
							@Override
							public void run() {
								mb.setText("");
								clicked.setText("");
								mb.setBackgroundColor(colors.back_color);
								clicked.setBackgroundColor(colors.back_color);
								clicked = null;
								setAllButtonsState(true);
							}							
						};
						handler.postDelayed(mRun, 500);						
					}
				} else {
					mb.setText(mb.word);
					clicked = mb;
					mb.setBackgroundColor(colors.front_color);
				}
			}
		}
		private void checkIfDone() {
			boolean done = true;
			for (MemoryButton b : buttons) {
				if (!b.isFound()) {
					done = false;
				}
			}
			if (done){
				finishHandler.sendMessage(Message.obtain());
			}
		}

	}
}
