/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2015 - Maarten Driesen
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

import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import com.google.common.eventbus.EventBus;

import neon.core.Engine;
import neon.ui.UserInterface;
import neon.ui.custom.MapPanel;
import neon.util.fsm.TransitionEvent;
import neon.util.fsm.State;

public class MapState extends State {
	@FXML private SwingNode mapNode;
	@FXML private Button quitButton;
	
	private EventBus bus;
	private UserInterface ui;
	private Scene scene;
	private MapPanel map;
	
	public MapState(State parent, EventBus bus, UserInterface ui) {
		super(parent);
		this.bus = bus;
		this.ui = ui;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("map.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	quitButton.setOnAction(event -> bus.post(new TransitionEvent("return")));
    	mapNode.setOnKeyPressed(new KeyListener());
	}
	
	@Override
	public void enter(TransitionEvent t) {
		map = new MapPanel(Engine.getGame().getAtlas().getCurrentZone());
		mapNode.setContent(map);
		mapNode.requestFocus();
		ui.showScene(scene);		
	}
	
	@FXML private void toggleFill() {
		map.toggleFill();
	}

	private class KeyListener implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch (ke.getCode()) {
			case ESCAPE:
			case M:
				bus.post(new TransitionEvent("return"));
				break;
			case SPACE:
				toggleFill();
				break;
			default:
				break;
			}
		}
	}
}
