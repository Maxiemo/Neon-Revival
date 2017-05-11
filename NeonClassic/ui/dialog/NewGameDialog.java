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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;

import com.google.common.eventbus.EventBus;

import neon.core.Engine;
import neon.core.event.LoadEvent;
import neon.entities.Player;
import neon.entities.Player.Specialisation;
import neon.entities.property.Gender;
import neon.resources.CGame;
import neon.resources.RCreature;
import neon.resources.RSign;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;
import neon.ui.custom.RaceCellRenderer;

public class NewGameDialog {
	@FXML private ComboBox<RCreature> raceBox;
	@FXML private ComboBox<RSign> signBox;
	@FXML private ToggleGroup genderGroup, specGroup;
	@FXML private RadioButton femaleButton, maleButton;
	@FXML private TextField nameField, professionField;
	@FXML private HBox specBox;
	@FXML private Button cancelButton;
	private EventBus bus;
	private UserInterface ui;
	private Stage stage;
		
	public NewGameDialog(UserInterface ui, EventBus bus) {
		this.bus = bus;
		this.ui = ui;

		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("new.fxml"));
		loader.setController(this);
		Parent root = null;
		
		try {
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// geslacht
		femaleButton.setUserData(Gender.FEMALE);
		maleButton.setUserData(Gender.MALE);

		// ras
        CGame game = (CGame)Engine.getResources().getResource("game", "config");
        initRaces(game);

		// specialisatie
		for(Specialisation spec : Player.Specialisation.values()) {
			RadioButton button = new RadioButton(spec.toString());
			button.setToggleGroup(specGroup);
			button.setUserData(spec);
			specBox.getChildren().add(button);
		}
		specGroup.getToggles().get(0).setSelected(true);

		// birthsign
        signBox.getItems().addAll(Engine.getResources().getResources(RSign.class));
        signBox.getSelectionModel().selectFirst();
        signBox.setCellFactory(new SignCellFactory());
        signBox.setButtonCell(new SignCellFactory().call(null));

		// cancel knop
		cancelButton.setOnAction(event -> stage.close());
		
        Scene scene = new Scene(root);
        scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
        scene.setFill(null);
		stage.setScene(scene);
	}

	public void show() {
		ClientUtils.centerStage(stage, ui);
		stage.show();
		nameField.requestFocus();
	}

	private void initRaces(CGame game) {
		for(String race : game.getPlayableRaces()) {
			raceBox.getItems().add((RCreature)Engine.getResources().getResource(race));
		}
        raceBox.getSelectionModel().selectFirst();
        raceBox.setCellFactory(new RaceCellRenderer());
        raceBox.setButtonCell(new RaceCellRenderer().call(null));
	}
	
	private class SignCellFactory implements Callback<ListView<RSign>, ListCell<RSign>> {
		@Override
		public ListCell<RSign> call(ListView<RSign> param) {
			final ListCell<RSign> cell = new ListCell<RSign>() {
				@Override
				public void updateItem(RSign item, boolean empty) {
					super.updateItem(item, empty);
					if (item != null) {
						setText(item.name);
					} else {
						setText(null);
					}
				}
			};
			return cell;
		}
	}

	@FXML private void startGame() {
		if(nameField.getText().equals("")) {
			ui.showMessage("Please give a name.", 2);
			nameField.requestFocus();
		} else if(checkSaves(nameField.getText())) {
			ui.showMessage("There is already a character with the given name. <br>" +
					"Choose another name or remove the existing character.", 3);
			nameField.requestFocus();
		} else {
			// TODO: menu moet eigenlijk geblokt worden totdat game geladen is
			stage.close();
			Gender gender = (Gender)genderGroup.getSelectedToggle().getUserData();
			Specialisation spec = (Specialisation)specGroup.getSelectedToggle().getUserData();
			bus.post(new LoadEvent(this, raceBox.getSelectionModel().getSelectedItem().id, 
					nameField.getText(), gender, spec, professionField.getText(), 
					signBox.getSelectionModel().getSelectedItem()));
		}
	}
	
	private boolean checkSaves(String name) {
		File save = new File("saves/" + name);
		return save.exists();
	}
}
