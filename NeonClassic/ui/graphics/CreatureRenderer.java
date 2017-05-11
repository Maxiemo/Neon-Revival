/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2014 - Maarten Driesen
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

package neon.ui.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import neon.entities.Creature;
import neon.entities.Player;
import neon.entities.property.Condition;
import neon.resources.RCreature;
import neon.resources.RCreature.Size;
import neon.util.ColorFactory;
import neon.util.TextureFactory;

public class CreatureRenderer {
	public static void paint(Creature creature, Graphics2D graphics, float zoomf, boolean isSelected) {
		if(creature instanceof Player) {
			paintPlayer((Player)creature, graphics, zoomf, isSelected);
		} else {
			paintCreature(creature, graphics, zoomf, isSelected);
		}
	}
		
	private static void paintPlayer(Player player, Graphics2D graphics, float zoomf, boolean isSelected) {
		paintCreature(player, graphics, zoomf, isSelected);
		Rectangle bounds = player.getShapeComponent();
		int x = bounds.x;
		int y = bounds.y;
		int zoom = (int)zoomf;
		graphics.setPaint(ColorFactory.getColor(player.species.color));
		graphics.drawLine(x*zoom + 2, y*zoom + zoom, x*zoom + zoom - 4, y*zoom + zoom);
		if(player.isSneaking()) {
			graphics.drawLine(x*zoom + 2, y*zoom, x*zoom + zoom - 4, y*zoom);
		}		
	}

	private static void paintCreature(Creature creature, Graphics2D graphics, float zoomf, boolean isSelected) {
		RCreature species = creature.species;
		Rectangle bounds = creature.getShapeComponent();
		int x = bounds.x;
		int y = bounds.y;
		String text = creature.hasCondition(Condition.DEAD) ? "%" : species.text;
		int zoom = (int)zoomf;
		Color color = ColorFactory.getColor(species.color);
		if(species.size == Size.tiny) {
			graphics.drawImage(TextureFactory.getImage(text, zoom*2/3, color), x*zoom + zoom/6, y*zoom + zoom/6, zoom*2/3, zoom*2/3, null);
		} else if(species.size == Size.huge) {
			graphics.drawImage(TextureFactory.getImage(text, zoom*3/2, color), x*zoom - zoom/4, y*zoom - zoom/4, zoom*3/2, zoom*3/2, null);
		} else {
			graphics.drawImage(TextureFactory.getImage(text, zoom, color), x*zoom, y*zoom, zoom, zoom, null);
		}
		if(creature.getActiveSpells().size() != 0) {
			graphics.setPaint(Color.blue);
			graphics.drawOval(x*zoom, y*zoom, bounds.width*zoom, bounds.height*zoom);
		} 		
	}
}
