/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2012-2014 - Maarten Driesen
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
import java.awt.event.*;
import java.util.*;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.Popup;

import com.google.common.eventbus.EventBus;

import neon.core.Atlas;
import neon.core.Engine;
import neon.core.event.CombatEvent;
import neon.core.event.MagicEvent;
import neon.core.handlers.*;
import neon.entities.Creature;
import neon.entities.Door;
import neon.entities.Item;
import neon.entities.Player;
import neon.entities.Weapon;
import neon.entities.property.Slot;
import neon.maps.Zone;
import neon.resources.CClient;
import neon.resources.RWeapon.WeaponType;
import neon.systems.animation.Translation;
import neon.ui.GamePanel;
import neon.ui.UserInterface;
import neon.ui.graphics.shapes.JVText;
import neon.util.fsm.*;

/**
 * Implements all methods to aim at something. This is done by showing a cursor 
 * on the main game field.
 * 
 * @author mdriesen
 */
public class AimState extends State {
	private Point target;
	private Player player;
	private JVText cursor;
	private Popup popup;
	private GamePanel panel;
	private CClient keys;
	private EventBus bus;
	private UserInterface ui;
	private Atlas atlas;
	
	/**
	 * Constructs a new AimModule.
	 */
	public AimState(State state, EventBus bus, UserInterface ui, CClient config) {
		super(state);
		this.bus = bus;
		this.ui = ui;
		keys = config;
		target = new Point();
	}

	@Override
	public void enter(TransitionEvent e) {
		panel = (GamePanel)getVariable("panel");
		player = (Player)getVariable("player");
		atlas = Engine.getGame().getAtlas();
		Rectangle bounds = player.getShapeComponent();
		target.setLocation(bounds.getLocation());
		setKeys();
		panel.print("Use arrow keys to move cursor. Press L to cancel, " +
				"F to shoot, T to talk and G to cast a spell.");
		cursor = panel.showCursor();
	}
	
