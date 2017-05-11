/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2012-2015 - Maarten Driesen
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

package neon.ui.dialog;

import java.awt.Rectangle;
import java.io.IOException;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.jdom2.Element;

import com.google.common.eventbus.EventBus;

import neon.core.Engine;
import neon.entities.Creature;
import neon.entities.Player;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;
import neon.ui.custom.TravelCellRenderer;
import neon.util.fsm.TransitionEvent;
import neon.resources.RPerson;

public class TravelDialog {
	@FXML private Button cancelButton;
	@FXML private ListView<Destination> destinationList;
	
	private Player player;
	private Creature agent;	// uw reisagent
	private EventBus bus;
	private UserInterface ui;
	private Stage stage;
	
	public TravelDialog(UserInterface ui, EventBus bus) {
		this.bus = bus;
		this.ui = ui;

		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("travel.fxml"));
		loader.setController(this);
		
		try {
	        Scene scene = new Scene(loader.load());
	        scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
	        scene.setFill(null);
			stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}

        destinationList.setCellFactory(new TravelCellRenderer());
        destinationList.setOnKeyPressed(new KeyHandler());
		cancelButton.setOnAction(event -> stage.close());
	}
	
	public void show(Player player, Creature agent) {
		this.agent = agent;
		this.player = player;
		initDestinations();

		ClientUtils.centerStage(stage, ui);
		stage.show();
	}
	
	private class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch(ke.getCode()) {
			case SPACE:
				travel();
				break;
			case ESCAPE:
				stage.close();
				break;
			default:
				break;
			}
		}
	}
	
	private void initDestinations() {
		ObservableList<Destination> destinations = destinationList.getItems();
		for(Element e : ((RPerson)Engine.getResources().getResource(agent.getID())).services) {
			if(e.getAttributeValue("id").equals("travel")) {
				for(Element dest : e.getChildren()) {
					int x = Integer.parseInt(dest.getAttributeValue("x"));
					int y = Integer.parseInt(dest.getAttributeValue("y"));
					int cost = Integer.parseInt(dest.getAttributeValue("cost"));
					destinations.add(new Destination(dest.getAttributeValue("name"), x, y, cost));
				}
			}
		}
	}
	
	@FXML private void travel() {
		try {
			Destination destination = destinationList.getSelectionModel().getSelectedItem();
			if(player.getInventoryComponent().getMoney() >= destination.cost) {
				player.getInventoryComponent().addMoney(-destination.cost);
				stage.close();						
				Rectangle bounds = player.getShapeComponent();
				bounds.setLocation(destination.x, destination.y);
				bus.post(new TransitionEvent("return"));
			} else {
				ui.showMessage("You don't have enough money to go there.", 2);
			}
		} catch (ArrayIndexOutOfBoundsException f) {
			ui.showMessage("No destination selected.", 2);
		}
	}
	
	public class Destination {
		public int x, y, cost;
		public String name;
		
		public Destination(String name, int x, int y, int cost) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.cost = cost;
		}
	}
}
