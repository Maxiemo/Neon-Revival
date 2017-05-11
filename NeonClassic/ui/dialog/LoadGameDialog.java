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

import javafx.collections.FXCollections;
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

import java.io.File;
import java.io.IOException;

import com.google.common.eventbus.EventBus;

import neon.core.event.LoadEvent;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;

public class LoadGameDialog {
	@FXML Button cancelButton;
	@FXML ListView<String> saveList;
	
	private UserInterface ui;
	private EventBus bus;
	private Stage stage;

	public LoadGameDialog(UserInterface ui, EventBus bus) {
		this.bus = bus;
		this.ui = ui;

		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("load.fxml"));
		loader.setController(this);
		
		try {
	        Scene scene = new Scene(loader.load());
	        scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
	        scene.setFill(null);
			stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		saveList.setOnKeyPressed(new KeyHandler());
		cancelButton.setOnAction(event -> stage.close());		
	}
	
	public void show() {
		initSaves();
		ClientUtils.centerStage(stage, ui);
		stage.show();
	}

	private void initSaves() {
		File savedir = new File("saves");
		if(savedir.isDirectory()) {
			File[] files = savedir.listFiles();
			ObservableList<String> saves = FXCollections.observableArrayList();
			for(int i = 0; i < files.length; i++) {
				saves.add(files[i].getName());
			}
			saveList.setItems(saves);
		}
//		ObservableList<String> saves = FXCollections.observableArrayList("ien", "twie", "drei");
//		saveList.setItems(saves);
	}
	
	private class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch(ke.getCode()) {
			case ENTER:
				startGame();
				break;
			case ESCAPE:
				stage.close();
				break;
			default:
				break;
			}
		}
	}
	
	@FXML private void startGame() {
		stage.close();		
		bus.post(new LoadEvent(this, saveList.getSelectionModel().getSelectedItem()));
	}
}
