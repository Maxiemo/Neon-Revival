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

import java.io.IOException;
import java.util.EventObject;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.script.*;

import com.google.common.eventbus.EventBus;

import neon.core.event.*;
import neon.core.handlers.CombatHandler;
import neon.core.handlers.DeathHandler;
import neon.core.handlers.InventoryHandler;
import neon.core.handlers.MagicHandler;
import neon.core.handlers.PhysicsHandler;
import neon.core.handlers.TurnHandler;
import neon.entities.Player;
import neon.narrative.EventAdapter;
import neon.narrative.QuestTracker;
import neon.resources.CServer;
import neon.resources.ResourceManager;
import neon.resources.builder.IniBuilder;
import neon.systems.physics.Dyn4jPhysics;
import neon.systems.physics.PhysicsSystem;
import neon.systems.timing.Timer;
import neon.systems.files.FileSystem;
import neon.systems.io.Port;

/**
 * The engine class is the core of the neon roguelike engine. It keeps track of 
 * all game elements.
 * 
 * @author mdriesen
 */
public class Engine {
	// wordt door engine geïnitialiseerd
	// TODO: alle global static state wegwerken
	private static ScriptEngine engine;	
	private static Logger logger;
	private static QuestTracker quests;	
	private static EventBus bus;	// event bus
	private static ResourceManager resources;
	
	private FileSystem files;		// virtual file system
	private PhysicsSystem physics;	// de physics engine
	private TaskQueue queue;

	// wordt extern geset
	private static Game game;
	
	/**
	 * Initializes the engine. 
	 */
	public Engine(Port port) {
		bus = port.getBus();
		engine = new ScriptEngineManager().getEngineByName("JavaScript");
		files = new FileSystem();
		queue = new TaskQueue();
		initLog();
		resources = new ResourceManager(new IniBuilder("neon.ini", files, queue));
		physics = new Dyn4jPhysics();
		physics.addListener(new PhysicsHandler(resources));
		quests = new QuestTracker(engine);
	}
	
	/**
	 * This method is the run method of the gamethread. It sets up the event
	 * system.
	 */
	public void run() {
		EventAdapter adapter = new EventAdapter(quests);
		bus.register(queue);
		bus.register(new CombatHandler());	
		bus.register(new DeathHandler(engine));
		bus.register(new InventoryHandler());
		bus.register(adapter);
		bus.register(quests);
		bus.register(new GameLoader(this));
		bus.register(new GameSaver(queue, files));
		bus.register(new TurnHandler((CServer)resources.getResource("ini", "config"), this));
	}
	
	private void initLog() {
		CServer config = new CServer("neon.ini");
		logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		logger.setLevel(Level.parse(config.getLogLevel()));
		try {
			Handler handler = new FileHandler("neon.log");
			handler.setFormatter(new SimpleFormatter());
			logger.addHandler(handler);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Convenience method to post an event to the event bus.
	 * 
	 * @param message
	 */
	public static void post(EventObject message) {
		bus.post(message);
	}
	
/*
 * alle scriptbrol
 */
	/**
	 * Executes a script.
	 * 
	 * @param script	the script to execute
	 * @return			the result of the script
	 */
	public static Object execute(String script) {
		try {
			return engine.eval(script);
		} catch(Exception e) {
			return null;	// niet geweldig goed
		}
	}
	
/*
 * alle getters
 */
	/**
	 * @return	the player
	 */
	public static Player getPlayer() {
		return game.getPlayer();
	}
	
	public static QuestTracker getQuestTracker() {
		return quests;
	}
	
	/**
	 * @return	the timer
	 */
	public static Timer getTimer() {
		return game.getTimer();
	}
	
	/**
	 * @return	the virtual filesystem of the engine
	 */
	public FileSystem getFileSystem() {
		return files;
	}
	
	/**
	 * @return	the physics engine
	 */
	public PhysicsSystem getPhysicsEngine() {
		return physics;
	}
	
	/**
	 * @return	the script engine
	 */
	public static ScriptEngine getScriptEngine() {
		return engine;
	}
	
	/**
	 * @return	the {@code Logger}
	 */
	public static Logger getLogger() {
		return logger;
	}
	
	/**
	 * Returns all configuration and other data concerning the currently
	 * loaded game.
	 * 
	 * @return	the current {@code Game}
	 */
	public static Game getGame() {
		return game;
	}
	
	public static ResourceManager getResources() {
		return resources;
	}

	/**
	 * @return	the {@code TaskQueue}
	 */
	public TaskQueue getQueue() {
		return queue;
	}
	
	/**
	 * Starts a new game.
	 */
	public void startGame(Game game) {
		Engine.game = game;

		// ontbrekende systemen opzetten
		bus.register(new MagicHandler(queue, game));
		
		// player registreren
		Player player = game.getPlayer();
		engine.put("journal", player.getJournal());	
		engine.put("player", player);
		engine.put("PC", player);
	}
	
	/**
	 * quit the game
	 */
	public static void quit() {
		System.exit(0);
	}
}
