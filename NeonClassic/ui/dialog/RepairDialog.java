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

package neon.ui.dialog;

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

import neon.core.Engine;
import neon.entities.Armor;
import neon.entities.Creature;
import neon.entities.Item;
import neon.entities.Player;
import neon.entities.Weapon;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;

public class RepairDialog {
	@FXML private Button cancelButton;
	@FXML private ListView<Item> itemList;
	
	private Player player;
	private UserInterface ui;
	private Stage stage;
	
	public RepairDialog(UserInterface ui) {
		this.ui = ui;
				
		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("repair.fxml"));
		loader.setController(this);
		
		try {
	        Scene scene = new Scene(loader.load());
	        scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
	        scene.setFill(null);
			stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}

        itemList.setOnKeyPressed(new KeyHandler());
		cancelButton.setOnAction(event -> stage.close());
	}
	
	public void show(Player player, Creature repairer) {
		this.player = player;
		initItems();
		ClientUtils.centerStage(stage, ui);
		stage.show();
	}
	
	private class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch(ke.getCode()) {
			case SPACE:
				repair();
				break;
			case ESCAPE:
				stage.close();
				break;
			default:
				break;
			}
		}
	}
	
	@FXML private void repair() {
		try {
			Item item = itemList.getSelectionModel().getSelectedItem();
			if(item instanceof Armor) {
				((Armor)item).setState(100);
			} else if(item instanceof Weapon) {
				((Weapon)item).setState(100);					
			}
			initItems();
			ui.showMessage("Item repaired.", 2);
		} catch (ArrayIndexOutOfBoundsException f) {
			ui.showMessage("No item selected.", 2);
		}
		
	}

	private void initItems() {
		ObservableList<Item> items = itemList.getItems();
		items.clear();
		for(long uid : player.getInventoryComponent()) {
			Item item = (Item)Engine.getGame().getEntity(uid);
			if(item instanceof Weapon && ((Weapon)item).getState() < 100) {
				items.add(item);
			} else if(item instanceof Armor && ((Armor)item).getState() < 100) {
				items.add(item);				
			}
		}
	}
}
