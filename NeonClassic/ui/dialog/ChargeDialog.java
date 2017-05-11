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

import neon.entities.Item;
import neon.entities.Player;
import neon.entities.components.Enchantment;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;
import neon.core.Engine;

public class ChargeDialog {
	@FXML private ListView<Item> itemList;
	@FXML private Button cancelButton;
	
	private Player player;
	private UserInterface ui;
	private Stage stage;
	
	public ChargeDialog(UserInterface ui) {
		this.ui = ui;
		
		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("charge.fxml"));
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
	
	public void show(Player player) {
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
				charge();
				break;
			case ESCAPE:
				stage.close();
				break;
			default:
				break;
			}
		}
	}
	

	@FXML private void charge() {
		try {
			Item item = itemList.getSelectionModel().getSelectedItem();
			item.getMagicComponent().setModifier(0);
			ui.showMessage("Item charged.", 2);
		} catch (ArrayIndexOutOfBoundsException f) {
			ui.showMessage("No item selected.", 2);
		}
	}

	private void initItems() {
		ObservableList<Item> items = itemList.getItems();
		items.clear();		
		for(long uid : player.getInventoryComponent()) {
			Item item = (Item)Engine.getGame().getEntity(uid);
			Enchantment enchantment = item.getMagicComponent();
			if(enchantment != null && enchantment.getMana() < enchantment.getBaseMana()) {
				items.add(item);
			}
		}
	}
}
