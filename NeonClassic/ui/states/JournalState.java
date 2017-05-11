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

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.util.*;
import java.io.IOException;

import com.google.common.eventbus.EventBus;

import neon.core.handlers.CombatUtils;
import neon.core.handlers.InventoryHandler;
import neon.entities.Player;
import neon.entities.components.HealthComponent;
import neon.entities.components.Stats;
import neon.entities.property.*;
import neon.resources.RSpell;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;
import neon.ui.custom.SpellCellRenderer;
import neon.util.fsm.TransitionEvent;
import neon.util.fsm.State;

public class JournalState extends State {
	@FXML private Button questButton, characterButton, quitButton, spellButton, equipButton;
	@FXML private ListView<String> skillsList, featList, traitList, abilityList;
	@FXML private ListView<RSpell> spellList;
	@FXML private VBox statsBox;
	@FXML private Label equipLabel;
	@FXML private GridPane questPane, characterPane, spellPane;
	
	private Scene scene;
	private EventBus bus;
	private UserInterface ui;
	private Player player;
	
	public JournalState(State parent, EventBus bus, UserInterface ui)  {
		super(parent);
		this.bus = bus;
		this.ui = ui;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("journal.fxml"));
		loader.setController(this);
		
		try {
			Parent root = loader.load();
	    	root.setOnKeyPressed(new KeyListener());
			scene = new Scene(root);
			scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	quitButton.setOnAction(event -> bus.post(new TransitionEvent("cancel")));
    	spellButton.setOnAction(event -> switchPane("spells"));
    	questButton.setOnAction(event -> switchPane("quests"));
    	characterButton.setOnAction(event -> switchPane("character"));
    	
		spellList.setCellFactory(new SpellCellRenderer());
		switchPane("character");
	}
	
	@Override
	public void enter(TransitionEvent t) {
		player = (Player)getVariable("player");
		Platform.runLater(() -> initJournal());
		Platform.runLater(() -> initSpells());
		Platform.runLater(() -> initStats());
		ui.showScene(scene);
	}

	private void switchPane(String pane) {
		characterPane.setVisible(false);
		questPane.setVisible(false);
		spellPane.setVisible(false);
		equipButton.setVisible(false);
		equipLabel.setVisible(false);

		switch(pane) {
		case("character"):
			characterPane.setVisible(true);
		break;
		case("quests"):
			questPane.setVisible(true);
		break;
		case("spells"):
			spellPane.setVisible(true);	
		equipButton.setVisible(true);
		equipLabel.setVisible(true);
		break;
		}
	}

	private void initJournal() {
		questPane.getChildren().removeAll();
		HashMap<String, Integer> questList = player.getJournal().getQuests();
		HashMap<String, String> questDescriptions = player.getJournal().getSubjects();
		int row = 0;
		for(Map.Entry<String, Integer> entry : questList.entrySet()) {
			Label title = new Label(entry.getKey() + ": " + (entry.getValue() == 100 ? "finished" : "active"));
			title.setId("dialog-quest-title");
			questPane.add(title, 0, row);
			questPane.add(new Label(questDescriptions.get(entry.getKey())), 0, row + 1);
			row += 2;
		}
	}
	
	private void initSpells() {
		spellList.getItems().clear();
		ArrayList<RSpell> formulae = new ArrayList<RSpell>();
		formulae.addAll(player.getMagicComponent().getSpells());
		formulae.addAll(player.getMagicComponent().getPowers());
		spellList.getItems().addAll(formulae);
	}
	
	private void initStats() {
		statsBox.getChildren().clear();
		skillsList.getItems().clear();
		featList.getItems().clear();
		traitList.getItems().clear();
		abilityList.getItems().clear();
			
		Stats stats = player.getStatsComponent();
		HealthComponent health = player.getHealthComponent();
		statsBox.getChildren().add(new Label("Name: " + player.getName()));
		statsBox.getChildren().add(new Label("Race: " + player.species.id));
		statsBox.getChildren().add(new Label("Specialisation: " + player.getSpecialisation()));
		statsBox.getChildren().add(new Label("Strength: " + stats.getStr()));
		statsBox.getChildren().add(new Label("Constitution: " + stats.getCon()));
		statsBox.getChildren().add(new Label("Dexterity: " + stats.getDex()));
		statsBox.getChildren().add(new Label("Intelligence: " + stats.getInt()));
		statsBox.getChildren().add(new Label("Charisma: " + stats.getCha()));
		statsBox.getChildren().add(new Label("Wisdom: " + stats.getWis()));
		statsBox.getChildren().add(new Label("Health: " + health.getHealth() + "/" + health.getBaseHealth()));
		statsBox.getChildren().add(new Label("Mana: " + player.getMagicComponent().getMana() + "/" + player.species.mana*player.species.iq));
		statsBox.getChildren().add(new Label("Size: " + player.species.size));
		statsBox.getChildren().add(new Label("Gender: " + (player.getGender().toString().toLowerCase())));
		int light = stats.getStr()*3;
		int medium = stats.getStr()*6;
		int heavy = stats.getStr()*9;
		statsBox.getChildren().add(new Label("Encumbrance: " + InventoryHandler.getWeight(player) + " (of " + light + "/" + medium + "/" + heavy + ") kg"));
		statsBox.getChildren().add(new Label("Defense value: " + CombatUtils.getDV(player)));
		statsBox.getChildren().add(new Label("Attack value: " + ClientUtils.getAVString(player)));
		
		for(Skill skill : Skill.values()) {
			skillsList.getItems().add(skill.toString().toLowerCase() + ": " + player.getSkill(skill));
		}
		
		for(Feat feat : player.getCharacteristicsComponent().getFeats()) {
			featList.getItems().add(feat.text);
		}
		for(Trait trait : player.getCharacteristicsComponent().getTraits()) {
			traitList.getItems().add(trait.text);
		}
		for(Ability ability : player.getCharacteristicsComponent().getAbilities()) {
			abilityList.getItems().add(ability.text + ": " + player.getCharacteristicsComponent().getAbility(ability));
		}
	}
	
	@FXML private void equipSpell() {
		player.getMagicComponent().equipSpell(spellList.getSelectionModel().getSelectedItem());
		initSpells();		
	}
	
	// key handler voor de listview
	private class KeyListener implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch (ke.getCode()) {
			case ESCAPE:
			case J:
				bus.post(new TransitionEvent("cancel"));
				break;
			case SPACE:
				equipSpell(); 					
				break;
			case Q:
				switchPane("quests");
				break;
			case S:
				switchPane("spells");
				break;
			case C:
				switchPane("character");
				break;
			default:
				break;
			}
		}
	}
}
