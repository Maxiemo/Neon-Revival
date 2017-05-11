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

package neon.ui.states;

import neon.core.Engine;
import neon.core.event.*;
import neon.entities.Player;
import neon.entities.components.HealthComponent;
import neon.entities.property.Attribute;
import neon.resources.CClient;
import neon.ui.*;
import neon.util.fsm.*;

import java.io.InputStream;
import java.util.Scanner;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

public class GameState extends State {
	private Player player;
	private GamePanel panel;
	private EventBus bus;
	private UserInterface ui;
	private Scene scene;
	
	public GameState(State parent, EventBus bus, UserInterface ui, CClient config) {
		super(parent, "game module");
		this.bus = bus;
		this.ui = ui;
		panel = new GamePanel();
		setVariable("panel", panel);	// panel ter beschikking stellen van andere states
		
		StackPane pane = new StackPane();
		SwingNode node = new SwingNode();
		pane.getChildren().add(node);
        node.setContent(panel);
        scene = new Scene(pane);
        scene.setOnKeyPressed(new KeyHandler(config));
       
		// maakt client functies beschikbaar voor scripting:
		Engine.getScriptEngine().put("engine", new ClientScriptInterface(panel));
	}

	
	@Override
	public void enter(TransitionEvent te) {
		ui.showScene(scene);
		if(te.toString().equals("start")) {
			player = Engine.getPlayer();
			setVariable("player", Engine.getPlayer());
			// in geval spel start, moeten de events van de huidige kloktick nu uitgevoerd worden
			bus.post(new TurnEvent(Engine.getTimer().getTime(), panel.getVisibleRectangle(), true));
		}
		panel.repaint();
		
		if(te.getParameter("message") != null) {
			panel.print(te.getParameter("message").toString());	
		}
	}

	private void save(boolean quit) {
		if(quit) {
			if(ui.showQuestion("Do you wish to quit?")) {
				if(ui.showQuestion("Do you wish to save?")) {
					bus.post(new SaveEvent(this));	// XXX: sync
				} 
				Engine.quit();
			} else {
				panel.repaint();
			}
		} else {
			if(ui.showQuestion("Do you wish to save?")) {
				bus.post(new SaveEvent(this));	// XXX: sync
			}
			panel.repaint();
		}
	}

	@Subscribe public void update(UpdateEvent ue) {
		panel.repaint();
	}
	
	@Subscribe public void handleCombat(CombatEvent.Finished ce) {
		if(ce.getDefender() instanceof Player) {
			panel.print("You were attacked!");
		} else if(ce.getAttacker() instanceof Player){
			switch(ce.getResult()) {
			case CombatEvent.DODGE:
				panel.print("The " + ce.getDefender() + " dodges the attack.");
				break;
			case CombatEvent.BLOCK:
				panel.print("The " + ce.getDefender() + " blocks the attack.");
				break;
			case CombatEvent.ATTACK:
				HealthComponent health = ce.getDefender().getHealthComponent();
				panel.print("You strike the " + ce.getDefender() + " (" + health.getHealth() + ").");
				break;
			case CombatEvent.DIE:
				panel.print("You killed the " + ce.getDefender() + ".");
				bus.post(new DeathEvent(ce.getDefender(), Engine.getTimer().getTime()));
				break;
			}
		}
	}

	@Subscribe public void handleSkill(SkillEvent se) {
		if(se.getStat() != Attribute.NONE) {
			panel.print("Stat raised: " + se.getSkill().stat);
		} else if(se.hasLevelled()) {
			panel.print("Level up.");			
		} else {
			panel.print("Skill raised: " + se.getSkill() + ".");
		}
	}
	
	private class KeyHandler implements EventHandler<KeyEvent> {
		private String map, sneak, journal;
		
		public KeyHandler(CClient config) {
	        map = config.map;
	        sneak = config.sneak;
	        journal = config.journal;
		}
		
		@Override
		public void handle(KeyEvent ke) {
			switch(ke.getCode()) {
			case ADD:
			case PLUS:
				panel.zoomIn(); 
				break;
			case SUBTRACT:
			case MINUS:
				panel.zoomOut(); 
				break;
			case ESCAPE: 
				save(true); 
				break;
			case CONTROL: 
				bus.post(new TransitionEvent("inventory"));
				break;
			case F5:
				save(false); 
				break;
			case F1:
				InputStream input = Engine.class.getResourceAsStream("help.html");
				String help = new Scanner(input, "UTF-8").useDelimiter("\\A").next();
				ui.showHelp(help);
				break;
			case F3:
				ui.showConsole(Engine.getScriptEngine());
				break;
			case F2:
				panel.toggleHUD();
				break;
			default:	// vies vies vies, misschien op te lossen met een hashmap<keycode, string>?
				String key = ke.getText();
				if(key.equals(sneak)) {
					player.setSneaking(!player.isSneaking());
					panel.repaint();
				} else if(key.equals(journal)) {
					bus.post(new TransitionEvent("journal"));					
				} else if(key.equals(map)) {
					bus.post(new TransitionEvent("map"));					
				}
				break;
			}
		}
	}
}
