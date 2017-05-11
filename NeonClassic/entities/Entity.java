/*
 *	Neon, a roguelike engine.
 *	Copyright (C) 2012-2013 - Maarten Driesen
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

package neon.entities;

import java.awt.Rectangle;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import neon.entities.components.Component;
import neon.entities.components.ScriptComponent;
import neon.entities.components.ShapeComponent;
import neon.ui.graphics.Renderable;
import neon.ui.graphics.shapes.JVShape;

/**
 * This class represents a game entity that can be drawn on screen.
 * 
 * @author mdriesen
 */
public abstract class Entity implements Renderable {
	// components
	public final ShapeComponent bounds;
	
	protected ClassToInstanceMap<Component> components = MutableClassToInstanceMap.create();

	private final long uid;
	private final String id;
	private int z = Byte.MAX_VALUE - 1;
	private JVShape shape;

	/**
	 * @param id	the id of the resource this entity is an instance of
	 * @param uid
	 */
	public Entity(String id, long uid) {
		this.id = id;
		this.uid = uid;

		// components
		bounds = new ShapeComponent(this, 0, 0, 1, 1);
		components.putInstance(ScriptComponent.class, new ScriptComponent(uid));
	}

	/**
	 * @return	the uid of this entity
	 */
	public long getUID() {
		return uid;
	}

	/**
	 * @return	the id of the resource this entity is an instance of
	 */
	public String getID() {
		return id;
	}

	public ShapeComponent getShapeComponent() {
		return bounds;
	}

	public JVShape getRenderComponent() {
		return shape;
	}

	public void setRenderComponent(JVShape shape) {
		this.shape = shape;
	}

	public ScriptComponent getScriptComponent() {
		return components.getInstance(ScriptComponent.class);
	}
	
	@Override
	public int getZ() {
		return z;
	}

	@Override
	public void setZ(int z) {
		this.z = z;		
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}
}
