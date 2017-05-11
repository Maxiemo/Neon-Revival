/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2010-2015 - Maarten Driesen
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

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;

import com.google.common.eventbus.EventBus;

import neon.resources.CClient;
import neon.ui.UserInterface;
import neon.ui.dialog.LoadGameDialog;
import neon.ui.dialog.NewGameDialog;
import neon.ui.dialog.OptionDialog;
import neon.util.fsm.State;
import neon.util.fsm.TransitionEvent;

public class MainMenuState extends State {
	private UserInterface ui;
	private Scene scene;

	@FXML private Hyperlink newLink, loadLink, optionLink, quitLink;
	@FXML private Text versionText, titleText;

	public MainMenuState(State parent, EventBus bus, UserInterface ui, String version, CClient config) {
		super(parent, "main menu");
		this.ui = ui;
		
		FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
		loader.setController(this);
		
		try {
			scene = new Scene(loader.load());
			scene.getStylesheets().add(UserInterface.class.getResource("menu.css").toExternalForm());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        titleText.setText(config.getTitle());
        
		String newString = config.getString("$newGame");
		newLink.setText(newString);
		newLink.setOnMouseEntered(event -> newLink.requestFocus());
		newLink.setOnAction(event -> new NewGameDialog(ui, bus).show());
        
		String loadString = config.getString("$loadGame");
		loadLink.setText(loadString);
		loadLink.setOnMouseEntered(event -> loadLink.requestFocus());
		loadLink.setOnAction(event -> new LoadGameDialog(ui, bus).show());

		String optionString = config.getString("$options");
		optionLink.setText(optionString);
		optionLink.setOnMouseEntered(event -> optionLink.requestFocus());
		optionLink.setOnAction(event -> new OptionDialog(ui, config).show());

		String quitString = config.getString("$quit");
		quitLink.setText(quitString);
		quitLink.setOnMouseEntered(event -> quitLink.requestFocus());
		quitLink.setOnAction(event -> System.exit(0));

		versionText.setText("release " + version);
	}
	
	@Override
	public void enter(TransitionEvent t) {
		ui.showScene(scene);
	}
	
	@FXML private void loadWebsite() throws IOException, URISyntaxException {
		if(Desktop.isDesktopSupported()) {			
			// TODO: faalt in linux
			Desktop.getDesktop().browse(new URI("http://sourceforge.net/projects/neon"));
		}
	}
}
