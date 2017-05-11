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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import neon.core.Engine;
import neon.core.handlers.InventoryHandler;
import neon.entities.Creature;
import neon.entities.Item;
import neon.entities.Player;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;
import neon.ui.custom.DescriptionPane;

public class TradeDialog {
	@FXML private Button cancelButton;
	@FXML private Label infoLabel, traderLabel;
	@FXML private ListView<Item> playerList, traderList;
	@FXML private DescriptionPane description;
	
	private Player player;
	private Creature trader;
	private String big, small;
	private UserInterface ui;
	private Stage stage;
	
	/**
	 * @param ui
	 * @param big	name of major denominations (euro, dollar)
	 * @param small	name of minor denominations (cents)
	 */
	public TradeDialog(UserInterface ui, String big, String small) {
		this.ui = ui;
		this.big = big;
		this.small = small;
		
		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("trade.fxml"));
		loader.setController(this);
		
		try {
	        Scene scene = new Scene(loader.load());
	        scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
	        scene.setFill(null);
			stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}

		KeyHandler handler = new KeyHandler();
		SelectionListener listener = new SelectionListener();
		playerList.setOnKeyPressed(handler);
		playerList.getSelectionModel().selectedItemProperty().addListener(listener);
		traderList.setOnKeyPressed(handler);
		traderList.getSelectionModel().selectedItemProperty().addListener(listener);
		
		cancelButton.setOnAction(event -> stage.close());
	}
	
	public void show(Player player, Creature trader) {
		this.trader = trader;
		traderLabel.setText(trader.getName());
		this.player = player;
		initGoods();

		ClientUtils.centerStage(stage, ui);
		stage.show();
	}
	
	private class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch(ke.getCode()) {
			case SPACE:
				trade();
				break;
			case ESCAPE:
				stage.close();
				break;
			default:
				break;
			}
		}
	}
	
	private void initGoods() {
		ObservableList<Item> sellData = playerList.getItems();
		sellData.clear();
		for(long uid : Engine.getPlayer().getInventoryComponent()) {
			sellData.add((Item)Engine.getGame().getEntity(uid));				
		}

		ObservableList<Item> buyData = traderList.getItems();
		buyData.clear();
		for(long uid : trader.getInventoryComponent()) {
			buyData.add((Item)Engine.getGame().getEntity(uid));				
		}
		
    	infoLabel.setText(ClientUtils.moneyString(player, big, small));
	}

	@FXML private void trade() {
		try {
			if(playerList.isFocused()) {
				int index = playerList.getSelectionModel().getSelectedIndex();
				sell(playerList.getSelectionModel().getSelectedItem());
				initGoods();
				playerList.getSelectionModel().select(Math.min(index, playerList.getItems().size() - 1));
			} else {
				int index = traderList.getSelectionModel().getSelectedIndex();
				buy(traderList.getSelectionModel().getSelectedItem());
				initGoods();
				traderList.getSelectionModel().select(Math.min(index, traderList.getItems().size() - 1));
			}
		} catch (Exception e) {
			if(playerList.isFocused()) {
				ui.showMessage("There is nothing left to sell.", 2);
			} else {
				ui.showMessage("There is nothing left to buy.", 2);
			}
		} 
	}
	
	private void buy(Item item) {
		int price = item.resource.cost;
		if(price > player.getInventoryComponent().getMoney()) {
			ui.showMessage("Not enough money to buy this item.", 2);
		} else {
			InventoryHandler.removeItem(trader, item);
			player.getInventoryComponent().addItem(item.getUID());				
			player.getInventoryComponent().addMoney(-price);
		}
	}
	
	private void sell(Item item) {
		InventoryHandler.removeItem(player, item);
		trader.getInventoryComponent().addItem(item.getUID());
		player.getInventoryComponent().addMoney(item.resource.cost);
	}

	private class SelectionListener implements ChangeListener<Item> {
		@Override
		public void changed(ObservableValue<? extends Item> observable, Item oldItem, Item newItem) {	
			description.update(newItem, small);
		}		
	}	
}
