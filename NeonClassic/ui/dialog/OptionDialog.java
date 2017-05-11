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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import neon.core.Engine;
import neon.resources.CClient;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;

public class OptionDialog {
	@FXML Button cancelButton;
	@FXML ToggleGroup keyGroup;
	@FXML CheckBox audioBox;
	@FXML RadioButton numpadButton, azertyButton, qwertyButton, qwertzButton;
	
	private UserInterface ui;
	private CClient config;
	private Stage stage;
	
	public OptionDialog(UserInterface ui, CClient config) {
		this.config = config;
		this.ui = ui;
		
		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("option.fxml"));
		loader.setController(this);
		
		try {
	        Scene scene = new Scene(loader.load());
	        scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
	        scene.setFill(null);
			stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// cancel knop
		cancelButton.setOnAction(event -> stage.close());
	}

	public void show() {
		switch(config.getSettings()) {
		case AZERTY: azertyButton.setSelected(true); break;
		case QWERTY: qwertyButton.setSelected(true); break;
		case QWERTZ: qwertzButton.setSelected(true); break;
		case NUMPAD: numpadButton.setSelected(true); break;
		}

		ClientUtils.centerStage(stage, ui);
		stage.show();
	}

	@FXML private void save() {
		Document doc = new Document();
		try {
			FileInputStream in = new FileInputStream("neon.ini");
			doc = new SAXBuilder().build(in);
			in.close();
		} catch (Exception e) {
			Engine.getLogger().severe(e.getMessage());
		}

		Element ini = doc.getRootElement();
		if(numpadButton.isSelected()) {
			config.setKeys(CClient.KeyboardSetting.NUMPAD);
			ini.getChild("keys").setText("numpad");
		} else if(azertyButton.isSelected()) {
			config.setKeys(CClient.KeyboardSetting.AZERTY);
			ini.getChild("keys").setText("azerty");
		} else if(qwertyButton.isSelected()) {
			config.setKeys(CClient.KeyboardSetting.QWERTY);
			ini.getChild("keys").setText("qwerty");
		} else if(qwertzButton.isSelected()) {
			config.setKeys(CClient.KeyboardSetting.QWERTZ);
			ini.getChild("keys").setText("qwertz");
		}

		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		try {
			FileOutputStream out = new FileOutputStream("neon.ini");
			outputter.output(doc, out);
			out.close();
		} catch (IOException e) {
			Engine.getLogger().severe(e.getMessage());
		}
		
		stage.close();
	}
}
