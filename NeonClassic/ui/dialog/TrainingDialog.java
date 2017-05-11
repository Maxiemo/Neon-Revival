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
import javafx.scene.control.ListView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.jdom2.Element;

import com.google.common.eventbus.EventBus;

import neon.core.Engine;
import neon.entities.Creature;
import neon.entities.Player;
import neon.entities.property.Skill;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;
import neon.util.fsm.TransitionEvent;
import neon.resources.RPerson;

public class TrainingDialog {
	@FXML private Button cancelButton;
	@FXML private ListView<Skill> skillList;
	
	private Player player;
	private Creature trainer;	// uw trainer
	private EventBus bus;
	private UserInterface ui;
	private Stage stage;
	
	public TrainingDialog(UserInterface ui, EventBus bus) {
		this.bus = bus;
		this.ui = ui;
		
		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("training.fxml"));
		loader.setController(this);
		
		try {
	        Scene scene = new Scene(loader.load());
	        scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
	        scene.setFill(null);
			stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}

        skillList.setOnKeyPressed(new KeyHandler());
		cancelButton.setOnAction(event -> stage.close());
	}
	
	public void show(Player player, Creature trainer) {
		this.trainer = trainer;
		this.player = player;
		initTraining();

		ClientUtils.centerStage(stage, ui);
		stage.show();
	}
	
	private class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch(ke.getCode()) {
			case SPACE:
				train();
				break;
			case ESCAPE:
				stage.close();
				break;
			default:
				break;
			}
		}
	}
	
	private void initTraining() {
		ObservableList<Skill> skills = skillList.getItems();

		for(Element e : ((RPerson)Engine.getResources().getResource(trainer.getName())).services) {
			if(e.getAttributeValue("id").equals("training")) {
				for(Element skill : e.getChildren()) {
					skills.add(Skill.valueOf(skill.getText().toUpperCase()));
				}
			}
		}
	}
	
	@FXML private void train() {
		try {
			player.trainSkill(skillList.getSelectionModel().getSelectedItem(), 1);
			ui.showMessage("Training finished.", 2);
			// terug naar gameModule
			stage.close();		
			bus.post(new TransitionEvent("return"));
		} catch (ArrayIndexOutOfBoundsException f) {
			ui.showMessage("No skill selected.", 2);
		}
	}
}
