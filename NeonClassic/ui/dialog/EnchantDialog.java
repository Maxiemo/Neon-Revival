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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import neon.entities.Clothing;
import neon.entities.Creature;
import neon.entities.Item;
import neon.entities.Player;
import neon.entities.Weapon;
import neon.entities.components.Enchantment;
import neon.magic.Effect;
import neon.resources.RSpell;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;

public class EnchantDialog {
	@FXML private ListView<Item> itemList;
	@FXML private ListView<Effect> spellList;
	@FXML private Button cancelButton;
	
	private UserInterface ui;
	private Stage stage;
	
	public EnchantDialog(UserInterface ui) {
		this.ui = ui;
		
		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("enchant.fxml"));
		loader.setController(this);

		try {
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
			scene.setFill(null);
			stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		itemList.getSelectionModel().selectedItemProperty().addListener(new SelectionListener());
		itemList.setOnKeyPressed(new KeyHandler());
		spellList.setOnKeyPressed(new KeyHandler());
		cancelButton.setOnAction(event -> stage.close());
	}
	
	public void show(Player player, Creature enchanter) {
		initLists();
		ClientUtils.centerStage(stage, ui);
		stage.show();
	}
	
	public void initLists() {
		ObservableList<Item> items = itemList.getItems();
		items.clear();
		ObservableList<Effect> effects = spellList.getItems();
		effects.clear();
		
		for(Long uid : Engine.getPlayer().getInventoryComponent()) {
			Item item = (Item)Engine.getGame().getEntity(uid);
			if((item instanceof Weapon || item instanceof Clothing) && 
					item.getMagicComponent() == null) {
				items.add(item);
			}
		}
	}
	
	@FXML private void enchant() {
		Effect effect = spellList.getSelectionModel().getSelectedItem();
		Item item = itemList.getSelectionModel().getSelectedItem();
		
		if(effect != null) {
			RSpell spell = new RSpell(effect.toString(), 0, 0, effect.name(), 0, 0, "enchant");
			item.setMagicComponent(new Enchantment(spell, 100, item.getUID()));
			ui.showMessage("Item enchanted.", 2);
			initLists();
		} else {
			ui.showMessage("No enchantment selected!", 2);				
		}		
	}
	
	private class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch(ke.getCode()) {
			case ESCAPE:
				stage.close();
				break;
			case ENTER:
				enchant();
				break;
			default:
				break;
			}
		}
	}
	
	private class SelectionListener implements ChangeListener<Item> {
		@Override
		public void changed(ObservableValue<? extends Item> observable, Item oldItem, Item newItem) {
			ObservableList<Effect> effects = spellList.getItems();
			effects.clear();
			if(newItem instanceof Weapon) {
				for(Effect effect : Effect.values()) {
					if(effect.getHandler().isWeaponEnchantment()) {
						effects.add(effect);
					}
				}
			} else {
				for(Effect effect : Effect.values()) {
					if(effect.getHandler().isClothingEnchantment()) {
						effects.add(effect);
					}
				}
			}
		}
	}
}
