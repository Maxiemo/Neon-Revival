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

package neon.ui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.stage.Stage;
import neon.core.Engine;
import neon.core.event.LoadEvent;
import neon.core.event.MagicEvent;
import neon.core.event.MessageEvent;
import neon.core.handlers.MagicHandler;
import neon.entities.Player;
import neon.resources.CClient;
import neon.systems.io.Port;
import neon.ui.states.*;
import neon.util.fsm.FiniteStateMachine;
import neon.util.fsm.Transition;
import neon.util.fsm.TransitionEvent;

public class Client {
	private UserInterface ui;
	private final FiniteStateMachine fsm;
	private final EventBus bus;
	private final String version;
	private CClient config;
	
	public Client(Port port, String version) {
		bus = port.getBus();
		this.version = version;
		fsm = new FiniteStateMachine();
		bus.register(new BusAdapter());
	}
	
	public void run(Stage stage) {
		config = (CClient)Engine.getResources().getResource("client", "config");
		// UI dingen aanmaken (toont ook stage gecentreerd op het scherm)
		ui = new UserInterface(config.getTitle(), stage);
		initFSM();
	}
	
	private void initFSM() {
		// eerst alle states aanmaken
		MainMenuState main = new MainMenuState(fsm, bus, ui, version, config);

		// game state en alle game substates
		GameState game = new GameState(fsm, bus, ui, config);
		bus.register(game);
		DoorState doors = new DoorState(game, bus, ui);
		LockState locks = new LockState(game, bus, ui);
		BumpState bump = new BumpState(game, bus, ui);
		MoveState move = new MoveState(game, bus, config);
		AimState aim = new AimState(game, bus, ui, config);

		// andere states
		DialogState dialog = new DialogState(fsm, bus, ui, config);
		InventoryState inventory = new InventoryState(fsm, bus, ui, config);
		ContainerState container = new ContainerState(fsm, bus, ui, config);
		JournalState journal = new JournalState(fsm, bus, ui);
		MapState map = new MapState(fsm, bus, ui);
		
		// start states setten
		fsm.addStartStates(main, move);
		
		// states verbinden met transitions
		fsm.addTransition(new Transition(main, game, "start"));
		fsm.addTransition(new Transition(journal, game, "cancel"));
		fsm.addTransition(new Transition(game, journal, "journal"));
		fsm.addTransition(new Transition(inventory, game, "cancel"));
		fsm.addTransition(new Transition(game, inventory, "inventory"));
		fsm.addTransition(new Transition(aim, move, "return"));
		fsm.addTransition(new Transition(move, aim, "aim"));
		fsm.addTransition(new Transition(aim, dialog, "dialog"));
		fsm.addTransition(new Transition(dialog, game, "return"));
		fsm.addTransition(new Transition(move, doors, "door"));
		fsm.addTransition(new Transition(aim, doors, "door"));
		fsm.addTransition(new Transition(doors, move, "return"));
		fsm.addTransition(new Transition(move, locks, "lock"));
		fsm.addTransition(new Transition(locks, move, "return"));
		fsm.addTransition(new Transition(game, container, "container"));
		fsm.addTransition(new Transition(container, game, "return"));
		fsm.addTransition(new Transition(dialog, game, "return"));
		fsm.addTransition(new Transition(move, bump, "bump"));
		fsm.addTransition(new Transition(bump, move, "return"));
		fsm.addTransition(new Transition(bump, dialog, "dialog"));
		fsm.addTransition(new Transition(game, map, "map"));
		fsm.addTransition(new Transition(map, game, "return"));
		
		// en starten
		fsm.start(new TransitionEvent("start"));
	}
	
	private class BusAdapter {
		@Subscribe public void transition(TransitionEvent te) {
			fsm.transition(te);
		}

		@Subscribe public void message(MessageEvent me) {
			ui.showMessage(me.toString(), me.getDuration());
		}
		
		@Subscribe public void load(LoadEvent.Done le) {
			fsm.transition(new TransitionEvent("start"));
		}
		
		@Subscribe public void result(MagicEvent.Result me) {
			if(me.getCaster() instanceof Player) {
				switch(me.getResult()) {
				case MagicHandler.MANA: ui.showMessage("Not enough mana to cast this spell.", 1); break;
				case MagicHandler.RANGE: ui.showMessage("Target out of range.", 1); break;
				case MagicHandler.NONE: ui.showMessage("No spell equiped.", 1); break;
				case MagicHandler.SKILL: ui.showMessage("Casting failed.", 1); break;
				case MagicHandler.OK: ui.showMessage("Spell cast.", 1); break;
				case MagicHandler.NULL: ui.showMessage("No target selected.", 1); break;
				case MagicHandler.LEVEL: ui.showMessage("Spell is too difficult to cast.", 1); break;
				case MagicHandler.SILENCED: ui.showMessage("You are silenced", 1); break;
				case MagicHandler.INTERVAL: ui.showMessage("Can't cast this power yet.", 1); break;
				}
			}
		}
	}
}
