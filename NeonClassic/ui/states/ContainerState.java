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

package neon.ui.states;

import neon.core.Atlas;
import neon.core.Engine;
import neon.maps.Zone;
import neon.resources.CClient;
import neon.ui.*;
import neon.ui.custom.DescriptionPane;
import neon.ui.custom.InventoryCellRenderer;
import neon.core.handlers.InventoryHandler;
import neon.core.handlers.MotionHandler;
import neon.entities.Container;
import neon.entities.Creature;
import neon.entities.Door;
import neon.entities.Entity;
import neon.entities.Item;
import neon.entities.Player;
import neon.util.fsm.*;

import java.awt.Rectangle;
import java.util.*;
import java.io.IOException;

import com.google.common.eventbus.EventBus;

import javafx.application.Platform;
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

public class ContainerState extends State {	
	@FXML private Button takeButton, quitButton;
	@FXML private ListView <Item> inventoryList;
	@FXML private ListView <Entity> containerList;
	@FXML private DescriptionPane description;
	@FXML private Label containerLabel;
	
	private Player player;
	private Object container;
	private EventBus bus;
	private UserInterface ui;
	private Scene scene;
	private HashMap<String, Integer> cData, iData;
	private String coin;
	private Atlas atlas;
	
	public ContainerState(State parent, EventBus bus, UserInterface ui, CClient config) {
		super(parent);
		this.bus = bus;
		this.ui = ui;
		coin = config.getSmall();
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("container.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		quitButton.setOnAction(event -> bus.post(new TransitionEvent("return")));
		
		SelectionListener sListener = new SelectionListener();
		MouseListener mListener = new MouseListener();
		KeyListener kListener = new KeyListener();
		
		inventoryList.getSelectionModel().selectedItemProperty().addListener(sListener);
		inventoryList.setOnMouseClicked(mListener);
		inventoryList.setOnKeyPressed(new KeyListener());
		iData = new HashMap<String, Integer>();
        inventoryList.setCellFactory(new InventoryCellRenderer(iData));
        inventoryList.focusedProperty().addListener(new FocusListener(inventoryList));

		containerList.getSelectionModel().selectedItemProperty().addListener(sListener);
		containerList.setOnMouseClicked(mListener);
		containerList.setOnKeyPressed(kListener);
		cData = new HashMap<String, Integer>();
		containerList.focusedProperty().addListener(new FocusListener(containerList));
	}
	
	@Override
	public void enter(TransitionEvent t) {
		atlas = Engine.getGame().getAtlas();
		container = t.getParameter("holder");
		containerLabel.setText(container.toString());
		player = (Player)getVariable("player");
		Platform.runLater(() -> update());
		ui.showScene(scene);
	}

	@FXML private void take() {
		try {
			if(inventoryList.isFocused()) {	// iets dumpen
				Item item = inventoryList.getSelectionModel().getSelectedItem();
				InventoryHandler.removeItem(player, item);
				if(container instanceof Container) {	// verandering registreren
					((Container)container).addItem(item.getUID());
				} else if(container instanceof Zone) {	// itempositie aanpassen
					Rectangle pBounds = player.getShapeComponent();
					Rectangle iBounds = item.getShapeComponent();
					iBounds.setLocation(pBounds.x, pBounds.y);
					atlas.getCurrentZone().addItem(item);
				} else if(container instanceof Creature) {
					InventoryHandler.addItem(((Creature)container), item);
				}
				update();
			} else {	// iets oppakken
				Entity item = containerList.getSelectionModel().getSelectedItem();
				if(item instanceof Container) {
					bus.post(new TransitionEvent("return"));
					bus.post(new TransitionEvent("container", "holder", item));
				} else if(item instanceof Door) {
					MotionHandler.teleport(player, (Door)item);
					bus.post(new TransitionEvent("return"));
				} else if(item instanceof Creature) {
					bus.post(new TransitionEvent("return"));
					bus.post(new TransitionEvent("container", "holder", item));
				} else {
					if(container instanceof Zone) {
						atlas.getCurrentZone().removeItem((Item)item);
					} else if(container instanceof Creature) {
						InventoryHandler.removeItem(((Creature)container), (Item)item);
					} else {
						((Container)container).removeItem(item.getUID());
					}
					InventoryHandler.addItem(player, (Item)item);
					update();
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			if(inventoryList.isFocused()) {
				ui.showMessage("There is nothing left to drop.", 2);
			} else {
				ui.showMessage("There is nothing left to pick up.", 2);
			}
		} 		
	}
	
	private void update() {
		ObservableList<Item> iBuffer = inventoryList.getItems();
		iBuffer.clear();
		iData.clear();
		ObservableList<Entity> cBuffer = containerList.getItems();
		cBuffer.clear();
		cData.clear();

		ArrayList<Object> items = new ArrayList<Object>();
		if(container instanceof Zone) {
			Zone zone = (Zone)container;
			Rectangle bounds = player.getShapeComponent();
			items.addAll(zone.getItems(bounds.getLocation()));
		} else if(container instanceof Creature) {
			items.addAll(((Creature)container).getInventoryComponent().getItems());
		} else {
			items.addAll(((Container)container).getItems());
		}
		
		for(Object o : items) {
			Item item = null;
			if(o instanceof Item) {
				item = (Item)o;				
			} else {
				item = (Item)Engine.getGame().getEntity((Long)o);
			}
			if(!cData.containsKey(item.getID())) {
				cData.put(item.getID(), 1);
				cBuffer.add(item);
			} else {
				cData.put(item.getID(), cData.get(item.getID()) + 1);
			}
		}

		for(long uid : player.getInventoryComponent()) {
			Item i = (Item)Engine.getGame().getEntity(uid);
			if(!iData.containsKey(i.getID())) {
				iData.put(i.getID(), 1);
				iBuffer.add(i);
			} else {
				iData.put(i.getID(), iData.get(i.getID()) + 1);
			}
		}
	}

	/*
	 * Item droppen of oppakken bij dubbelklikken.
	 */
	private class MouseListener implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent me) {
			if(me.getClickCount() == 2) {
				take();
			}
		}
	}

	/*
	 * Description panel updaten als selectie wijzigt.
	 */
	private class SelectionListener implements ChangeListener<Entity> {
		@Override
		public void changed(ObservableValue<? extends Entity> value, Entity oldItem, Entity newItem) {	
			description.update(newItem, coin);
		}		
	}
	
	/*
	 * Verwijdert de selectie van de lijst die niet meer gefocused is.
	 */
	private class FocusListener implements ChangeListener<Boolean> {
		public ListView<? extends Entity> list;
		
		public FocusListener(ListView<? extends Entity> list) {
			this.list = list;
		}
		
		@Override
		public void changed(ObservableValue<? extends Boolean> value,
				Boolean previous, Boolean current) {
			if(!current) {
				list.getSelectionModel().clearSelection();
			}
		}		
	}
	
	/*
	 *  Key handler voor de listviews.
	 */
	private class KeyListener implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch (ke.getCode()) {
			case ESCAPE:
				bus.post(new TransitionEvent("return"));
				break;
			case SPACE:
				take(); 					
				break;
			default:
				break;
			}
		}
	}
}
