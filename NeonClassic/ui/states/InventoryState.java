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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.awt.Rectangle;
import java.util.*;
import java.io.IOException;

import com.google.common.eventbus.EventBus;

import neon.core.Engine;
import neon.core.handlers.InventoryHandler;
import neon.core.handlers.MagicHandler;
import neon.core.handlers.SkillHandler;
import neon.entities.Item;
import neon.entities.Player;
import neon.entities.components.HealthComponent;
import neon.entities.property.Skill;
import neon.resources.CClient;
import neon.resources.RItem;
import neon.resources.RText;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;
import neon.ui.custom.DescriptionPane;
import neon.ui.custom.InventoryCellRenderer;
import neon.ui.dialog.BookDialog;
import neon.util.fsm.TransitionEvent;
import neon.util.fsm.State;

public class InventoryState extends State {
	@FXML private Label infoLabel;
	@FXML private DescriptionPane description;
	@FXML private ListView<Item> inventoryList;
	@FXML private Button useButton, dropButton, quitButton;
	
	private HashMap<String, Integer> listData = new HashMap<String, Integer>();
	private Player player;
	private EventBus bus;
	private UserInterface ui;
	private Scene scene;
	private String coin;
	
	public InventoryState(State parent, EventBus bus, UserInterface ui, CClient config) {
		super(parent, "inventory module");
		this.bus = bus;
		this.ui = ui;
		coin = config.getSmall();
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("inventory.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		inventoryList.getSelectionModel().selectedItemProperty().addListener(new SelectionListener());
		inventoryList.setOnMouseClicked(new MouseListener());
		inventoryList.setOnKeyPressed(new KeyListener());
        inventoryList.setCellFactory(new InventoryCellRenderer(listData));
		
    	quitButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
    	useButton.setOnAction(event -> use(inventoryList.getSelectionModel().getSelectedItem()));
    	dropButton.setOnAction(event -> drop(inventoryList.getSelectionModel().getSelectedItem()));    	
	}
	
	@Override
	public void enter(TransitionEvent t) {
		player = (Player)getVariable("player");
		initList();
		ui.showScene(scene);
	}

	private void drop(Item item) {
		if(item == null) {
			ui.showMessage("There is nothing left to drop.", 2);			
		} else {
			InventoryHandler.removeItem(player, item);
			Rectangle pBounds = player.getShapeComponent();
			Rectangle iBounds = item.getShapeComponent();
			iBounds.setLocation(pBounds.x, pBounds.y);
			Engine.getGame().getAtlas().getCurrentZone().addItem(item);
			initList();	
		}
	}
	
	private void use(Item item) {
		if(item == null) {
			ui.showMessage("There is nothing left to use/eat/(un)equip.", 2);
		} else if(item instanceof Item.Potion) {
			InventoryHandler.removeItem(player, item);
			MagicHandler.drink(player, (Item.Potion)item);
			initList();
		} else if(item instanceof Item.Book && !(item instanceof Item.Scroll)) {
			RText text = (RText)Engine.getResources().getResource(((RItem.Text)item.resource).content + ".html", "text");
			new BookDialog(ui).show(item.toString(), text.getText());
		} else if(item instanceof Item.Food) {
			InventoryHandler.removeItem(player, item);
			MagicHandler.eat(player, (Item.Food)item);
			initList();
		} else if(item instanceof Item.Aid) {
			InventoryHandler.removeItem(player, item);
			HealthComponent health = player.getHealthComponent();
			health.heal(SkillHandler.check(player, Skill.MEDICAL)/5f);
			initList();
		} else if(!player.getInventoryComponent().hasEquiped(item.getUID())) {
			InventoryHandler.equip(item, player);
			refreshListView();
		} else {
			InventoryHandler.unequip(item, player);
			refreshListView();
		}
	}
	
	/*
	 * Zeer vieze manier om de listview te refreshen.
	 */
	private void refreshListView() {
		int selected = inventoryList.getSelectionModel().getSelectedIndex();
		ObservableList<Item> buffer = inventoryList.getItems();
		inventoryList.setItems(null);
		inventoryList.setItems(buffer);
		inventoryList.getSelectionModel().select(selected);
	}
	
	private void initList() {
		ObservableList<Item> buffer = inventoryList.getItems();
		buffer.clear();
		listData.clear();
		
		for(long uid : player.getInventoryComponent()) {
			Item i = (Item)Engine.getGame().getEntity(uid);
			if(!listData.containsKey(i.getID())) {
				listData.put(i.getID(), 1);
				buffer.add(i);
			} else {
				listData.put(i.getID(), listData.get(i.getID()) + 1);
			}
		}

		CClient ini = (CClient)Engine.getResources().getResource("client", "config");
		infoLabel.setText("Weight: " + InventoryHandler.getWeight(player) + " kg. " + 
				ClientUtils.moneyString(player, ini.getBig(), ini.getSmall()));
    	inventoryList.getSelectionModel().select(0);
	}
		
	private class MouseListener implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent me) {
			if(me.getClickCount() == 2) {
				use(inventoryList.getSelectionModel().getSelectedItem());
			}
		}
	}

	private class SelectionListener implements ChangeListener<Item> {
		@Override
		public void changed(ObservableValue<? extends Item> observable, Item oldItem, Item newItem) {	
			description.update(newItem, coin);
		}		
	}
	
	// key handler voor de listview
	private class KeyListener implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			Item item = inventoryList.getSelectionModel().getSelectedItem();
			
			switch (ke.getCode()) {
			case ESCAPE:
			case CONTROL:
				bus.post(new TransitionEvent("cancel"));
				break;
			case SPACE:
				use(item); 					
				break;
			case DELETE:
				drop(item);
				break;
			default:
				break;
			}
		}
	}
}
