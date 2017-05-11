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

import java.io.IOException;
import java.util.Collection;

import com.google.common.eventbus.EventBus;

import neon.core.event.StoreEvent;
import neon.core.Engine;
import neon.core.handlers.InventoryHandler;
import neon.entities.Creature;
import neon.entities.EntityFactory;
import neon.entities.Item;
import neon.entities.Player;
import neon.resources.RCraft;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;
import neon.ui.custom.CraftCellRenderer;

public class CrafterDialog {
	@FXML private Button cancelButton;
	@FXML private ListView<RCraft> itemList;
	
	private Player player;
	private EventBus bus;
	private UserInterface ui;
	private Stage stage;
	
	public CrafterDialog(UserInterface ui, String coin, EventBus bus) {
		this.ui = ui;
		this.bus = bus;
		
		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("crafting.fxml"));
		loader.setController(this);

		try {
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
			scene.setFill(null);
			stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        itemList.setCellFactory(new CraftCellRenderer(coin));
		itemList.setOnKeyPressed(new KeyHandler());
		cancelButton.setOnAction(event -> stage.close());
	}
	
	public void show(Player player, Creature crafter) {
		this.player = player;
		initItems();
		ClientUtils.centerStage(stage, ui);
		stage.show();
	}
	
	@FXML private void craft() {
		try {
			RCraft craft = itemList.getSelectionModel().getSelectedItem();
			if(player.getInventoryComponent().getMoney() >= craft.cost) {
				Collection<Long> removed = InventoryHandler.removeItems(player, craft.raw, craft.amount);
				for(long uid : removed) {	// gebruikte items verwijderen
					bus.post(new StoreEvent(this, uid));
				}
				Item item = EntityFactory.getItem(craft.result, Engine.getGame().getStore().createNewEntityUID());
				bus.post(new StoreEvent(this, item));
				player.getInventoryComponent().addItem(item.getUID());
				player.getInventoryComponent().addMoney(-craft.cost);
				ui.showMessage("Item crafted.", 2);
				initItems();
			} else {
				ui.showMessage("You don't have enough money.", 2);
			}
		} catch (ArrayIndexOutOfBoundsException f) {
			ui.showMessage("No item selected.", 2);
		}
		
	}
	
	private class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch(ke.getCode()) {
			case SPACE:
				craft();
				break;
			case ESCAPE:
				stage.close();
				break;
			default:
				break;
			}
		}
	}
	
	private void initItems() {
		ObservableList<RCraft> items = itemList.getItems();
		items.clear();		
		for(RCraft thing : Engine.getResources().getResources(RCraft.class)) {
			if(InventoryHandler.getAmount(player, thing.raw) >= thing.amount) {
				items.add(thing);
			}
		}
	}
}
