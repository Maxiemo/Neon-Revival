/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2012 - Maarten Driesen
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

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import neon.ui.ClientUtils;
import neon.ui.UserInterface;

public class BookDialog {
	@FXML private Button cancelButton;
	@FXML private WebView area;
	
	private Stage stage;
	private UserInterface ui;
	
	public BookDialog(UserInterface ui) {
		this.ui = ui;
		
		stage = new Stage(StageStyle.TRANSPARENT);
		stage.initOwner(ui.getStage());
		stage.initModality(Modality.WINDOW_MODAL);

		FXMLLoader loader = new FXMLLoader(getClass().getResource("book.fxml"));
		loader.setController(this);

		try {
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
			scene.setFill(null);
			stage.setScene(scene);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		area.setOnKeyPressed(new KeyHandler());
		cancelButton.setOnAction(event -> stage.close());
	}
	
	// TODO: custom stylesheet toevoegen
	public void show(String title, String text) {
		WebEngine engine = area.getEngine();
		engine.loadContent(text);
		
		ClientUtils.centerStage(stage, ui);
		stage.show();
	}

	private class KeyHandler implements EventHandler<KeyEvent> {
		@Override
		public void handle(KeyEvent ke) {
			switch(ke.getCode()) {
			case ESCAPE:
				stage.close();
				break;
			default:
				break;
			}
		}
	}
}
