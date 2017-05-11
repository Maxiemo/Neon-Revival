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

package neon.ui.states;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.Popup;

import com.google.common.eventbus.EventBus;

import neon.core.Engine;
import neon.core.event.CombatEvent;
import neon.core.handlers.MotionHandler;
import neon.entities.Creature;
import neon.entities.Player;
import neon.resources.RCreature;
import neon.resources.RCreature.Size;
import neon.ui.GamePanel;
import neon.ui.UserInterface;
import neon.util.fsm.State;
import neon.util.fsm.TransitionEvent;

public class BumpState extends State {
	private Creature creature;
	private Popup popup;
	private GamePanel panel;
	private EventBus bus;
	private UserInterface ui;
	private Player player;
	
	public BumpState(State parent, EventBus bus, UserInterface ui) {
		super(parent);
		this.bus = bus;
		this.ui = ui;
	}
	
	@Override
	public void enter(TransitionEvent t) {
		player = (Player)getVariable("player");
		creature = (Creature)t.getParameter("creature");
		panel = (GamePanel)getVariable("panel");
		setKeys();
		setActions();
		if(creature.hasDialog()) {
			popup = ui.showPopup("1) attack 2) talk 3) pick pocket 4) switch place 0) cancel");
		} else if(isMount(creature)) {
			popup = ui.showPopup("1) attack 3) pick pocket 4) switch place 5) mount 0) cancel");
		} else {
			popup = ui.showPopup("1) attack 3) pick pocket 4) switch place 0) cancel");
		}
	}

	@Override
	public void exit(TransitionEvent t) {
		unsetKeys();
		popup.hide();
		panel.repaint();
	}
	
	private void setKeys() {
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('0'), "cancel");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('1'), "attack");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('2'), "talk");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('3'), "pick");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('4'), "switch");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('5'), "mount");
	}
	
	private void unsetKeys() {
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke('0'));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke('1'));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke('2'));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke('3'));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke('4'));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke('5'));
	}
	
	private void setActions() {
        panel.getActionMap().put("cancel", new KeyAction("cancel"));
        panel.getActionMap().put("attack", new KeyAction("attack"));
        panel.getActionMap().put("talk", new KeyAction("talk"));
        panel.getActionMap().put("pick", new KeyAction("pick"));
        panel.getActionMap().put("switch", new KeyAction("switch"));
        panel.getActionMap().put("mount", new KeyAction("mount"));
	}

	@SuppressWarnings("serial")
	private class KeyAction extends AbstractAction {
		public KeyAction(String command) {
			putValue(ACTION_COMMAND_KEY, command);
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			switch(ae.getActionCommand()) {
			case "attack": 
				bus.post(new CombatEvent(player, creature));
				creature.brain.makeHostile(player);
				bus.post(new TransitionEvent("return"));
				break;
			case "talk": 
				if(creature.hasDialog()) {
					bus.post(new TransitionEvent("dialog", "speaker", creature));
				}
				break;
			case "pick": 
				System.out.println("not implemented"); 
				bus.post(new TransitionEvent("return"));
				break;
			case "switch": 
				swap();
				bus.post(new TransitionEvent("return"));
				break;
			case "mount": 
				if(isMount(creature)) {
					player.mount(creature);
					Rectangle pBounds = player.getShapeComponent();
					Rectangle cBounds = creature.getShapeComponent();
					pBounds.setLocation(cBounds.x, cBounds.y);
					Engine.getGame().getAtlas().getCurrentZone().removeCreature(creature.getUID());
					panel.repaint();
					bus.post(new TransitionEvent("return"));
				}
				break;
			case "cancel": 
				bus.post(new TransitionEvent("return"));
				break;
			}
		}
	}

	private void swap() {
		Rectangle pBounds = player.getShapeComponent();
		Rectangle cBounds = creature.getShapeComponent();
		
		if(MotionHandler.move(player, cBounds.x, cBounds.y) == MotionHandler.OK) {
			cBounds.setLocation(pBounds.x, pBounds.y);			
		}
	}
	
	private boolean isMount(Creature mount) {
		return mount.species.type == RCreature.Type.animal && mount.species.size == Size.large
				&& !mount.brain.isHostile();
	}
}
