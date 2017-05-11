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
import neon.core.UIDStore;
import neon.core.handlers.InventoryHandler;
import neon.entities.Creature;
import neon.entities.EntityFactory;
import neon.entities.Item;
import neon.entities.Player;
import neon.resources.RRecipe;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;
import neon.ui.custom.PotionCellRenderer;

public class PotionDialog {
	@FXML private ListView<RRecipe> recipeList;
	@FXML private Button cancelButton;
	
	private Player player;
	private UserInterface ui;
	private Stage stage;
	private UIDStore store;
	
	public PotionDialog(UserInterface ui, String coin) {
		this.ui = ui;
		
		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("potion.fxml"));
		loader.setController(this);

		try {
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
			scene.setFill(null);
			stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}

        recipeList.setCellFactory(new PotionCellRenderer(coin));
		recipeList.setOnKeyPressed(new KeyHandler());
		cancelButton.setOnAction(event -> stage.close());
	}
	
	public void show(Player player, Creature mixer) {
		this.player = player;
		store = Engine.getGame().getStore();
		initPotions();
		ClientUtils.centerStage(stage, ui);
		stage.show();
	}
	
	@FXML private void brew() {
		try {
			RRecipe potion = recipeList.getSelectionModel().getSelectedItem();
			if(player.getInventoryComponent().getMoney() >= potion.cost) {
				for(String item : potion.ingredients) {
					long uid = removeItem(player, item);
					store.removeEntity(uid);
				}
				Item item = EntityFactory.getItem(potion.toString(), store.createNewEntityUID());
				store.addEntity(item);
				player.getInventoryComponent().addItem(item.getUID());
				player.getInventoryComponent().addMoney(-potion.cost);
				initPotions();
				ui.showMessage("Potion created.", 2);
			} else {
				ui.showMessage("You don't have enough money.", 2);
			}
		} catch (ArrayIndexOutOfBoundsException f) {
			ui.showMessage("No potion selected.", 2);
		}		
	}
	
	private void initPotions() {
		ObservableList<RRecipe> recipes = recipeList.getItems();
		recipes.clear();
		for(RRecipe recipe : Engine.getResources().getResources(RRecipe.class)) {
			boolean ok = true;
			for(String item : recipe.ingredients) {
				if(!hasItem(player, item)) {
					ok = false;
				}
			}
			if(ok) {
				recipes.add(recipe);					
			}
		}
	}
	
	private class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch(ke.getCode()) {
			case SPACE:
				brew();
				break;
			case ESCAPE:
				stage.close();
				break;
			default:
				break;
			}
		}
	}
	
	private boolean hasItem(Creature creature, String item) {
		for(long uid : creature.getInventoryComponent()) {
			if(store.getEntity(uid).getID().equals(item)) {
				return true;
			}
		}
		return false;
	}

	private long removeItem(Creature creature, String id) {
		for(long uid : creature.getInventoryComponent()) {
			Item item = (Item)store.getEntity(uid);
			if(item.getID().equals(id)) {
				InventoryHandler.removeItem(creature, item);
				return uid;
			}
		}
		return 0;
	}
}
