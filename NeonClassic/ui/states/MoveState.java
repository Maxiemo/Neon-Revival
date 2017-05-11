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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import com.google.common.eventbus.EventBus;

import neon.core.Atlas;
import neon.core.Engine;
import neon.core.event.CombatEvent;
import neon.core.event.MagicEvent;
import neon.core.event.TurnEvent;
import neon.core.handlers.*;
import neon.entities.*;
import neon.entities.property.Condition;
import neon.entities.property.Slot;
import neon.resources.CClient;
import neon.resources.RItem;
import neon.resources.RSpell;
import neon.ui.GamePanel;
import neon.util.fsm.TransitionEvent;
import neon.util.fsm.State;

public class MoveState extends State {
	private GamePanel panel;
	private Player player;
	private CClient keys;
	private EventBus bus;
	private Atlas atlas;

	public MoveState(State parent, EventBus bus, CClient config) {
		super(parent, "move module");
		this.bus = bus;
		keys = config;
	}
	
	@Override
	public void enter(TransitionEvent e) {
		player = (Player)getVariable("player");
		panel = (GamePanel)getVariable("panel");
		atlas = Engine.getGame().getAtlas();
		setKeys();
		setActions();
	}
	
	@Override
	public void exit(TransitionEvent e) {
		unsetKeys();
	}
	
