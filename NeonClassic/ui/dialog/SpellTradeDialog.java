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

import java.io.IOException;

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

import neon.entities.Creature;
import neon.entities.Player;
import neon.resources.RSpell;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;
import neon.ui.custom.SpellTradeCellRenderer;

public class SpellTradeDialog {
	@FXML private Button cancelButton;
	@FXML private ListView<RSpell> spellList;
	@FXML private Label infoLabel;
	
	private Player player;
	private Creature trader;
	private String big, small;
	private UserInterface ui;
	private Stage stage;
	
	/**
	 * Initializes a new spell trading dialog box.
	 * 
	 * @param ui
	 * @param big	name of major denominations (euro, dollar)
	 * @param small	name of minor denominations (cents)
	 */
	public SpellTradeDialog(UserInterface ui, String big, String small) {
		this.big = big;
		this.small = small;
		this.ui = ui;
		
		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("spelltrader.fxml"));
		loader.setController(this);
		
		try {
	        Scene scene = new Scene(loader.load());
	        scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
	        scene.setFill(null);
			stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}

        spellList.setOnKeyPressed(new KeyHandler());
        spellList.setCellFactory(new SpellTradeCellRenderer(small));
		cancelButton.setOnAction(event -> stage.close());
	}
	
	public void show(Player player, Creature trader) {
		this.trader = trader;
		this.player = player;
		initSpells();

		ClientUtils.centerStage(stage, ui);
		stage.show();
	}
	
	private class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch(ke.getCode()) {
			case SPACE:
				buy();
				break;
			case ESCAPE:
				stage.close();
				break;
			default:
				break;
			}
		}
	}
	
	private void initSpells() {
		ObservableList<RSpell> spells = spellList.getItems();
		spells.clear();		
		for(RSpell s : trader.getMagicComponent().getSpells()) {
			spells.add(s);			
		}
		
    	infoLabel.setText(ClientUtils.moneyString(player, big, small));
	}
	
	@FXML private void buy() {
		try {
			RSpell spell = spellList.getSelectionModel().getSelectedItem();
			if(player.getInventoryComponent().getMoney() >= spell.cost) {
				if(!player.getMagicComponent().getSpells().contains(spell)) {
					player.getMagicComponent().addSpell(spell);
					ui.showMessage("You bought the spell " + spell + ".", 2);
					initSpells();
				} else {
					ui.showMessage("You already have that spell.", 2);
				}
			} else {
				ui.showMessage("You don't have enough money.", 2);						
			}
		} catch (Exception e) {
			ui.showMessage("There is nothing left to buy.", 2);
		} 		
	}
}
