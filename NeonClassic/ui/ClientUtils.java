/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2015 - Maarten Driesen
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

import javafx.stage.Stage;
import neon.core.Engine;
import neon.entities.Creature;
import neon.entities.Weapon;
import neon.entities.components.Inventory;
import neon.entities.property.Slot;
import neon.resources.RWeapon.WeaponType;

public class ClientUtils {
	/**
	 * Returns a string with the amount of money in big and small denominations.
	 * 
	 * @param money
	 * @param big
	 * @param small
	 * @return
	 */
	public static String moneyString(Creature creature, String big, String small) {
		int money = creature.getInventoryComponent().getMoney();
		
		if(money == 0) {
			return "No money.";
		} else {
			int gold = (money - money%100)/100;
			int copper = money%100;
			return "Money: " + gold + " " + big + " and " + copper + " " + small + ".";
		}
	}
	
	/**
	 * Centers the given stage on the main user interface stage.
	 * 
	 * @param stage
	 * @param ui
	 */
	public static void centerStage(Stage stage, UserInterface ui) {
		stage.setWidth(ui.getStage().getWidth() - 20);
		stage.setHeight(ui.getStage().getHeight() - 50);
		stage.setX(ui.getStage().getX() + ui.getStage().getWidth()/2 - stage.getWidth()/2);
		stage.setY(ui.getStage().getY() + ui.getStage().getHeight()/2 - stage.getHeight()/2);		
	}
	
	/**
	 * Returns a string with the attack value of the given creature.
	 * 
	 * @param creature
	 * @return	the given creature's AV
	 */
	public static String getAVString(Creature creature) {
		Inventory inventory = creature.getInventoryComponent();
		String damage;
		
		if(inventory.hasEquiped(Slot.WEAPON)) {
			Weapon weapon = (Weapon)Engine.getGame().getEntity(inventory.get(Slot.WEAPON));
			damage = weapon.getDamage();
			if(weapon.getWeaponType().equals(WeaponType.BOW) || 
					weapon.getWeaponType().equals(WeaponType.CROSSBOW)) {
				Weapon ammo = (Weapon)Engine.getGame().getEntity(inventory.get(Slot.AMMO));
				damage += " : " + ammo.getDamage();
			}	
		} else if(inventory.hasEquiped(Slot.AMMO)) {
			Weapon ammo = (Weapon)Engine.getGame().getEntity(inventory.get(Slot.AMMO));
			damage = ammo.getDamage();
		} else {
			damage = creature.species.av;
		}
		
		return damage;
	}
}
