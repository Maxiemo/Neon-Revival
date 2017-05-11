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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import neon.entities.Door;
import neon.entities.Item;
import neon.entities.components.Lock;
import neon.resources.RItem;
import neon.util.TextureFactory;

public class ItemRenderer {
	public static void paint(Item item, Graphics2D graphics, float zoom, boolean isSelected) {
		if(item instanceof Door) {
			paintDoor((Door)item, graphics, zoom, isSelected);
		} else {
			paintItem(item, graphics, zoom, isSelected);
		}
	}

	public static void paintItem(Item item, Graphics2D graphics, float zoom, boolean isSelected) {
		Rectangle bounds = item.getShapeComponent();
		Rectangle2D rect = new Rectangle2D.Float(bounds.x*zoom, bounds.y*zoom, zoom, zoom);
		graphics.setPaint(TextureFactory.getTexture(item.resource.text, (int)zoom, item.resource.color));
		graphics.fill(rect);		
	}

	public static void paintDoor(Door door, Graphics2D graphics, float zoom, boolean isSelected) {
		Rectangle bounds = door.getShapeComponent();
		Rectangle2D rect = new Rectangle2D.Float(bounds.x*zoom, bounds.y*zoom, zoom, zoom);
		String text = door.resource.text;
		Lock lock = door.lock;
		if(lock != null) {
			if(lock.isLocked()) {
				text = ((RItem.Door)door.resource).locked;
			} else if(lock.isClosed()) {
				text = ((RItem.Door)door.resource).closed;
			}
		}
		graphics.setPaint(TextureFactory.getTexture(text, (int)zoom, door.resource.color));
		graphics.fill(rect);		
	}
}
