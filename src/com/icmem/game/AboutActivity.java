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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
		TextView tv = (TextView)findViewById(R.id.about_the_game_tv);
		tv.setText(Html.fromHtml(getText()));
	}
	
	private String getText() {
		StringBuilder sb =new StringBuilder(300);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					getAssets().open("About.txt")));
			String l;
			while((l = br.readLine()) != null) {
				sb.append(l);
			}			
		} catch (IOException e) {
			sb.append("Couldn't read file");
		}
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					getAssets().open("AboutIs.txt")));
			String l;
			while((l = br.readLine()) != null) {
				sb.append(l);
			}			
		} catch (IOException e) {
			sb.append("Couldn't read file");
		}
		
		return sb.toString();
	}

}
