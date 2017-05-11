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

package neon.ui.custom;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextAlignment;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import neon.entities.Armor;
import neon.entities.Clothing;
import neon.entities.Container;
import neon.entities.Door;
import neon.entities.Entity;
import neon.entities.Item;
import neon.entities.Weapon;
import neon.entities.components.Enchantment;
import neon.entities.components.ShapeComponent;
import neon.resources.RClothing;
import neon.resources.RSpell;
import neon.resources.RWeapon;
import neon.ui.graphics.ItemRenderer;

public class DescriptionPane extends Label {
	public DescriptionPane() {
		setTextAlignment(TextAlignment.CENTER);
		setContentDisplay(ContentDisplay.TOP);
	}
	
	public void update(Entity entity, String coin) {
		if(entity != null) {
			// eerst prentje in orde brengen
			BufferedImage image = new BufferedImage(50, 60, BufferedImage.TYPE_INT_RGB);
			Graphics2D buffer = image.createGraphics();
			ShapeComponent bounds = entity.getShapeComponent();
			buffer.translate(-bounds.x * 50, -bounds.y * 50);
			ItemRenderer.paint((Item)entity, buffer, 50, false);
			setGraphic(new ImageView(SwingFXUtils.toFXImage(image, null)));
			
			StringBuffer description = new StringBuffer();
			description.append(entity.toString());
			description.append("\n");

			if(entity instanceof Door) {
			} else if(entity instanceof Container) {
			} else if(entity instanceof Item) {
				Item item = (Item)entity;
				if(item.resource.cost > 0) {
					description.append("\n");
					description.append("Price: " + item.resource.cost + " " + coin);
				}
				if(item.resource.weight > 0) {
					description.append("\n");
					description.append("Weight: " + item.resource.weight);
				}
				
				Enchantment enchantment = item.getMagicComponent();
				if(enchantment != null) {
					String def = item instanceof Item.Food ? "Effect: " : "Enchantment: ";
					RSpell spell = enchantment.getSpell();
					String text = (spell.name != null ? spell.name : spell.id);
					description.append("\n");
					description.append(def + text);
				}
				if(item instanceof Armor) {
					RClothing resource = (RClothing)item.resource;
					description.append("\n");
					description.append("Type: " + resource.slot.toString().toLowerCase());
					description.append("\n");
					description.append("Armor class: " + resource.kind.toString().toLowerCase());
					description.append("\n");
					description.append("Armor rating: " + resource.rating);
					description.append("\n");
					description.append("State: " + ((Armor)item).getState() + "%");
				} else if(item instanceof Clothing) {
					RClothing resource = (RClothing)item.resource;
					description.append("\n");
					description.append("Type: " + resource.slot.toString().toLowerCase());
				} else if(item instanceof Weapon) {
					RWeapon resource = (RWeapon)item.resource;
					description.append("\n");
					description.append("Type: " + resource.weaponType);
					description.append("\n");
					description.append("Damage: " + resource.damage);
					description.append("\n");
					description.append("State: " + ((Weapon)item).getState() + "%");
					if(enchantment != null) {
					description.append("\n");
					description.append("Magic charge: " + enchantment.getMana() + 
							"/" + enchantment.getBaseMana());
					}
				}
			}

			setText(description.toString());
		} else {
			setGraphic(null);
			setText(null);
		}
	}
}