	private void setKeys() {
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.up), "up");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.upleft), "upleft");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.left), "left");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.downleft), "downleft");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.down), "down");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.downright), "downright");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.right), "right");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.upright), "upright");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.wait), "wait");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.magic), "magic");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.shoot), "shoot");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.look), "look");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.act), "act");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.talk), "talk");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.unmount), "unmount");
	}
	
	private void unsetKeys() {
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.up));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.upleft));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.left));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.downleft));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.down));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.downright));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.right));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.upright));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.wait));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.magic));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.shoot));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.look));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.act));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.talk));
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(KeyStroke.getKeyStroke(keys.unmount));
	}
	
	private void setActions() {
        panel.getActionMap().put("up", new KeyAction("up"));
        panel.getActionMap().put("upleft", new KeyAction("upleft"));
        panel.getActionMap().put("left", new KeyAction("left"));
        panel.getActionMap().put("downleft", new KeyAction("downleft"));
        panel.getActionMap().put("down", new KeyAction("down"));
        panel.getActionMap().put("downright", new KeyAction("downright"));
        panel.getActionMap().put("right", new KeyAction("right"));
        panel.getActionMap().put("upright", new KeyAction("upright"));
        panel.getActionMap().put("wait", new KeyAction("wait"));
        panel.getActionMap().put("magic", new KeyAction("magic"));
        panel.getActionMap().put("shoot", new KeyAction("shoot"));
        panel.getActionMap().put("look", new KeyAction("look"));	
        panel.getActionMap().put("act", new KeyAction("act"));
        panel.getActionMap().put("talk", new KeyAction("talk"));
        panel.getActionMap().put("unmount", new KeyAction("unmount"));		
	}
	
	private void move(int x, int y) {
		// TODO: dit moet gedeeltelijk naar MotionHandler?
		Rectangle bounds = player.getShapeComponent();
		Point p = new Point(bounds.x + x, bounds.y + y);

		// kijken of creature in de weg staat
		Creature other = atlas.getCurrentZone().getCreature(p);
		if(other != null && !other.hasCondition(Condition.DEAD)) {
			if(other.brain.isHostile()) {
				bus.post(new CombatEvent(player, other));
				bus.post(new TurnEvent(Engine.getTimer().addTick(), panel.getVisibleRectangle())); // volgende beurt
			} else {
				bus.post(new TransitionEvent("bump", "creature", other));
			}
		} else {	// niemand in de weg, dus moven
			if(MotionHandler.move(player, p) == MotionHandler.DOOR) {
				for(long uid : atlas.getCurrentZone().getItems(p)) {
					if(Engine.getGame().getEntity(uid) instanceof Door) {
						bus.post(new TransitionEvent("door", "door", Engine.getGame().getEntity(uid)));
					}
				}
			}
			bus.post(new TurnEvent(Engine.getTimer().addTick(), panel.getVisibleRectangle())); // volgende beurt
		}
	}
	
	/*
	 * dingen om te doen als spatie is gebruikt
	 */
	private void act() {
		// hier de lijst klonen, anders concurrentmodificationexceptions bij item oppakken
		Rectangle bounds = player.getShapeComponent();
		ArrayList<Long> items = new ArrayList<Long>(atlas.getCurrentZone().getItems(bounds));
		Creature c = atlas.getCurrentZone().getCreature(bounds.getLocation());
		if(c != null) {
			items.add(c.getUID());
		}

		if(items.size() == 1) {
			Entity entity = Engine.getGame().getEntity(items.get(0));
			if(entity instanceof Container) {
				Container container = (Container)entity;
				if(container.lock.isLocked()) {
					if(container.lock.hasKey() && hasItem(player, container.lock.getKey())) {
						bus.post(new TransitionEvent("container", "holder", entity));
					} else {
						bus.post(new TransitionEvent("lock", "lock", container.lock));						
					}
				} else {
					bus.post(new TransitionEvent("container", "holder", entity));
				}
			} else if(entity instanceof Door) {
				if(MotionHandler.teleport(player, (Door)entity) == MotionHandler.OK) {
					bus.post(new TurnEvent(Engine.getTimer().addTick(), panel.getVisibleRectangle()));
				}
			} else if(entity instanceof Creature){
				bus.post(new TransitionEvent("container", "holder", entity));							
			} else {
				atlas.getCurrentZone().removeItem((Item)entity);
				InventoryHandler.addItem(player, (Item)entity);
			}
		} else if(items.size() > 1) {
			bus.post(new TransitionEvent("container", "holder", atlas.getCurrentZone()));
		}
	}
	
	private boolean hasItem(Creature creature, RItem item) {
		for(long uid : creature.getInventoryComponent()) {
			if(Engine.getGame().getEntity(uid).getID().equals(item.id)) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("serial")
	private class KeyAction extends AbstractAction {
		public KeyAction(String command) {
			putValue(ACTION_COMMAND_KEY, command);
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			switch(ae.getActionCommand()) {
			case "up":
				move(0, - 1);
				break;
			case "upright": 
				move(1, - 1);
				break;
			case "right": 
				move(1, 0);
				break;
			case "downright": 
				move(1, 1);
				break;
			case "down": 
				move(0, 1);
				break;
			case "downleft": 
				move(- 1, 1);
				break;
			case "left": 
				move(- 1, 0);
				break;
			case "upleft": 
				move(- 1, - 1);
				break;
			case "wait": 
				move(0, 0);
				break;
			case "act": 
				act();
				break;
			case "look": 
				bus.post(new TransitionEvent("aim"));
				break;
			case "shoot": 
				bus.post(new TransitionEvent("aim"));
				break;
			case "talk": 
				bus.post(new TransitionEvent("aim"));
				break;
			case "unmount": 
				if(player.isMounted()) {
					Creature mount = player.getMount();
					player.unmount();
					atlas.getCurrentZone().addCreature(mount);
					Rectangle pBounds = player.getShapeComponent();
					Rectangle mBounds = mount.getShapeComponent();
					mBounds.setLocation(pBounds.x, pBounds.y);
				}
				break;
			case "magic": 
				if(player.getMagicComponent().getSpell() != null) {
					RSpell spell = player.getMagicComponent().getSpell();
					if(spell.range > 0) {
						bus.post(new TransitionEvent("aim"));
					} else {
						bus.post(new MagicEvent.OnSelf(this, player, spell));
					}
				} else if(player.getInventoryComponent().hasEquiped(Slot.MAGIC)) {
					Item item = (Item)Engine.getGame().getEntity(player.getInventoryComponent().get(Slot.MAGIC));
					if(item.getMagicComponent().getSpell().range > 0) {
						bus.post(new TransitionEvent("aim"));
					} else {
						bus.post(new MagicEvent.ItemOnSelf(this, player, item));
					}

				} 
				break;
			}
		}
	}
}