	@Override
	public void exit(TransitionEvent e) {
		if(popup != null) {	// kan gebeuren als popup nog niet geset is in look()
			popup.hide();
		}
		panel.hideCursor();
		panel.repaint();
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
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.magic), "magic");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.shoot), "shoot");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.look), "look");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.act), "act");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys.talk), "talk");
        panel.getActionMap().put("up", new ButtonAction("up"));
        panel.getActionMap().put("upleft", new ButtonAction("upleft"));
        panel.getActionMap().put("left", new ButtonAction("left"));
        panel.getActionMap().put("downleft", new ButtonAction("downleft"));
        panel.getActionMap().put("down", new ButtonAction("down"));
        panel.getActionMap().put("downright", new ButtonAction("downright"));
        panel.getActionMap().put("right", new ButtonAction("right"));
        panel.getActionMap().put("upright", new ButtonAction("upright"));		
        panel.getActionMap().put("magic", new ButtonAction("magic"));		
        panel.getActionMap().put("shoot", new ButtonAction("shoot"));		
        panel.getActionMap().put("look", new ButtonAction("look"));		
        panel.getActionMap().put("act", new ButtonAction("act"));		
        panel.getActionMap().put("talk", new ButtonAction("talk"));		
	}

	private void shoot() {
		Rectangle bounds = player.getShapeComponent();
		if(target.distance(bounds.x, bounds.y) < 5) {
			Creature victim = atlas.getCurrentZone().getCreature(target);
			if(victim != null) {
				Weapon ammo = (Weapon)Engine.getGame().getEntity(player.getInventoryComponent().get(Slot.AMMO));
				if(player.getInventoryComponent().hasEquiped(Slot.AMMO) && ammo.getWeaponType() == WeaponType.THROWN) {
					shoot(ammo, victim);
					bus.post(new CombatEvent(CombatEvent.FLING, player, victim));
				} else if(CombatUtils.getWeaponType(player) == WeaponType.BOW) {
					if(player.getInventoryComponent().hasEquiped(Slot.AMMO) && ammo.getWeaponType() == WeaponType.ARROW) {
						shoot(ammo, victim);
						bus.post(new CombatEvent(CombatEvent.SHOOT, player, victim));
					} else {
						ui.showMessage("No arrows equiped!", 1);
					}
				} else if(CombatUtils.getWeaponType(player) == WeaponType.CROSSBOW) {
					if(player.getInventoryComponent().hasEquiped(Slot.AMMO) && ammo.getWeaponType() == WeaponType.BOLT) {
						bus.post(new CombatEvent(CombatEvent.SHOOT, player, victim));
					} else {
						ui.showMessage("No bolts equiped!", 1);
					}
				} else {
					ui.showMessage("No ranged weapon equiped!", 1);
				}
			} else {
				ui.showMessage("No target!", 1);
			}
		} else {
			ui.showMessage("Out of range!", 1);
		}		
		bus.post(new TransitionEvent("return"));			
	}
	
	private void shoot(Item projectile, Creature victim) {
		// get bounds of all involved entities
		Rectangle prBounds = projectile.getShapeComponent();
		Rectangle vBounds = victim.getShapeComponent();
		Rectangle plBounds = player.getShapeComponent();

		// shoot
		prBounds.setLocation(vBounds.x, vBounds.y);
		atlas.getCurrentZone().addItem(projectile);
		new Thread(new Translation(projectile, plBounds.x, plBounds.y, 
				vBounds.x, vBounds.y, 100, panel)).start();
	}
	
	private void talk() {
		Rectangle bounds = player.getShapeComponent();
		if(target.distance(bounds.getLocation()) < 2) {
			Creature creature = atlas.getCurrentZone().getCreature(target);
			if(creature != null) {
				if(creature.hasDialog()) {
					// dialog module
					bus.post(new TransitionEvent("dialog", "speaker", creature));
				} else {
					bus.post(new TransitionEvent("return", "message", "Creature can't talk."));
				}
			} else {
				bus.post(new TransitionEvent("return", "message", "No person to talk to selected."));
			}
		} else {
			ui.showMessage("Too far away.", 1);
		}		
	}
	
	private void cast() {
		if(player.getMagicComponent().getSpell() != null) {
			bus.post(new MagicEvent.CreatureOnPoint(this, player, target));
		} else if(player.getInventoryComponent().hasEquiped(Slot.MAGIC)) {
			Item item = (Item)Engine.getGame().getEntity(player.getInventoryComponent().get(Slot.MAGIC));
			bus.post(new MagicEvent.ItemOnPoint(this, player, item, target));
		}

		bus.post(new TransitionEvent("return"));
	}
	
	private void look() {
		cursor.setX(target.x);
		cursor.setY(target.y);
		panel.repaint();
		// beschrijving van waar naar gekeken wordt
		Rectangle bounds = player.getShapeComponent();
		if(target.distance(bounds.getLocation()) < 20) {
			Zone zone = atlas.getCurrentZone();
			String items = "";
			String actors = "";
			ArrayList<Long> things = new ArrayList<Long>(zone.getItems(target));
			if(things.size() == 1) {
				items = ", " + Engine.getGame().getEntity(things.get(0));
			} else if(things.size() > 1) {
				items = ", several items";
			}
			Creature creature = atlas.getCurrentZone().getCreature(target);
			if(creature != null) {
				actors = ", " + creature.toString();
			}
			popup = ui.showPopup(zone.getRegion(target) + items + actors);
		} else {
			popup = ui.showPopup("Too far away.");			
		}
	}
	
	private void act() {
		for(long uid : atlas.getCurrentZone().getItems(target)) {
			if(Engine.getGame().getEntity(uid) instanceof Door) {
				bus.post(new TransitionEvent("door", "door", Engine.getGame().getEntity(uid)));
				break;
			}
		}
	}

	@SuppressWarnings("serial")
	private class ButtonAction extends AbstractAction {
		public ButtonAction(String command) {
			putValue(ACTION_COMMAND_KEY, command);
		}

		@Override
		public void actionPerformed(ActionEvent ae) {
			switch(ae.getActionCommand()) {
			case "up":
				target.y--;
				look();
				break;
			case "upright":
				target.x++; target.y--;
				look();
				break;
			case "right":
				target.x++;
				look();
				break;
			case "downright":
				target.x++; target.y++;
				look();
				break;
			case "down":
				target.y++;
				look();
				break;
			case "downleft":
				target.x--;	target.y++;
				look();
				break;
			case "left":
				target.x--;
				look();
				break;
			case "upleft":
				target.x--; target.y--;
				look();
				break;
			case "act":
				act();
			case "look":
				bus.post(new TransitionEvent("return", "message", "Aiming cancelled."));
				break;
			case "shoot":
				shoot();
				break;
			case "magic":
				cast();
				break;
			case "talk":
				talk();
				break;
			}
		}
	}
}
