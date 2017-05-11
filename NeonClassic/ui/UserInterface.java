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

package neon.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.script.ScriptEngine;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import neon.ui.console.JConsole;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

/**
 * Implements the main user interface of the neon engine. Any engine state can 
 * register a <code>Scene</code> to show. 
 * 
 * @author	mdriesen
 */
public class UserInterface {
	private final Stage stage;
	private Popup popup;
	private HelpWindow help;
	
    /**
     * Initialize the user interface, using data provided by the engine.
     * 
     * @param title	the title of the window
     * @param stage	the main window of this application
     */
	public UserInterface(String title, Stage stage) {
		this.stage = stage;
		stage.setOnCloseRequest(event -> System.exit(0));
		stage.setTitle("Neon: " + title);
        stage.setWidth(1024);
        stage.setHeight(600);
		stage.show();
		stage.centerOnScreen();
	}
	
    /**
     * Shows a <code>Scene</code>.
     * 
     * @param scene	the scene to show
     */
	public void showScene(Scene scene) {
		final Scene s = scene;
		Platform.runLater(new Runnable() {
			public void run() {
				stage.setScene(s);
			}
		});
	}
	
	/**
	 * @return	the main {@code Stage}
	 */
	public Stage getStage() {
		return stage;
	}
	
	/**
	 * Shows a message on screen for the time given.
	 * 
	 * @param message	the message to show, html code is allowed
	 * @param time		the time in seconds
	 */
	public void showMessage(String message, int time, int pos) {
		JLabel label = new JLabel("<html><center>" + message + "</center></html>");
		label.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(5,10,10,10)));
		JDialog dialog = new JDialog();
		dialog.setModal(false);
		dialog.setFocusableWindowState(false);	// anders focus op dialog, niet op contentpane
		dialog.add(label);
		dialog.setUndecorated(true);

		Timer timer = new Timer(1000 * time, new DialogListener(dialog));
		timer.setRepeats(false);
		timer.start();
		
		dialog.pack();
		int x = (int)stage.getX() + ((int)stage.getWidth() - dialog.getWidth())/2;
		int y = (int)stage.getY() + ((int)stage.getHeight()*9/10 - dialog.getHeight())/2;
		
		switch(pos) {
		case SwingConstants.CENTER: 
			break;
		default:	// beneden op scherm
			y = (int)stage.getY() + (int)stage.getHeight() - dialog.getHeight();
			break;
		}
		
		dialog.setLocation(x, y);
		dialog.setVisible(true);
	}

	/**
	 * Shows a message centered on screen for the time given.
	 * 
	 * @param message	the message to show, html code is allowed
	 * @param time		the time in seconds
	 */
	public void showMessage(String message, int time) {
		showMessage(message, time, SwingConstants.CENTER);
	}
	
	/**
	 * Shows a yes/no question centered on screen.
	 * 
	 * @param question	the message to show, html code is allowed
	 * @return	the answer to the question
	 */
	public boolean showQuestion(String question) {
		JPanel content = new JPanel(new BorderLayout());
		content.add(new JLabel("<html><center>" + question + "</center></html>"));
		JPanel buttons = new JPanel();
		JButton yes = new JButton("Yes");
		yes.setMnemonic('Y');
		JButton no = new JButton("No");
		no.setMnemonic('N');
		buttons.add(yes);
		buttons.add(no);
		content.add(buttons, BorderLayout.PAGE_END);
		content.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(10,10,10,10)));
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.setContentPane(content);
		dialog.setUndecorated(true);

		DialogListener listener = new DialogListener(dialog);
		yes.addActionListener(listener);
		no.addActionListener(listener);
        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('y'), "Yes");
        content.getActionMap().put("Yes", listener);
        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('n'), "No");
        content.getActionMap().put("No", listener);

		dialog.pack();
		int x = (int)stage.getX() + ((int)stage.getWidth() - dialog.getWidth())/2;
		int y = (int)stage.getY() + ((int)stage.getHeight() - dialog.getHeight())/2;
        dialog.setLocation(x, y);
		dialog.setVisible(true);
		
		return listener.answer;
	}
	
	/**
	 * Shows a popup message at the bottom of the screen.
	 * 
	 * @param message	the message to show, html code is allowed
	 * @return	the {@code Popup}
	 */
	public Popup showPopup(String message) {
		JLabel label = new JLabel("<html><center>" + message + "</center></html>");
		label.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(4,10,8,10)));
		int x = (int)(stage.getX() + (stage.getScene().getWidth() - label.getPreferredSize().width)/2);
		int y = (int)(stage.getY() + stage.getScene().getHeight() - label.getPreferredSize().height);

		if(popup != null) {
			popup.hide();
		}

		popup = PopupFactory.getSharedInstance().getPopup(null, label, x, y);
		popup.show();
		
		return popup;
	}
	
	/**
	 * Shows the console
	 */
	public void showConsole(ScriptEngine engine) {
		JConsole console = new JConsole(engine);
		console.show();
	}
	
	/**
	 * Shows the given string in a help window.
	 */
	public void showHelp(String text) {
		if(help == null) {
			help = new HelpWindow();
		}
		help.show("Neon help", text);
	}
	
	@SuppressWarnings("serial")
	private class DialogListener extends AbstractAction {
		private JDialog dialog;
		private boolean answer;
		
		public DialogListener(JDialog dialog) {
			this.dialog = dialog;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			answer = "Yes".equals(e.getActionCommand()) || "y".equals(e.getActionCommand());
			dialog.dispose();
		}
	}
}
