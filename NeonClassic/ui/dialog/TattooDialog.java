/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2013 - Maarten Driesen
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;
import javax.swing.border.*;
import neon.core.Engine;
import neon.entities.Creature;
import neon.entities.Player;
import neon.resources.RTattoo;
import neon.ui.UserInterface;

// TODO: tattoos
public class TattooDialog implements KeyListener {
	private JDialog frame;
	private Player player;
	private JList<RTattoo> tattoos;
	private JPanel panel;
	private String coin;
	private UserInterface ui;
	
	public TattooDialog(UserInterface ui, String coin) {
		this.coin = coin;
		this.ui = ui;
		
		panel = new JPanel(new BorderLayout());
		panel.setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.RAISED), new EmptyBorder(10,10,10,10)));		
        
		// lijst met tattoos
		tattoos = new JList<RTattoo>();
		tattoos.setFocusable(false);
		tattoos.setCellRenderer(new TattooCellRenderer());
        JScrollPane scroller = new JScrollPane(tattoos);
        tattoos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	scroller.setBorder(new TitledBorder("Tattoos"));
        panel.add(scroller, BorderLayout.CENTER);
        
        // instructies geven
		JLabel instructions = new JLabel("Use arrow keys to select a tattoo, press enter to draw, esc to exit.");
    	instructions.setBorder(new CompoundBorder(new TitledBorder("Instructions"), new EmptyBorder(0,5,10,5)));
        panel.add(instructions, BorderLayout.PAGE_END);

 		frame.setContentPane(panel);
        frame.addKeyListener(this);
        try {
        	frame.setOpacity(0.9f);
        } catch(UnsupportedOperationException e) {
        	System.out.println("setOpacity() not supported.");
        }
	}
	
	public void show(Player player, Creature tattooist) {
		this.player = player;
		initTattoos();
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);	
	}
	
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ESCAPE: 
			frame.dispose();
			break;
		case KeyEvent.VK_UP:
			if(tattoos.getSelectedIndex() > 0) {
				tattoos.setSelectedIndex(tattoos.getSelectedIndex()-1);
			}
			break;
		case KeyEvent.VK_DOWN:
			tattoos.setSelectedIndex(tattoos.getSelectedIndex()+1); 
			break;
		case KeyEvent.VK_ENTER:
			try {
				RTattoo tattoo = tattoos.getSelectedValue();
				if(!player.getTattoos().contains(tattoo)) {
					if(player.getInventoryComponent().getMoney() >= tattoo.cost) {
						player.addTattoo(tattoo);
						ui.showMessage("You got the tattoo '" + tattoo.name + "'.", 2);
						player.getInventoryComponent().addMoney(-tattoo.cost);
						initTattoos();
					} else {
						ui.showMessage("You don't have enough money.", 2);						
					}
				} else {
					ui.showMessage("You already have that tattoo.", 2);
				}
			} catch (Exception f) {
				ui.showMessage("There is nothing left to buy.", 2);
			} 
			break;
		}
	}
	
	private void initTattoos() {
		tattoos.setListData(Engine.getResources().getResources(RTattoo.class));
    	tattoos.setSelectedIndex(0);
	}
	
	private class TattooCellRenderer implements ListCellRenderer<RTattoo> {
		private UIDefaults defaults = UIManager.getLookAndFeelDefaults();

	    public Component getListCellRendererComponent(JList<? extends RTattoo> list, 
	    		RTattoo value, int index, boolean isSelected, boolean cellHasFocus) {
	    	JLabel label = new JLabel(value.name + " (" + value.cost + " " + coin + ")");
	        
			if(isSelected) {
				label.setBackground(defaults.getColor("List.selectionBackground"));
				label.setForeground(defaults.getColor("List.selectionForeground"));
			} else {
				label.setForeground(defaults.getColor("List.foreground"));
			}
			label.setOpaque(isSelected);
			return label;
	    }			
	}
}
