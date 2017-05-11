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

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import neon.entities.Creature;
import neon.entities.Player;
import neon.magic.Effect;
import neon.resources.RSpell;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;

public class SpellMakerDialog {
	@FXML private Button cancelButton;
	@FXML private TextField nameField;
	@FXML private Slider sizeSpinner, rangeSpinner, durationSpinner;
	@FXML private ComboBox<Effect> effectBox;
	@FXML private Label sizeLabel, rangeLabel, durationLabel;
	
	private Player player;
	private UserInterface ui;
	private Stage stage;
	
	public SpellMakerDialog(UserInterface ui) {
		this.ui = ui;
		
		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("spellmaker.fxml"));
		loader.setController(this);
		
		try {
	        Scene scene = new Scene(loader.load());
	        scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
	        scene.setFill(null);
			stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		cancelButton.setOnAction(event -> stage.close());
		effectBox.getItems().addAll(neon.magic.Effect.values());
		sizeSpinner.valueProperty().addListener((ObservableValue<? extends Number> value, 
	            Number previous, Number current) -> sizeLabel.setText(String.format("%.0f", current)));
		rangeSpinner.valueProperty().addListener((ObservableValue<? extends Number> value, 
	            Number previous, Number current) -> rangeLabel.setText(String.format("%.0f", current)));
		durationSpinner.valueProperty().addListener((ObservableValue<? extends Number> value, 
	            Number previous, Number current) -> durationLabel.setText(String.format("%.0f", current)));
	}
	
	public void show(Player player, Creature enchanter) {
		this.player = player;
		
		ClientUtils.centerStage(stage, ui);
		stage.show();
	}

	@FXML private void createSpell() {
		if(isValid()) {
			RSpell spell = new RSpell(nameField.getText(), (int)rangeSpinner.getValue(), 
					(int)durationSpinner.getValue(),effectBox.getSelectionModel().getSelectedItem().toString(), 
					(int)sizeSpinner.getValue(), (int)sizeSpinner.getValue(), "spell");
			player.getMagicComponent().addSpell(spell);
			stage.close();		
		} else {
			ui.showMessage("Please fill in all required fields.", 2);
		}
	}

	private boolean isValid() {
		return(nameField.getText() != null && !nameField.getText().isEmpty());
	}
}
