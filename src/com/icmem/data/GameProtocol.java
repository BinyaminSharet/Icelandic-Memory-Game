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

/**
 * @author bsharet
 * This class contains the constant keys of the UPDATE API.
 * {
 *	"chunk_number" : <i - number>
 *	"total" : <i - total chunk count >
 *	"games" : 
 *	[
 *		{
 *			"id" : <i - ID>,
 *			"Version" : <i - Version of this gameset>
 *			"Action" : <s - optional, one of the following actions:
 *					new, update, delete >
 *			"Title" : <s - Display name of the current game>,
 *			"Description" : <s - optional, Description of the game>,
 *			"Pairs" : {
 *				<s - first word> : <s - matching word>,
 *			}
 *		},
 *	]
 *	
 * }
 */
public final class GameProtocol {
	final public static String J_ID_GAMES_ARRAY = "games";
	final public static String J_ID_GAME_ID = "id";
	final public static String J_ID_GAME_VERSION = "version";
	final public static String J_ID_GAME_TITLE = "title";
	final public static String J_ID_GAME_DESC = "description";
	final public static String J_ID_GAME_OPERATION = "operation";
	final public static String J_ID_GAME_PAIRS = "pairs";
	final public static String J_ID_GAME_PAIR_FIRST = "1";
	final public static String J_ID_GAME_PAIR_SECOND = "2";
	final public static String J_ID_CHUNK_NUMBER = "chunk_number";
	final public static String J_ID_CHUNK_TOTAL = "total";
	final public static String OPERATION_DELETE = "delete";
	final public static String OPERATION_UPDATE = "update";
	final public static String OPERATION_NEW = "new";
}
