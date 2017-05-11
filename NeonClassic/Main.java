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

package neon;

import javafx.application.Application;
import javafx.stage.Stage;
import neon.core.Engine;
import neon.systems.io.LocalPort;
import neon.ui.Client;

/**
 * The main class of the neon roguelike engine.
 * 
 * @author	mdriesen
 */
public class Main extends Application {
	private static final String version = "0.4.3";	// huidige versie

	public static void main(String[] args) {
		launch(args);
	}

	/**
     * The application's start method. This method creates an {@code Engine} and 
     * a {@code Client} instance and connects them.
     * 
     * @param Stage	the JavaFX {@code Stage} for this application
     */
	@Override
	public void start(Stage stage) {
		// poorten aanmaken en verbinden
		LocalPort cPort = new LocalPort();
		LocalPort sPort = new LocalPort();
		cPort.connect(sPort);
		sPort.connect(cPort);
		
		// engine en client aanmaken
		new Engine(sPort).run();
		new Client(cPort, version).run(stage);
	}
}