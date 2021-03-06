/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2010 - Maarten Driesen
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

package neon.util.spatial;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimpleIndex<E> implements SpatialIndex<E> {
	private CopyOnWriteArrayList<E> elements = new CopyOnWriteArrayList<E>();
	private Rectangle bounds;
	
	public Collection<E> getElements() {
		return elements;
	}

	public Collection<E> getElements(Rectangle bounds) {
		return elements;
	}

	public void insert(E e, Rectangle bounds) {
		elements.add(e);
		if(this.bounds == null) {
			this.bounds = new Rectangle(bounds);
		} else {
			this.bounds.add(bounds);
		}
	}

	public void remove(E e) {
		elements.remove(e);
	}
	
	public void clear() {
		elements.clear();
	}
	
	public int getWidth() {
		return bounds != null ? bounds.width : 0;
	}
	
	public int getHeight() {
		return bounds != null ? bounds.height : 0;
	}
}
