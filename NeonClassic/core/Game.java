/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2013-2015 - Maarten Driesen
 * 
 *	This program is free software; you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation; either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neon.core;

import javax.script.ScriptEngine;

import neon.entities.Entity;
import neon.entities.Player;
import neon.maps.Map;
import neon.systems.files.FileSystem;
import neon.systems.physics.PhysicsSystem;
import neon.systems.timing.Timer;

// TODO: game houdt alles bij wat moet gesaved worden
public class Game {
	private final UIDStore store;
	private final Player player;
	private final Atlas atlas;
	private final Timer timer = new Timer();
	private final ScriptEngine engine;
	
	public Game(Player player, FileSystem files, ScriptEngine engine, PhysicsSystem physics) {
		store = new UIDStore("temp/store");
		atlas = new Atlas(files, "temp/atlas", physics);
		this.player = player;
		this.engine = engine;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public UIDStore getStore() {
		return store;
	}
	
	public Atlas getAtlas() {
		return atlas;
	}
	
	public Entity getEntity(long uid) {
		return store.getEntity(uid);
	}
	
	public Timer getTimer() {
		return timer;
	}
	
	public void setMap(Map map) {
		atlas.setMap(map);
		engine.put("map", map);
	}
}
