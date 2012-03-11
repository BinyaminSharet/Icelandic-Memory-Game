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

import android.app.Application;

public class MemoryApp extends Application {

	public static final String DBG_STR = "ICEMEM";
	private static MemoryApp app;
	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
	}
	
	public static MemoryApp getApplication() {
		return app;
	}	
}
